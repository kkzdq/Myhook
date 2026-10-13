package com.myhook.hooks

import com.myhook.core.HookContext

/**
 * 百度网盘 — 去除开屏/回屏广告。
 * Hook: com.baidu.netdisk.advertise.splash.SplashManager.limitCountAdShow() -> true
 */
object BaiduNetdisk : AppHook {
    override val packages = setOf("com.baidu.netdisk")

    override fun apply(ctx: HookContext) {
        ctx.hookMethod("com.baidu.netdisk.advertise.splash.SplashManager", "limitCountAdShow") { true }
    }
}
