package com.myhook.hooks

import com.myhook.core.DexSearch
import com.myhook.core.HookContext

/**
 * 醒图 (com.xt.retouch) — 解锁会员功能。
 *
 * Ported from HookVip `C3627` / `C3579` (DexKit) + actions `C3084(24)` / `C3084(25)`:
 *   1. uses "SubscribeSdkHelper" + "getUserVipInfo error" -> 把返回的 VIP 对象字段改成永久会员
 *      (flag=true, endTime/vipRealEnd/benefitEndAt=113143670061000L, curVipLevel="vip", startTime=now)
 *   2. uses "SubscribeApiImpl" + "no strategy prop: " + "force local auth" -> true
 */
object Retouch : AppHook {
    override val packages = setOf("com.xt.retouch")

    private const val FOREVER = 113143670061000L

    override fun apply(ctx: HookContext) {
        DexSearch.findAndHook(
            ctx, returnType = null,
            strings = listOf("SubscribeSdkHelper", "getUserVipInfo error"),
        ) { chain ->
            val r = chain.proceed()
            if (r != null) {
                ctx.setField(r, "flag", true)
                ctx.setField(r, "endTime", FOREVER)
                ctx.setField(r, "vipRealEnd", FOREVER)
                ctx.setField(r, "benefitEndAt", FOREVER)
                ctx.setField(r, "curVipLevel", "vip")
                ctx.setField(r, "startTime", System.currentTimeMillis())
            }
            r
        }

        DexSearch.findAndHook(
            ctx, returnType = null,
            strings = listOf("SubscribeApiImpl", "no strategy prop: ", "force local auth"),
        ) { true }
    }
}
