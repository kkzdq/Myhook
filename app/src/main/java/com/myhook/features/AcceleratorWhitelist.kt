package com.myhook.features

import com.myhook.core.ConfigKeys
import com.myhook.core.ConfigStore
import com.myhook.core.HookContext
import com.myhook.core.HookLog

/**
 * 加速器白名单 — enable this on each app you want the accelerator to proxy.
 *
 * Mechanism (from Fuck for VIP): inside a VPN/accelerator process, hook
 * `android.net.VpnService$Builder.establish()` and inject every whitelisted package with
 * `addAllowedApplication(pkg)` before the tunnel is built. The whitelist is exactly the set of
 * apps that have this toggle enabled.
 */
object AcceleratorWhitelist : Feature {
    override val key = ConfigKeys.ACCEL
    override val title = "加速器白名单"
    override val desc = "让加速器为这个 app 提供 VPN 代理（在加速器里也开启）"

    /** Accelerator / VPN apps that must never be re-proxied (verbatim from Fuck for VIP). */
    private val EXCLUDE = setOf(
        "com.richtech.spacevpn",
        "world.letsgo.booster.android.pro",
        "tool.seagull.v",
        "com.open.hotspot.vpn.free",
        "com.fat.vpn",
        "com.vpn.free.hotspot.secure.vpnify",
        "com.net.x2.app",
    )

    override fun apply(ctx: HookContext) {
        if (ctx.packageName in EXCLUDE) {
            HookLog.i("${ctx.packageName} in exclusion list, skip accelerator whitelist")
            return
        }
        val whitelist = ConfigStore.string(ConfigKeys.ACCEL_WHITELIST, "")
            .split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (whitelist.isEmpty()) return

        ctx.hookMethod("android.net.VpnService\$Builder", "establish") { chain ->
            val builder = chain.thisObject
            for (pkg in whitelist) {
                try {
                    builder.javaClass
                        .getMethod("addAllowedApplication", String::class.java)
                        .invoke(builder, pkg)
                } catch (t: Throwable) {
                    HookLog.w("addAllowedApplication($pkg) failed: $t")
                }
            }
            chain.proceed()
        }
    }
}
