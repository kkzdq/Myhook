package com.myhook.hooks

import com.myhook.core.DexSearch
import com.myhook.core.HookContext

/**
 * Via 浏览器 (mark.via) — 屏蔽内置广告域名列表。
 *
 * Ported from Fuck for VIP (FFV) — the app has no HookVip hook.
 * FFV used DexKit: match a `void` method whose body references
 * "eW91a3UuY29tLGlxaXlpLmNvbSxtZ3R2LmNvbSxxcS5jb20=" (base64 of
 * "youku.com,iqiyi.com,mgtv.com,qq.com") and skip it (return null).
 */
object Via : AppHook {
    override val packages = setOf("mark.via")

    private const val DOMAIN_BLOB = "eW91a3UuY29tLGlxaXlpLmNvbSxtZ3R2LmNvbSxxcS5jb20="

    override fun apply(ctx: HookContext) {
        DexSearch.findAndHook(ctx, returnType = "void", strings = listOf(DOMAIN_BLOB)) { null }
    }
}
