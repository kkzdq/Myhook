package com.myhook.hooks

import com.myhook.core.HookContext

/**
 * 阿里云盘 (com.alicloud.databox) — 解锁会员/试用。
 *
 * Hook:
 * Hook:
 *   com.alicloud.databox.account.service.idl.business.UserTrialResponse#getTrialStatus() -> "1"
 *   com.alicloud.databox.account.service.idl.business.BusinessFeature#getTrialStatus()     -> "1"
 *   com.alicloud.databox.account.service.idl.business.BusinessFeature#getLastTrialTime()   -> 218330035688L
 *   com.alicloud.databox.account.service.idl.business.BusinessFeature#getIntercept()       -> false
 */
object AliCloud : AppHook {
    override val packages = setOf("com.alicloud.databox")

    private const val IDL = "com.alicloud.databox.account.service.idl.business"

    override fun apply(ctx: HookContext) {
        ctx.hookMethod("$IDL.UserTrialResponse", "getTrialStatus") { "1" }
        ctx.hookMethod("$IDL.BusinessFeature", "getTrialStatus") { "1" }
        ctx.hookMethod("$IDL.BusinessFeature", "getLastTrialTime") { 218330035688L }
        ctx.hookMethod("$IDL.BusinessFeature", "getIntercept") { false }
    }
}
