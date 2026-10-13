package com.myhook.hooks

import com.myhook.core.HookContext

/**
 * AppShare (info.muge.appshare) — 移除开屏广告 / 解锁本地会员。
 *
 * Ported from HookVip `C0317` / actions `C1992(2)` + `C0223(4)`:
 *   info.muge.appshare.view.launch.LaunchBean 构造: args[0] = 1, args[1] = false
 *   info.muge.appshare.view.user.vip.beans.VipDetail#getVips() 后置:
 *       endTime = 113143670061000L, vipType = 2
 */
object AppShare : AppHook {
    override val packages = setOf("info.muge.appshare")

    private const val FOREVER = 113143670061000L

    override fun apply(ctx: HookContext) {
        ctx.hookAllConstructors("info.muge.appshare.view.launch.LaunchBean") { chain ->
            val args = chain.args.toMutableList()
            if (args.size >= 2) {
                args[0] = 1
                args[1] = false
                chain.proceed(args.toTypedArray())
            } else {
                chain.proceed()
            }
        }

        ctx.hookMethod(
            "info.muge.appshare.view.user.vip.beans.VipDetail",
            "getVips",
        ) { chain ->
            val r = chain.proceed()
            if (r != null) {
                ctx.setField(r, "endTime", FOREVER)
                ctx.setField(r, "vipType", 2)
            }
            r
        }
    }
}
