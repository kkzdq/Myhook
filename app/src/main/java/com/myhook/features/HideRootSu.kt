package com.myhook.features

import com.myhook.core.ConfigKeys
import com.myhook.core.ConfigStore
import com.myhook.core.HookContext
import java.io.File

/**
 * Hide root / su. Logic ported from HookVip (C3114.m6012 / m6013) — the cleaner of the two:
 *  - File.exists() returns false for the well-known su / Superuser paths
 *  - Runtime.exec() renames a bare "su" argument so shell-based root checks fail
 */
object HideRootSu : Feature {
    override val key = ConfigKeys.HIDE_ROOT_ENABLE
    override val title = "隐藏 Root / su"
    override val desc = "su 路径 File.exists 返回 false；Runtime.exec 里的 su 改名"
    override val packages: Set<String> = emptySet()

    private val SU_PATHS = setOf(
        "/system/bin/su", "/system/xbin/su", "/sbin/su", "/system/sbin/su",
        "/system/app/Superuser.apk", "/system/app/SuperSU/SuperSU.apk",
        "/system/bin/failsafe/su", "/su/bin/su", "/magisk/.core/bin/su",
        "/system/xbin/daemonsu", "/system/etc/init.d/99SuperSUDaemon",
    )

    override fun apply(ctx: HookContext) {
        if (!ConfigStore.bool(key, false)) return

        ctx.hookMethod("java.io.File", "exists") { chain ->
            val f = chain.thisObject
            if (f is File && f.absolutePath in SU_PATHS) false else chain.proceed()
        }

        ctx.hookMethod("java.lang.Runtime", "exec", String::class.java) { chain ->
            val cmd = chain.getArg(0) as? String
            if (cmd == null) {
                chain.proceed()
            } else {
                val renamed = cmd.split(" ").joinToString(" ") {
                    if (it == "su" || it.endsWith("/su")) "cnmSu" else it
                }
                chain.proceed(arrayOf<Any?>(renamed))
            }
        }
    }
}
