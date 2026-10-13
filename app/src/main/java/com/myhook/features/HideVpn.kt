package com.myhook.features

import com.myhook.core.ConfigKeys
import com.myhook.core.ConfigStore
import com.myhook.core.HookContext

/**
 * Hide VPN. Ported from Fuck for VIP's hook_hide_vpn (the ConnectivityManager / NetworkCapabilities /
 * NetworkInfo family). Keeps the two checks that matter most and are safe to override.
 */
object HideVpn : Feature {
    override val key = ConfigKeys.HIDE_VPN_ENABLE
    override val title = "隐藏 VPN"
    override val desc = "NetworkCapabilities.hasTransport(VPN) 返回 false；NetworkInfo.getType 不返回 VPN"
    override val packages: Set<String> = emptySet()

    private const val TRANSPORT_VPN = 4
    private const val TYPE_VPN = 17
    private const val TYPE_WIFI = 1

    override fun apply(ctx: HookContext) {
        if (!ConfigStore.bool(key, false)) return

        ctx.hookMethod("android.net.NetworkCapabilities", "hasTransport", Int::class.java) { chain ->
            if ((chain.getArg(0) as? Int) == TRANSPORT_VPN) false else chain.proceed()
        }
        ctx.hookMethod("android.net.NetworkInfo", "getType") { chain ->
            val real = chain.proceed()
            if (real == TYPE_VPN) TYPE_WIFI else real
        }
    }
}
