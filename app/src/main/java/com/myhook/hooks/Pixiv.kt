package com.myhook.hooks

import android.accounts.Account
import android.accounts.AccountManager
import com.myhook.core.HookContext

/**
 * pixiv (jp.pxv.android) — 解锁高级版。
 *
 * Hook:
 *   android.accounts.AccountManager#getAccountsByType("net.pixiv") 后置:
 *       对每个 account 调 setUserData(acc, "isPremium", "true")
 *   jp.pxv.android.domain.auth.entity.OAuthUser 构造后: isPremium = true
 */
object Pixiv : AppHook {
    override val packages = setOf("jp.pxv.android")

    override fun apply(ctx: HookContext) {
        ctx.hookMethod(
            "android.accounts.AccountManager",
            "getAccountsByType",
            String::class.java,
        ) { chain ->
            val result = chain.proceed()
            val type = chain.args.getOrNull(0) as? String
            if (type == "net.pixiv" && result is Array<*>) {
                val am = chain.getThisObject() as? AccountManager
                if (am != null) {
                    result.filterIsInstance<Account>().forEach { acc ->
                        runCatching { am.setUserData(acc, "isPremium", "true") }
                    }
                }
            }
            result
        }

        ctx.hookAllConstructors("jp.pxv.android.domain.auth.entity.OAuthUser") { chain ->
            val r = chain.proceed()
            ctx.setField(chain.getThisObject(), "isPremium", true)
            r
        }
    }
}
