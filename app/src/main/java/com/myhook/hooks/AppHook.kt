package com.myhook.hooks

import com.myhook.core.HookContext

/**
 * A hook targeting one or more application packages. Implementations are registered in
 * [AppRegistry] and invoked once per matching process.
 */
interface AppHook {
    /** Package names this hook applies to. */
    val packages: Set<String>

    /** Installs the hooks. Called with the target package's own ClassLoader. */
    fun apply(ctx: HookContext)
}
