package com.myhook.features

import com.myhook.core.ConfigStore

/** Registry of extension features. Add new features here. */
object FeatureRegistry {

    /** The six per-app extension features. */
    val all: List<Feature> = listOf(
        AcceleratorWhitelist,
        DisableSensor,
        BlankPass,
        DialogBlocking,
        ControlInterception,
        AdBlocking,
    )

    /** Features that the user enabled for [pkg]. */
    fun enabledFor(pkg: String): List<Feature> =
        all.filter { ConfigStore.bool(it.enabledKey(pkg), false) }
}
