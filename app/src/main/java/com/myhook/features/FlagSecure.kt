package com.myhook.features

import com.myhook.core.ConfigKeys
import com.myhook.core.ConfigStore
import com.myhook.core.HookContext

/**
 * Disable FLAG_SECURE so the target window can be screenshotted / screen-recorded.
 * Ported from Fuck for VIP's hook_flag_secure.
 */
object FlagSecure : Feature {
    override val key = ConfigKeys.FLAG_SECURE_ENABLE
    override val title = "允许截屏（去 FLAG_SECURE）"
    override val desc = "清除 Window 上的 FLAG_SECURE 位"
    override val packages: Set<String> = emptySet()

    private const val FLAG_SECURE = 0x2000

    override fun apply(ctx: HookContext) {
        if (!ConfigStore.bool(key, false)) return

        ctx.hookMethod("android.view.Window", "setFlags", Int::class.java, Int::class.java) { chain ->
            val flags = (chain.getArg(0) as? Int) ?: 0
            val mask = (chain.getArg(1) as? Int) ?: 0
            chain.proceed(arrayOf<Any?>(flags and FLAG_SECURE.inv(), mask and FLAG_SECURE.inv()))
        }
        ctx.hookMethod("android.view.Window", "addFlags", Int::class.java) { chain ->
            val flags = (chain.getArg(0) as? Int) ?: 0
            chain.proceed(arrayOf<Any?>(flags and FLAG_SECURE.inv()))
        }
    }
}
