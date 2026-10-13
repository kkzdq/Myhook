package com.myhook.core

import io.github.libxposed.api.XposedInterface
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.FindMethod
import org.luckypray.dexkit.query.enums.StringMatchType
import org.luckypray.dexkit.query.matchers.MethodMatcher

/**
 * Runtime dex search via DexKit.
 *
 * Some target apps fully obfuscate their class names, so there is nothing static to hook. Both
 * source modules (HookVip / Fuck-for-VIP) solve this by bundling DexKit and matching a method by
 * its **return type plus a string constant it uses**, then hooking whatever comes back. This helper
 * does the same thing in one place.
 *
 * The search runs on a background thread (a full-APK scan can take a second or two) and every
 * failure is logged and swallowed — a failed search must never take the target app down.
 */
object DexSearch {

    fun findAndHook(
        ctx: HookContext,
        returnType: String?,
        strings: List<String>,
        paramCount: Int? = null,
        matchType: StringMatchType = StringMatchType.Contains,
        hooker: (XposedInterface.Chain) -> Any?,
    ) {
        val tag = "dexkit[${strings.joinToString("|")}]"
        Thread {
            try {
                DexKitBridge.create(ctx.classLoader, false).use { bridge ->
                    val matcher = MethodMatcher.create()
                    if (returnType != null) matcher.returnType(returnType, matchType)
                    if (paramCount != null) matcher.paramCount(paramCount)
                    matcher.usingStrings(strings, matchType)

                    val hits = bridge.findMethod(FindMethod.create().matcher(matcher))
                    HookLog.i("$tag -> ${hits.size} hit(s)")
                    for (md in hits) {
                        try {
                            val m = md.getMethodInstance(ctx.classLoader)
                            m.isAccessible = true
                            ctx.xposed.hook(m).intercept { chain -> hooker(chain) }
                            HookLog.i("$tag hooked ${md.className}#${md.methodName}")
                        } catch (t: Throwable) {
                            HookLog.w("$tag hook ${md.className}#${md.methodName} failed: $t")
                        }
                    }
                }
            } catch (t: Throwable) {
                HookLog.e("$tag search failed", t)
            }
        }.apply { name = "myhook-dexkit" }.start()
    }
}
