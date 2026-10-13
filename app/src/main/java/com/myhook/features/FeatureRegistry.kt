package com.myhook.features

/** Registry of extension features. Add new features here. */
object FeatureRegistry {

    private val features: List<Feature> = listOf(
        AcceleratorWhitelist,
        HideRootSu,
        HideDeveloperSettings,
        HideVpn,
        FlagSecure,
        // TODO phase 2: JustTrustMe, HTTP Canary pass, protocol downgrade, sensor faking,
        //                Toast interception, clipboard/contact blocking, device spoof,
        //                mock location, dialog blocking, ad blocking (from HookVip + FFV)
    )

    val all: List<Feature> get() = features

    fun forPackage(pkg: String): List<Feature> =
        features.filter { it.packages.isEmpty() || pkg in it.packages }
}
