package com.myhook.hooks

import com.myhook.core.HookContext

/**
 * 甲壳虫ADB助手 (com.didjdk.adbhelper) — 解锁会员。
 *
 * Ported from Fuck for VIP (FFV) — the app has no HookVip hook.
 * FFV callback `p169o00o00oO/o000OOo` case 16:
 *   b.s.m#J(android.content.Context) -> true
 */
object AdbHelper : AppHook {
    override val packages = setOf("com.didjdk.adbhelper")

    override fun apply(ctx: HookContext) {
        ctx.hookMethod("b.s.m", "J", android.content.Context::class.java) { true }
    }
}
