package com.myhook.core

import android.database.Cursor
import android.net.Uri
import android.os.SystemClock

/** Reads config from [ConfigProvider] with a 1s cache. Safe to call from any hooked process. */
object ConfigStore {
    private var cache: Map<String, Any?> = emptyMap()
    private var lastRead = 0L

    @Synchronized
    private fun ensureLoaded() {
        val now = SystemClock.elapsedRealtime()
        if (cache.isNotEmpty() && now - lastRead < 1000L) return
        val ctx = AppContext.get() ?: return
        try {
            val cursor = ctx.contentResolver.query(
                Uri.parse(ConfigKeys.CONFIG_URI), null, null, null, null
            ) ?: return
            cursor.use { c ->
                if (c.moveToFirst()) {
                    val map = HashMap<String, Any?>()
                    for (i in 0 until c.columnCount) {
                        val key = c.getColumnName(i)
                        map[key] = when (c.getType(i)) {
                            Cursor.FIELD_TYPE_INTEGER -> c.getLong(i)
                            Cursor.FIELD_TYPE_STRING -> c.getString(i)
                            Cursor.FIELD_TYPE_FLOAT -> c.getFloat(i)
                            else -> null
                        }
                    }
                    cache = map
                    lastRead = now
                }
            }
        } catch (t: Throwable) {
            HookLog.e("ConfigStore read failed", t)
        }
    }

    fun bool(key: String, def: Boolean): Boolean {
        ensureLoaded()
        return when (val v = cache[key]) {
            is Long -> v != 0L
            is Boolean -> v
            else -> def
        }
    }

    fun int(key: String, def: Int): Int {
        ensureLoaded()
        return (cache[key] as? Long)?.toInt() ?: def
    }

    fun string(key: String, def: String): String {
        ensureLoaded()
        return cache[key] as? String ?: def
    }
}
