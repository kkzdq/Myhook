package com.myhook.hooks

import com.myhook.core.HookContext
import org.json.JSONObject

/**
 * ES文件浏览器 (com.estrongs.android.pop) — 解锁会员。
 *
 * Hook:
 *   okhttp3.ResponseBody#string() 后置: 若响应体是 JSON 且 result 里有 isVip，
 *   整个响应体替换成伪造的会员 JSON（ret/result/serverTime）。
 */
object EsExplorer : AppHook {
    override val packages = setOf("com.estrongs.android.pop")

    private const val FOREVER = 113143670061000L
    private const val AVATAR = "https://example.com/avatar.png"
    private const val NICK = "雾"

    override fun apply(ctx: HookContext) {
        ctx.hookMethod("okhttp3.ResponseBody", "string") { chain ->
            val r = chain.proceed()
            if (r !is String) return@hookMethod r
            val patched = runCatching {
                val j = JSONObject(r)
                val res = j.optJSONObject("result")
                if (res != null && res.has("isVip")) fakeVip() else null
            }.getOrNull()
            patched ?: r
        }
    }

    private fun fakeVip(): String {
        val mail = JSONObject()
            .put("name", NICK)
            .put("picture", AVATAR)
            .put("authType", 1)
        val result = JSONObject()
            .put("isVip", true)
            .put("vipFinishAt", FOREVER)
            .put("headImgUrl", AVATAR)
            .put("nickName", "")
            .put("mail", mail)
        return JSONObject()
            .put("ret", 200)
            .put("result", result)
            .put("serverTime", System.currentTimeMillis())
            .toString()
    }
}
