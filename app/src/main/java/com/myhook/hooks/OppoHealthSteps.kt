package com.myhook.hooks

import com.myhook.core.ConfigKeys
import com.myhook.core.ConfigStore
import com.myhook.core.HookContext
import com.myhook.core.HookLog
import io.github.libxposed.api.XposedInterface
import java.util.Calendar

/**
 * OPPO Health (com.heytap.health) step editing.
 *
 * Hooks the OPPO data-engine getters, so it keeps working on modern OPPO Health builds and can
 * both *raise* (floor) and *cap* (lower) the value. Only today's record is touched.
 */
object OppoHealthSteps : AppHook {

    override val packages = setOf("com.heytap.health")

    private val STEP_CLASSES = listOf(
        "com.heytap.databaseengine.model.SportDataStat",
        "com.heytap.databaseengine.model.proxy.SportDataStatProxy",
        "com.heytap.databaseengineservice.db.table.DBSportDataStat",
    )

    override fun apply(ctx: HookContext) {
        if (!ConfigStore.bool(ConfigKeys.OPPO_ENABLE, false)) {
            HookLog.i("OPPO step hook disabled")
            return
        }
        var installed = 0
        for (cn in STEP_CLASSES) {
            if (ctx.hookMethod(cn, "getTotalSteps") { chain -> onTotalSteps(chain) }) installed++
        }
        if (installed == 0) {
            HookLog.w("OPPO step hook: no target class matched (OPPO Health version changed?)")
        }
    }

    private fun onTotalSteps(chain: XposedInterface.Chain): Any? {
        val real = chain.proceed()
        if (real !is Int) return real
        if (!isToday(chain.thisObject)) return real
        return applyStep(real)
    }

    private fun applyStep(real: Int): Int {
        val target = ConfigStore.int(ConfigKeys.OPPO_TARGET, 0)
        if (target <= 0) return real
        return if (ConfigStore.int(ConfigKeys.OPPO_MODE, 0) == 1) minOf(real, target)
        else maxOf(real, target)
    }

    private fun isToday(obj: Any?): Boolean {
        if (obj == null) return true
        return try {
            val date = obj.javaClass.getMethod("getDate").invoke(obj) as Int
            date == todayInt()
        } catch (t: Throwable) {
            true
        }
    }

    private fun todayInt(): Int {
        val c = Calendar.getInstance()
        return c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH) + 1) * 100 + c.get(Calendar.DAY_OF_MONTH)
    }
}
