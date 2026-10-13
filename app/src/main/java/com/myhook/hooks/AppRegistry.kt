package com.myhook.hooks

import com.myhook.features.Feature
import com.myhook.features.FeatureRegistry

/** Central registry of every hook. Add new app hooks here. */
object AppRegistry {

    private val hooks: List<AppHook> = listOf(
        OppoHealthSteps,
        DuolingoTimezone,
        DuolingoVip,
        NeteaseMusic,
        BiliDkplat,
        BaiduInput,
        BaiduNetdisk,
        MobileTicket,
        Pixiv,
        AppShare,
        Cainiao,
        Etalien,
        Doubao,
        Thanox,
        EsExplorer,
        ShunYi,
        DeviceInfo,
        Wps,
        AliCloud,
        Douban,
        AdbHelper,
        Pydroid,
    )

    fun hooksFor(pkg: String): List<AppHook> = hooks.filter { pkg in it.packages }

    fun featuresFor(pkg: String): List<Feature> = FeatureRegistry.enabledFor(pkg)

    /** True when at least one real hook targets [pkg] (i.e. the app is actually supported). */
    fun hasHook(pkg: String): Boolean = hooks.any { pkg in it.packages }

    /** How many hook classes target [pkg]. */
    fun hookCountFor(pkg: String): Int = hooks.count { pkg in it.packages }

    /** Every package that currently has an implemented hook. */
    fun supportedPackages(): Set<String> = hooks.flatMapTo(HashSet()) { it.packages }
}
