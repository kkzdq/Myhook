package com.myhook.hooks

import com.myhook.core.HookContext
import com.myhook.core.HookLog

/**
 * 外部存储设备 (com.android.externalstorage) — 放开 Android/data 等目录的访问限制。
 *
 * Ported from Fuck for VIP (FFV) — the app has no HookVip hook.
 * FFV `p169o00o00oO/o00000O0.OooO0OO` hooks the platform provider directly:
 *   com.android.externalstorage.ExternalStorageProvider#shouldHideDocument(String)        -> false
 *   com.android.externalstorage.ExternalStorageProvider#shouldBlockDirectoryFromTree(String) -> false
 *   (fallback for older builds) #shouldBlockFromTree(String)                                -> false
 */
object ExternalStorage : AppHook {
    override val packages = setOf("com.android.externalstorage")

    private const val CLS = "com.android.externalstorage.ExternalStorageProvider"

    override fun apply(ctx: HookContext) {
        ctx.hookMethod(CLS, "shouldHideDocument", String::class.java) { false }

        val hooked = ctx.hookMethod(CLS, "shouldBlockDirectoryFromTree", String::class.java) { false }
        if (!hooked) {
            val fb = ctx.hookMethod(CLS, "shouldBlockFromTree", String::class.java) { false }
            if (!fb) HookLog.w("externalstorage: no shouldBlock*FromTree method found")
        }
    }
}
