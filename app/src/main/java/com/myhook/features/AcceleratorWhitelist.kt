package com.myhook.features

import com.myhook.core.ConfigKeys
import com.myhook.core.ConfigStore
import com.myhook.core.HookContext
import com.myhook.core.HookLog

/**
 * Accelerator / VPN proxy whitelist.
 *
 * Implementation is taken from Fuck for VIP (the more complete one), NOT HookVip:
 *  - HookVip simply adds *every* installed app to the VPN (getInstalledApplications -> addAllowedApplication).
 *  - Fuck for VIP uses a *user-configurable* whitelist, skips a fixed exclusion list of accelerator/VPN
 *    apps, and guards each add with its own try/catch.
 *
 * Mechanism: inside the accelerator process, hook `android.net.VpnService$Builder.establish()` and
 * inject each whitelisted package with `addAllowedApplication(pkg)` right before the tunnel is built.
 */
object AcceleratorWhitelist : Feature {

    override val key = ConfigKeys.ACCEL_ENABLE

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

    override val packages: Set<String> = emptySet()

    override fun apply(ctx: HookContext) {
        if (!ConfigStore.bool(key, false)) return
        if (ctx.packageName in EXCLUDE) {
            HookLog.i("${ctx.packageName} in exclusion list, skip accelerator whitelist")
            return
        }
        val whitelist = ConfigStore.string(ConfigKeys.ACCEL_WHITELIST, "")
            .split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (whitelist.isEmpty()) {
            HookLog.i("accelerator whitelist empty, nothing to proxy")
            return
        }
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
