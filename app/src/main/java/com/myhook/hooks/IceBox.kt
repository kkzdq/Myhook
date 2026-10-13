package com.myhook.hooks

import com.myhook.core.HookContext
import com.myhook.core.HookLog

/**
 * 冰箱 IceBox (com.catchingnow.icebox) — 解锁高级版。
 *
 * Ported from HookVip `C1722` / `C1694` (DexKit) + action `C0826(23)`:
 *   the matched method is MMKV's `getBoolean` (matcher: class `com.tencent.mmkv.MMKV`,
 *   name `getBoolean`); when the call is about the key "is_purchased" the result is forced to true.
 *
 * `com.tencent.mmkv.MMKV` is a *library* class with a stable name, so we hook it directly instead
 * of running a DexKit search. Both argument positions are checked (key vs. default value) so the
 * behaviour does not depend on which one HookVip matched.
 */
object IceBox : AppHook {
    override val packages = setOf("com.catchingnow.icebox")

    private const val KEY = "is_purchased"

    override fun apply(ctx: HookContext) {
        val ok = ctx.hookMethod(
            "com.tencent.mmkv.MMKV",
            "getBoolean", String::class.java, Boolean::class.javaPrimitiveType!!,
        ) { chain ->
            if (chain.args.any { it == KEY }) true else chain.proceed()
        }
        if (!ok) HookLog.w("icebox: MMKV#getBoolean not hooked")
    }
}
