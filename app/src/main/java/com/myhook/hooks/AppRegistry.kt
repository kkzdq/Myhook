package com.myhook.hooks

import com.myhook.features.AcceleratorWhitelist
import com.myhook.features.Feature
import com.myhook.features.FeatureRegistry

/** Central registry of every hook and feature. Add new modules here. */
object AppRegistry {

    private val hooks: List<AppHook> = listOf(
        OppoHealthSteps,
        DuolingoTimezone,
        // TODO phase 2: port HookVip's per-app hooks (com.netease.cloudmusic, tv.danmaku.bili, ...)
    )

    fun hooksFor(pkg: String): List<AppHook> = hooks.filter { pkg in it.packages }

    fun featuresFor(pkg: String): List<Feature> = FeatureRegistry.forPackage(pkg)
}
