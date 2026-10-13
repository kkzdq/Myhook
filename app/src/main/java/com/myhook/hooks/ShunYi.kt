package com.myhook.hooks

import com.myhook.core.HookContext
import com.myhook.core.HookLog
import java.util.Date

/**
 * 瞬译 (com.spaceship.screen.textcopy) — 解锁高级版。
 *
 * Hook:
 *   构造 com.revenuecat.purchases.EntitlementInfos 时，把参数替换成：
 *     args[0] = { "premium" -> 伪造的 EntitlementInfo(expirationDate=永久,
 *                isActive=true, productIdentifier="", latestPurchaseDate=now) }
 *     args[1] = VerificationResult.VERIFIED
 *   （RevenueCat SDK 据此认为订阅有效。）
 */
object ShunYi : AppHook {
    override val packages = setOf("com.spaceship.screen.textcopy")

    private const val FOREVER = 113143670061000L

    override fun apply(ctx: HookContext) {
        ctx.hookAllConstructors("com.revenuecat.purchases.EntitlementInfos") { chain ->
            val infoCls = ctx.findClass("com.revenuecat.purchases.EntitlementInfo")
            val vCls = ctx.findClass("com.revenuecat.purchases.VerificationResult")
            if (infoCls == null || vCls == null) return@hookAllConstructors chain.proceed()

            val info = allocate(infoCls)
            if (info != null) {
                ctx.setField(info, "expirationDate", Date(FOREVER))
                ctx.setField(info, "isActive", true)
                ctx.setField(info, "productIdentifier", "")
                ctx.setField(info, "latestPurchaseDate", Date())
            }
            val verified = vCls.enumConstants?.firstOrNull { it.toString() == "VERIFIED" }
            val map = java.util.Collections.singletonMap("premium", info)
            chain.proceed(arrayOf<Any?>(map, verified))
        }
    }

    /** Instantiate without a public no-arg constructor: try the ctor, then `Unsafe`. */
    private fun allocate(cls: Class<*>): Any? {
        runCatching {
            val c = cls.getDeclaredConstructor()
            c.isAccessible = true
            return c.newInstance()
        }
        return runCatching {
            val u = Class.forName("sun.misc.Unsafe")
            val f = u.getDeclaredField("theUnsafe")
            f.isAccessible = true
            val unsafe = f.get(null)
            u.getMethod("allocateInstance", Class::class.java).invoke(unsafe, cls)
        }.onFailure { HookLog.w("shunyi allocate failed: $it") }.getOrNull()
    }
}
