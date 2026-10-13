package com.myhook.features

import com.myhook.core.HookContext

/**
 * An extension feature ("拓展功能"). Every feature is configured **per app**: the user taps an app
 * in the list and toggles the features that apply to it.
 */
interface Feature {
    /** Stable id, also used to build the per-app config key. */
    val key: String

    /** UI title. */
    val title: String

    /** UI description. */
    val desc: String

    /** Config key that stores "enabled for [pkg]". */
    fun enabledKey(pkg: String): String = "f_${key}_$pkg"

    fun apply(ctx: HookContext)
}
