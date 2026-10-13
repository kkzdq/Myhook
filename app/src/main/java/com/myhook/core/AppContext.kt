package com.myhook.core

import android.content.Context

/**
 * Retrieves the [Context] of the process the module is currently injected into.
 * The new LibXposed API does not hand a Context to module callbacks, so we read it from
 * ActivityThread the same way most modules do.
 */
object AppContext {
    @Volatile
    private var cached: Context? = null

    fun get(): Context? {
        cached?.let { return it }
        return try {
            val activityThread = Class.forName("android.app.ActivityThread")
            val app = activityThread.getMethod("currentApplication").invoke(null) as? Context
            cached = app
            app
        } catch (t: Throwable) {
            null
        }
    }
}
