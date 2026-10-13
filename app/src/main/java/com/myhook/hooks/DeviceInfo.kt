package com.myhook.hooks

import com.myhook.core.HookContext
import com.myhook.core.HookLog
import org.json.JSONObject

/**
 * 设备信息 (com.liuzh.deviceinfo) — 解锁 Pro。
 *
 * Hook:
 *   getString("ltoken")     -> "lovejiuwu"
 *   getString("user_info")  -> 伪造会员 JSON（uid=888888, vip.available=true, ...）
 *   getString("person_data")-> 在原有 JSON 上覆盖 name/photo/state
 *   getBoolean("is_pro_user")-> true
 */
object DeviceInfo : AppHook {
    override val packages = setOf("com.liuzh.deviceinfo")

    private const val FOREVER = 113143670061000L
    private const val AVATAR = "https://example.com/avatar.png"
    private const val NICK = "雾"

    override fun apply(ctx: HookContext) {
        val cls = ctx.findClass("android.app.SharedPreferencesImpl")
        if (cls == null) {
            HookLog.w("deviceinfo: SharedPreferencesImpl not found")
            return
        }

        runCatching {
            val m = cls.getDeclaredMethod("getString", String::class.java, String::class.java)
            m.isAccessible = true
            ctx.xposed.hook(m).intercept { chain ->
                val key = chain.args.getOrNull(0) as? String
                val orig = chain.proceed() as? String
                when (key) {
                    "ltoken" -> "lovejiuwu"
                    "user_info" -> fakeUserInfo()
                    "person_data" -> patchPerson(orig)
                    else -> orig
                }
            }
            HookLog.i("deviceinfo: hooked getString")
        }.onFailure { HookLog.w("deviceinfo getString hook failed: $it") }

        runCatching {
            val m = cls.getDeclaredMethod("getBoolean", String::class.java, Boolean::class.javaPrimitiveType)
            m.isAccessible = true
            ctx.xposed.hook(m).intercept { chain ->
                if ((chain.args.getOrNull(0) as? String) == "is_pro_user") true else chain.proceed()
            }
            HookLog.i("deviceinfo: hooked getBoolean")
        }.onFailure { HookLog.w("deviceinfo getBoolean hook failed: $it") }
    }

    private fun fakeUserInfo(): String {
        val wechat = JSONObject()
            .put("avatar", AVATAR)
            .put("nickname", NICK)
        val account = JSONObject().put("wechat", wechat)
        val vip = JSONObject()
            .put("available", true)
            .put("vip_expire", FOREVER)
        return JSONObject()
            .put("account", account)
            .put("avatar", AVATAR)
            .put("nickname", NICK)
            .put("register_time", System.currentTimeMillis())
            .put("uid", "888888")
            .put("vip", vip)
            .toString()
    }

    private fun patchPerson(orig: String?): String? {
        if (orig.isNullOrEmpty()) return orig
        return runCatching {
            JSONObject(orig)
                .put("name", NICK)
                .put("photo", AVATAR)
                .put("state", 3)
                .toString()
        }.getOrDefault(orig)
    }
}
