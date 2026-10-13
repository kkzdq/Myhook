package com.myhook.hooks

import com.myhook.core.HookContext

/**
 * 铁路12306 (com.MobileTicket) — 去除开屏广告。
 *
 * Hook:
 *   com.MobileTicket.utils.AdUtils#splashNeedShowAgain()          -> false
 *   com.MobileTicket.utils.HomePageUtils$Companion#getCAN_SHOW_SPLASH() -> false
 *   com.MobileTicket.ads.bean.BeanAds#haveSplashAd()              -> false
 *   com.MobileTicket.ads.bean.BeanAds#getImgUrl()                 -> ""
 */
object MobileTicket : AppHook {
    override val packages = setOf("com.MobileTicket")

    override fun apply(ctx: HookContext) {
        ctx.hookMethod("com.MobileTicket.utils.AdUtils", "splashNeedShowAgain") { false }
        ctx.hookMethod(
            "com.MobileTicket.utils.HomePageUtils\$Companion",
            "getCAN_SHOW_SPLASH",
        ) { false }
        ctx.hookMethod("com.MobileTicket.ads.bean.BeanAds", "haveSplashAd") { false }
        ctx.hookMethod("com.MobileTicket.ads.bean.BeanAds", "getImgUrl") { "" }
    }
}
