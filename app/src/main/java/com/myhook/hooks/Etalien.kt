package com.myhook.hooks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.myhook.core.HookContext
import com.myhook.core.HookLog

/**
 * 外星仔加速器 (com.etalien.booster) — 去除开屏广告（免广告领时长）。
 *
 * Ported from HookVip `C1212` (plaintext part) / action `C0826(7)`:
 *   com.zero.flutter_gromore_ads.page.AdSplashActivity#onCreate(Bundle) 后置:
 *       发送本地广播 BR_AdSplashLoadActivity（通知开屏已加载，跳过等待广告）
 */
object Etalien : AppHook {
    override val packages = setOf("com.etalien.booster")

    override fun apply(ctx: HookContext) {
        ctx.hookMethod(
            "com.zero.flutter_gromore_ads.page.AdSplashActivity",
            "onCreate", Bundle::class.java,
        ) { chain ->
            val r = chain.proceed()
            runCatching {
                val lbm = Class.forName(
                    "androidx.localbroadcastmanager.content.LocalBroadcastManager",
                    false, ctx.classLoader,
                )
                val inst = lbm
                    .getMethod("getInstance", Context::class.java)
                    .invoke(null, chain.getThisObject())
                lbm.getMethod("sendBroadcast", Intent::class.java)
                    .invoke(inst, Intent("BR_AdSplashLoadActivity"))
            }.onFailure { HookLog.w("etalien broadcast failed: $it") }
            r
        }
    }
}
