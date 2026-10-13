package com.myhook.hooks

import com.myhook.core.DexSearch
import com.myhook.core.HookContext

/**
 * MacroDroid (com.arlosoft.macrodroid) — 解锁 Pro。
 *
 * Hook:
 * DexKit search: match a `boolean` method whose body references both "Macro" and "#DDDDDD"
 * (the app's own pro/UI strings) and force it to true.
 */
object MacroDroid : AppHook {
    override val packages = setOf("com.arlosoft.macrodroid")

    override fun apply(ctx: HookContext) {
        DexSearch.findAndHook(
            ctx,
            returnType = "boolean",
            strings = listOf("Macro", "#DDDDDD"),
        ) { true }
    }
}
