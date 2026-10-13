package com.myhook.hooks

import com.myhook.core.HookContext

/**
 * 小X分身 (com.bly.dkplat) — 解锁会员。
 * Hook: com.bly.dkplat.cache.UserCache
 *   getVipType() -> 1, isVipUser() -> true, getExpiredTime() -> 113143670061000L
 */
object BiliDkplat : AppHook {
    override val packages = setOf("com.bly.dkplat")

    private const val FOREVER = 113143670061000L

    override fun apply(ctx: HookContext) {
        ctx.hookMethod("com.bly.dkplat.cache.UserCache", "getVipType") { 1 }
        ctx.hookMethod("com.bly.dkplat.cache.UserCache", "isVipUser") { true }
        ctx.hookMethod("com.bly.dkplat.cache.UserCache", "getExpiredTime") { FOREVER }
    }
}
