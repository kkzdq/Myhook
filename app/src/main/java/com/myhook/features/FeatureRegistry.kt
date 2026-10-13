package com.myhook.features

/** Registry of extension features. Add new features here. */
object FeatureRegistry {

    private val features: List<Feature> = listOf(
        AcceleratorWhitelist,
        // TODO phase 2: JustTrustMe, hide root/su, hide developer mode, sensor faking,
        //                protocol downgrade, Toast interception (from HookVip + Fuck for VIP)
    )

    val all: List<Feature> get() = features

    fun forPackage(pkg: String): List<Feature> =
        features.filter { it.packages.isEmpty() || pkg in it.packages }
}
