package com.myhook.hooks

import android.util.Base64
import com.myhook.core.HookContext
import com.myhook.core.HookLog
import org.json.JSONObject
import java.lang.reflect.Method
import javax.crypto.Cipher
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.SecretKeyFactory

/**
 * WPS Office (cn.wps.moffice_eng) — 解锁 Pro。
 *
 * Ported from HookVip `C3521` / provider-hook `C3238` (`AbstractC0930.m2482`).
 * The privilege blob lives in `wps_privilege.xml` as a DES-CBC encrypted, base64-wrapped JSON.
 * We hook `SharedPreferencesImpl`:
 *   getString  @ wps_privilege.xml -> 解密 -> 打补丁(expire_time=永久, is_available=1, result=ok) -> 重新加密
 *   getBoolean @ 指定 key           -> true
 *
 * Crypto (from `AbstractC1420`):
 *   key = DESKeySpec(前8字节 of nibble-decode("4enJian6ouwEishi1inghuan"))
 *   iv  = {0x61,0xE1,0x18,0xFD,0x58,0x1A,0xB2,0x36}
 *   Base64 flag = NO_WRAP(2)
 */
object Wps : AppHook {
    override val packages = setOf("cn.wps.moffice_eng")

    private const val KEY_STR = "4enJian6ouwEishi1inghuan"
    private const val PRIV_FILE = "wps_privilege.xml"
    private const val FOREVER = 113143670061000L

    private val IV = byteArrayOf(
        0x61, 0xE1.toByte(), 0x18, 0xFD.toByte(),
        0x58, 0x1A, 0xB2.toByte(), 0x36,
    )

    private val BOOL_TRUE_KEYS = setOf(
        "user_account_monthly",
        "vip_user",
        "vm_layout_preview_mode",
        "vm_layout_preview_mode_feature_displayed",
    )

    override fun apply(ctx: HookContext) {
        val cls = ctx.findClass("android.app.SharedPreferencesImpl")
        if (cls == null) {
            HookLog.w("wps: SharedPreferencesImpl not found")
            return
        }

        runCatching {
            val m = cls.getDeclaredMethod("getString", String::class.java, String::class.java)
            m.isAccessible = true
            ctx.xposed.hook(m).intercept { chain ->
                val orig = chain.proceed() as? String
                if (prefFileOf(chain) == PRIV_FILE) {
                    runCatching { fakePrivilege(orig) }
                        .onFailure { HookLog.w("wps fakePrivilege failed: $it") }
                        .getOrDefault(orig)
                } else {
                    orig
                }
            }
            HookLog.i("wps: hooked getString")
        }.onFailure { HookLog.w("wps getString hook failed: $it") }

        runCatching {
            val m: Method = cls.getDeclaredMethod(
                "getBoolean", String::class.java, Boolean::class.javaPrimitiveType,
            )
            m.isAccessible = true
            ctx.xposed.hook(m).intercept { chain ->
                val key = chain.args.getOrNull(0) as? String
                if (key != null && key in BOOL_TRUE_KEYS) true else chain.proceed()
            }
            HookLog.i("wps: hooked getBoolean")
        }.onFailure { HookLog.w("wps getBoolean hook failed: $it") }
    }

    /** Reads the `mFile` field of SharedPreferencesImpl and returns its file name. */
    private fun prefFileOf(chain: io.github.libxposed.api.XposedInterface.Chain): String? =
        runCatching {
            val obj = chain.getThisObject() ?: return null
            var c: Class<*>? = obj.javaClass
            while (c != null) {
                try {
                    val f = c.getDeclaredField("mFile")
                    f.isAccessible = true
                    return (f.get(obj) as? java.io.File)?.name
                } catch (_: NoSuchFieldException) {
                    c = c.superclass
                }
            }
            null
        }.getOrNull()

    private fun desKey(): ByteArray {
        val out = ByteArray(KEY_STR.length / 2)
        var i = 0
        var j = 0
        while (i < out.size) {
            out[i] = (nib(KEY_STR[j + 1]) or (nib(KEY_STR[j]) shl 4)).toByte()
            i++
            j += 2
        }
        return out
    }

    private fun nib(c: Char): Int {
        val v = when {
            c >= 'a' -> (c - 'a') + 10
            c >= 'A' -> (c - 'A') + 10
            else -> c - '0'
        }
        return v and 15
    }

    private fun cipher(mode: Int): Cipher {
        val key = SecretKeyFactory.getInstance("DES").generateSecret(DESKeySpec(desKey()))
        val c = Cipher.getInstance("DES/CBC/PKCS5Padding")
        c.init(mode, key, IvParameterSpec(IV))
        return c
    }

    private fun encrypt(plain: String): String =
        Base64.encodeToString(cipher(Cipher.ENCRYPT_MODE).doFinal(plain.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)

    private fun decrypt(b64: String): String =
        String(cipher(Cipher.DECRYPT_MODE).doFinal(Base64.decode(b64, Base64.NO_WRAP)), Charsets.UTF_8)

    private fun fakePrivilege(orig: String?): String {
        val now = System.currentTimeMillis() / 1000
        if (!orig.isNullOrEmpty()) {
            val json = JSONObject(decrypt(orig))
            json.put("result", "ok")
            val data = json.optJSONObject("data") ?: JSONObject()
            data.put("expire_time", FOREVER / 1000)
            data.put("now", now)
            data.put("is_available", 1)
            json.put("data", data)
            json.put("download_time", now)
            return encrypt(json.toString())
        }
        val data = JSONObject()
            .put("credit_margin", 0L)
            .put("credit_total", 0L)
            .put("credit_used", 0L)
            .put("expire_time", FOREVER / 1000)
            .put("fake_service_id", 0L)
            .put("is_available", 1L)
            .put("now", now)
            .put("page", 0L)
            .put("permit_type_id", 223L)
            .put("service_id", 223L)
            .put("uid", 0L)
            .put("update_time", 0L)
        val root = JSONObject()
            .put("data", data)
            .put("download_time", now)
            .put("result", "ok")
        return encrypt(root.toString())
    }
}
