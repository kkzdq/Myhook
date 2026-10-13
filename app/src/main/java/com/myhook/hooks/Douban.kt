package com.myhook.hooks

import com.myhook.core.HookContext

/**
 * 豆瓣 (com.douban.frodo) — 去信息流广告。
 *
 * Ported from Fuck for VIP (FFV) — the app has no HookVip hook.
 * FFV callback `p169o00o00oO/o000OOo` case 20:
 *   com.douban.frodo.baseproject.ad.model.FeedAd#isBlocked() -> true
 */
object Douban : AppHook {
    override val packages = setOf("com.douban.frodo")

    override fun apply(ctx: HookContext) {
        ctx.hookMethod("com.douban.frodo.baseproject.ad.model.FeedAd", "isBlocked") { true }
    }
}
