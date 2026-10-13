package com.myhook.features

import android.content.ContentResolver
import com.myhook.core.ConfigKeys
import com.myhook.core.ConfigStore
import com.myhook.core.HookContext

/**
 * Hide developer options / ADB. Ported from Fuck for VIP's hook_development_settings /
 * hook_adb_debugging (development_settings_enabled + adb_enabled -> 0).
 */
object HideDeveloperSettings : Feature {
    override val key = ConfigKeys.HIDE_DEV_ENABLE
    override val title = "隐藏开发者模式 / ADB"
    override val desc = "development_settings_enabled / adb_enabled 一律返回 0"
    override val packages: Set<String> = emptySet()

    private val NAMES = setOf("development_settings_enabled", "adb_enabled")

    override fun apply(ctx: HookContext) {
        if (!ConfigStore.bool(key, false)) return
        for (cls in listOf("android.provider.Settings\$Global", "android.provider.Settings\$Secure")) {
            ctx.hookMethod(cls, "getInt", ContentResolver::class.java, String::class.java, Int::class.java) { chain ->
                if ((chain.getArg(1) as? String) in NAMES) 0 else chain.proceed()
            }
            ctx.hookMethod(cls, "getString", ContentResolver::class.java, String::class.java) { chain ->
                if ((chain.getArg(1) as? String) in NAMES) "0" else chain.proceed()
            }
        }
    }
}
