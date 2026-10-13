package com.myhook.hooks

import com.myhook.core.HookContext

/**
 * 企业微信 (com.tencent.wework) — 强制开启平板模式。
 *
 * Hook:
 *   com.tencent.wework.foundation.impl.WeworkServiceImpl#isAndroidPad() -> true
 */
object WeWork : AppHook {
    override val packages = setOf("com.tencent.wework")

    override fun apply(ctx: HookContext) {
        ctx.hookMethod(
            "com.tencent.wework.foundation.impl.WeworkServiceImpl",
            "isAndroidPad",
        ) { true }
    }
}
