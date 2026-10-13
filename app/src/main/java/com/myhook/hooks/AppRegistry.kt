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
    )

    fun hooksFor(pkg: String): List<AppHook> = hooks.filter { pkg in it.packages }

    fun featuresFor(pkg: String): List<Feature> = FeatureRegistry.enabledFor(pkg)
}
