package com.myhook.core

import android.util.Log

/**
 * Thin logging wrapper so every message carries the MyHook tag.
 *
 * Everything goes to **logcat only** (`adb logcat -s MyHook`) — the module never writes a log file.
 * Whether anything is emitted at all is controlled by the "详细日志" switch in the UI
 * ([ConfigKeys.DEBUG_LOG]); it defaults to on. Errors are always emitted so a broken config read
 * is still visible.
 *
 * Reads the flag through [ConfigStore], which deliberately logs its own failures with
 * `android.util.Log` directly so this call cannot recurse.
 */
object HookLog {
    const val TAG = "MyHook"

    private fun enabled(): Boolean = try {
        ConfigStore.bool(ConfigKeys.DEBUG_LOG, true)
    } catch (t: Throwable) {
        true
    }

    fun i(msg: String) {
        if (enabled()) Log.i(TAG, msg)
    }

    fun w(msg: String, t: Throwable? = null) {
        if (enabled()) Log.w(TAG, msg, t)
    }

    /** Errors bypass the switch — a silent failure is worse than a noisy one. */
    fun e(msg: String, t: Throwable? = null) {
        Log.e(TAG, msg, t)
    }
}
