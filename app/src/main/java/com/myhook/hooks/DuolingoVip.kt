package com.myhook.hooks

import com.myhook.core.HookContext

/**
 * 多邻国 — 解锁订阅（Super）。
 * Hook: com.duolingo.data.user.SubscriberLevel.hasSubscription() -> true
 *
 * Coexists with [DuolingoTimezone] (different capability, same app).
 */
object DuolingoVip : AppHook {
    override val packages = setOf("com.duolingo")

    override fun apply(ctx: HookContext) {
        ctx.hookMethod("com.duolingo.data.user.SubscriberLevel", "hasSubscription") { true }
    }
}
