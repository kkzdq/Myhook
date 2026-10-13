package com.myhook.features

import android.database.MatrixCursor
import android.net.Uri
import com.myhook.core.ConfigKeys
import com.myhook.core.HookContext
import com.myhook.core.HookLog

/**
 * 空白通行证 — ported from Fuck for VIP's `hook_contact` (displayed as "blank_pass").
 * Makes contact/privacy queries return an empty cursor, so the app thinks it got a result while
 * seeing nothing.
 */
object BlankPass : Feature {
    override val key = ConfigKeys.FEATURE_BLANK_PASS
    override val title = "空白通行证"
    override val desc = "联系人等隐私查询返回空白（拿到结果但看不到内容）"

    private val BLANK_AUTHORITIES = listOf("contacts", "com.android.providers.contacts", "calllog")

    override fun apply(ctx: HookContext) {
        // ContentResolver.query(Uri, String[], String, String[], String)
        ctx.hookMethod(
            "android.content.ContentResolver", "query",
            Uri::class.java, Array<String>::class.java, String::class.java,
            Array<String>::class.java, String::class.java,
        ) { chain ->
            val uri = chain.getArg(0) as? Uri
            if (uri != null && BLANK_AUTHORITIES.any { uri.toString().contains(it) }) {
                val projection = chain.getArg(1) as? Array<*>
                MatrixCursor(projection?.map { it?.toString() ?: "_id" }?.toTypedArray() ?: arrayOf("_id"))
            } else {
                chain.proceed()
            }
        }
        HookLog.i("BlankPass installed for ${ctx.packageName}")
    }
}
