package com.myhook.features

import android.app.Activity
import com.myhook.core.ConfigKeys
import com.myhook.core.HookContext
import com.myhook.core.HookLog

/**
 * 广告拦截 — ported from Fuck for VIP's `hook_ad`.
 * Auto-finishes the activities of the well-known Chinese ad SDKs.
 */
object AdBlocking : Feature {
    override val key = ConfigKeys.FEATURE_AD_BLOCK
    override val title = "广告拦截"
    override val desc = "自动关闭优量汇/穿山甲/AdMob/Mbridge 的广告页"

    private val AD_ACTIVITIES = setOf(
        "com.qq.e.ads.PortraitADActivity",
        "com.google.android.gms.ads.AdActivity",
        "com.bytedance.sdk.openadsdk.stub.activity.Stub_Standard_Portrait_Activity",
        "com.mbridge.msdk.reward.player.MBRewardVideoActivity",
    )

    override fun apply(ctx: HookContext) {
        ctx.hookMethod("android.app.Activity", "onResume") { chain ->
            val self = chain.thisObject
            if (self is Activity && self.javaClass.name in AD_ACTIVITIES) {
                try {
                    self.finish()
                } catch (t: Throwable) {
                    HookLog.w("ad activity finish failed", t)
                }
            }
            chain.proceed()
        }
        HookLog.i("AdBlocking installed for ${ctx.packageName}")
    }
}
