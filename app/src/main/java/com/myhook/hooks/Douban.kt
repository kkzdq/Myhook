package com.myhook.hooks

import com.myhook.core.HookContext

/**
 * 豆瓣 (com.douban.frodo) — 去信息流广告。
 *
 * Hook:
 * Hook:
 *   com.douban.frodo.baseproject.ad.model.FeedAd#isBlocked() -> true
 */
object Douban : AppHook {
    override val packages = setOf("com.douban.frodo")

    override fun apply(ctx: HookContext) {
        ctx.hookMethod("com.douban.frodo.baseproject.ad.model.FeedAd", "isBlocked") { true }
    }
}
