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

    /**
     * Read-only on purpose. The provider must stay `exported` because hooked processes run with the
     * **target app's** UID and have to be able to read the config — but nothing outside this module
     * has any business writing it. The UI writes the preferences directly, so the write path was
     * never used; keeping it open would just let any installed app rewrite MyHook's config.
     */
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int {
        android.util.Log.w(HookLog.TAG, "ConfigProvider is read-only; ignoring update from uid=${android.os.Process.myUid()}")
        return 0
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun getType(uri: Uri): String? = null
}
