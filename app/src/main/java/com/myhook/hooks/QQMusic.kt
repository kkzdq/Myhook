package com.myhook.hooks

import com.myhook.core.DexSearch
import com.myhook.core.HookContext

/**
 * QQ音乐 (com.tencent.qqmusic) — 解锁超品音质 / 去下载限制。
 *
 * Hook:
 * DexKit method searches:
 *   1. returnType void, uses "DownloadSongTask" + "redownloadImmediately enter" -> skip
 *   2. returnType boolean, uses "[checkPay] superVip:"                          -> true
 *   3. returnType boolean, uses "[onUserTry] invalid user or super VIP("        -> true
 */
object QQMusic : AppHook {
    override val packages = setOf("com.tencent.qqmusic")

    override fun apply(ctx: HookContext) {
        DexSearch.findAndHook(
            ctx, returnType = "void",
            strings = listOf("DownloadSongTask", "redownloadImmediately enter"),
        ) { null }

        DexSearch.findAndHook(
            ctx, returnType = "boolean",
            strings = listOf("[checkPay] superVip:"),
        ) { true }

        DexSearch.findAndHook(
            ctx, returnType = "boolean",
            strings = listOf("[onUserTry] invalid user or super VIP("),
        ) { true }
    }
}
