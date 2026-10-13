package com.myhook.hooks

import com.myhook.core.HookContext
import com.myhook.core.HookLog

/**
 * Pydroid 3 (ru.iiec.pydroid3) — 解锁高级版。
 *
 * Ported from Fuck for VIP (FFV) — the app has no HookVip hook.
 * FFV used DexKit to find a `boolean` method whose body references the string
 * "premium_purchased" and forced it to `true`. That string is the app's premium preference key,
 * so hooking the preference read directly is equivalent and needs no runtime dex search:
 *   SharedPreferencesImpl#getBoolean("premium_purchased", ...) -> true
 */
object Pydroid : AppHook {
    override val packages = setOf("ru.iiec.pydroid3")

    private const val KEY = "premium_purchased"

    override fun apply(ctx: HookContext) {
        val cls = ctx.findClass("android.app.SharedPreferencesImpl")
        if (cls == null) {
            HookLog.w("pydroid: SharedPreferencesImpl not found")
            return
        }
        runCatching {
            val m = cls.getDeclaredMethod(
                "getBoolean", String::class.java, Boolean::class.javaPrimitiveType,
            )
            m.isAccessible = true
            ctx.xposed.hook(m).intercept { chain ->
                if ((chain.args.getOrNull(0) as? String) == KEY) true else chain.proceed()
            }
            HookLog.i("pydroid: hooked getBoolean($KEY)")
        }.onFailure { HookLog.w("pydroid hook failed: $it") }
    }
}
