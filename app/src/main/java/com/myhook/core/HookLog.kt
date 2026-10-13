package com.myhook.core

import android.util.Log

/** Thin logging wrapper so every message carries the MyHook tag. */
object HookLog {
    const val TAG = "MyHook"

    fun i(msg: String) {
        Log.i(TAG, msg)
    }

    fun w(msg: String, t: Throwable? = null) {
        Log.w(TAG, msg, t)
    }

    fun e(msg: String, t: Throwable? = null) {
        Log.e(TAG, msg, t)
    }
}
