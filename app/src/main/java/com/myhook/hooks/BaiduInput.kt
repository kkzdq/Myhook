package com.myhook.hooks

import com.myhook.core.HookContext

/**
 * 百度输入法 — 解除剪切板数量/字数上限。
 * Hook: com.baidu.input.sync.clipboard.ClipboardSyncHelperKt.a()/b() -> Integer.MAX_VALUE
 */
object BaiduInput : AppHook {
    override val packages = setOf("com.baidu.input")

    override fun apply(ctx: HookContext) {
        ctx.hookMethod("com.baidu.input.sync.clipboard.ClipboardSyncHelperKt", "a") { Int.MAX_VALUE }
        ctx.hookMethod("com.baidu.input.sync.clipboard.ClipboardSyncHelperKt", "b") { Int.MAX_VALUE }
    }
}
