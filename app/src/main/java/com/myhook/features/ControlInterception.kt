package com.myhook.features

import com.myhook.core.ConfigKeys
import com.myhook.core.HookContext
import com.myhook.core.HookLog

/**
 * 控件拦截 — ported from Fuck for VIP's `hook_view_block` (displayed as "control_interception").
 * Blocks `ViewStub.inflate()` so lazy-loaded ad layouts never get created.
 */
object ControlInterception : Feature {
    override val key = ConfigKeys.FEATURE_CONTROL_INTERCEPT
    override val title = "控件拦截"
    override val desc = "拦掉 ViewStub 懒加载的广告控件"

    override fun apply(ctx: HookContext) {
        ctx.hookMethod("android.view.ViewStub", "inflate") { null }
        HookLog.i("ControlInterception installed for ${ctx.packageName}")
    }
}
