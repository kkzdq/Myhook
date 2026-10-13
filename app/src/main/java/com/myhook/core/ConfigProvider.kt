package com.myhook.core

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

/**
 * Config store for hooked processes. Deliberately a plain ContentProvider instead of
 * XSharedPreferences: XSharedPreferences relies on world-readable preference files, which is
 * exactly the deprecated mechanism LSPosed flags with
 * "This module uses a deprecated feature that will be removed soon".
 */
class ConfigProvider : ContentProvider() {

    private fun prefs(): android.content.SharedPreferences =
        requireNotNull(context).getSharedPreferences(ConfigKeys.PREFS, Context.MODE_PRIVATE)

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        val all = prefs().all
        val cols = projection ?: all.keys.toTypedArray()
        val cursor = MatrixCursor(cols)
        val row = arrayOfNulls<Any?>(cols.size)
        cols.forEachIndexed { i, key -> row[i] = all[key] }
        cursor.addRow(row)
        return cursor
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int {
        val editor = prefs().edit()
        values?.keySet()?.forEach { key ->
            when (val v = values.get(key)) {
                is Int -> editor.putInt(key, v)
                is Long -> editor.putLong(key, v)
                is Boolean -> editor.putBoolean(key, v)
                is String -> editor.putString(key, v)
                else -> editor.remove(key)
            }
        }
        editor.apply()
        return 1
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        update(uri, values, null, null)
        return uri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun getType(uri: Uri): String? = null
}
