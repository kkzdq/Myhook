package com.myhook.core

import android.content.Context

/**
 * Retrieves the [Context] of the process the module is currently injected into.
 *
 * The LibXposed API does not hand a Context to module callbacks, so we take it from
 * `Application.attach(Context)`'s argument (see [MyHookEntry]) and fall back to
 * `ActivityThread.currentApplication()`.
 *
 * Note the ordering trap: `Application.attach` runs *inside* `Instrumentation.newApplication`,
 * which `ActivityThread` calls **before** it assigns `mInitialApplication`. So at attach time
 * `currentApplication()` is still null — the argument is the only usable Context there.
 */
object AppContext {
    @Volatile
    private var cached: Context? = null

    /** Records a Context as soon as one is available (e.g. from `Application.attach`). */
    fun set(ctx: Context?) {
        if (ctx != null) cached = ctx
    }

    fun get(): Context? {
        cached?.let { return it }
        return try {
            val activityThread = Class.forName("android.app.ActivityThread")
            val app = activityThread.getMethod("currentApplication").invoke(null) as? Context
            if (app != null) cached = app
            app
        } catch (t: Throwable) {
            null
        }
    }
}
