package com.myhook.hooks

import com.myhook.core.ConfigKeys
import com.myhook.core.ConfigStore
import com.myhook.core.HookContext
import com.myhook.core.HookLog
import java.time.ZoneId
import java.util.TimeZone

/**
 * Duolingo (com.duolingo) timezone faker — ported from duotimefaker.
 *
 * Note: this is a *different* capability from HookVip's Duolingo hook (which unlocks Super by
 * forcing SubscriberLevel.hasSubscription() -> true). Both can coexist.
 */
object DuolingoTimezone : AppHook {

    override val packages = setOf("com.duolingo")

    private const val DEFAULT_TZ = "Asia/Tokyo"

    override fun apply(ctx: HookContext) {
        if (!ConfigStore.bool(ConfigKeys.DUO_ENABLE, false)) {
            HookLog.i("Duolingo timezone hook disabled")
            return
        }
        val tz = ConfigStore.string(ConfigKeys.DUO_TIMEZONE, DEFAULT_TZ)

        ctx.hookMethod("java.util.TimeZone", "getDefault") { TimeZone.getTimeZone(tz) }
        ctx.hookMethod("java.time.ZoneId", "systemDefault") { ZoneId.of(tz) }
        ctx.hookMethod("java.lang.System", "getProperty", String::class.java) { chain ->
            if (chain.getArg(0) == "user.timezone") tz else chain.proceed()
        }

        // android.icu.util.TimeZone.getDefault()
        try {
            val icu = Class.forName("android.icu.util.TimeZone", false, ctx.classLoader)
            val getDefault = icu.getMethod("getDefault")
            val getTimeZone = icu.getMethod("getTimeZone", String::class.java)
            ctx.xposed.hook(getDefault).intercept { getTimeZone.invoke(null, tz) }
            HookLog.i("hooked android.icu.util.TimeZone#getDefault")
        } catch (t: Throwable) {
            HookLog.w("ICU TimeZone hook failed: $t")
        }
    }
}
