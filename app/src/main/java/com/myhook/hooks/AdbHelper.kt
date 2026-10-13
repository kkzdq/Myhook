package com.myhook.hooks

import com.myhook.core.HookContext

/**
 * 甲壳虫ADB助手 (com.didjdk.adbhelper) — 解锁会员。
 *
 * Hook:
 * Hook:
 *   b.s.m#J(android.content.Context) -> true
 */
object AdbHelper : AppHook {
    override val packages = setOf("com.didjdk.adbhelper")

    override fun apply(ctx: HookContext) {
        ctx.hookMethod("b.s.m", "J", android.content.Context::class.java) { true }
    }
}
