package com.myhook.hooks

import com.myhook.core.DexSearch
import com.myhook.core.HookContext

/**
 * 酷安 (com.coolapk.market) — 去广告。
 *
 * Hook:
 * DexKit method searches:
 *   1. uses "feedDetailReplySponsorCard" + "sponsorType"                     -> null
 *   2. returnType void, uses "410.500" + "410.000"                           -> null
 *   3. returnType boolean, uses "广告请求开关已关闭,请联系穿山甲管理员" + "loadNativeAd" -> false
 */
object CoolApk : AppHook {
    override val packages = setOf("com.coolapk.market")

    override fun apply(ctx: HookContext) {
        DexSearch.findAndHook(
            ctx, returnType = null,
            strings = listOf("feedDetailReplySponsorCard", "sponsorType"),
        ) { null }

        DexSearch.findAndHook(
            ctx, returnType = "void",
            strings = listOf("410.500", "410.000"),
        ) { null }

        DexSearch.findAndHook(
            ctx, returnType = "boolean",
            strings = listOf("广告请求开关已关闭,请联系穿山甲管理员", "loadNativeAd"),
        ) { false }
    }
}
