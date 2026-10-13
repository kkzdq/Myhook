package com.myhook.features

import com.myhook.core.HookContext

/**
 * An extension feature ("拓展功能") — a toggle that installs system-level hooks in the target
 * process rather than a per-app VIP unlock.
 */
interface Feature {
    /** Config key that toggles this feature. */
    val key: String

    /** Packages to install into. Empty set = every package in the module scope. */
    val packages: Set<String>

    fun apply(ctx: HookContext)
}
