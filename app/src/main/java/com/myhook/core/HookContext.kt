package com.myhook.core

import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Method

/**
 * Per-process hook helper. Wraps the LibXposed [XposedInterface] with a couple of convenience
 * helpers so individual hooks stay short.
 */
class HookContext(
    val xposed: XposedInterface,
    val classLoader: ClassLoader,
    val packageName: String,
) {
    fun findClass(name: String): Class<*>? = try {
        Class.forName(name, false, classLoader)
    } catch (t: Throwable) {
        null
    }

    /** Hooks [className]#[methodName] and routes every call through [hooker]. */
    fun hookMethod(
        className: String,
        methodName: String,
        vararg paramTypes: Class<*>,
        hooker: (XposedInterface.Chain) -> Any?,
    ): Boolean {
        val cls = findClass(className) ?: return false
        return try {
            val m: Method = cls.getDeclaredMethod(methodName, *paramTypes)
            m.isAccessible = true
            xposed.hook(m).intercept { chain -> hooker(chain) }
            HookLog.i("hooked $className#$methodName")
            true
        } catch (t: Throwable) {
            HookLog.w("hook $className#$methodName failed: $t")
            false
        }
    }
}
