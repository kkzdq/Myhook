package com.myhook.core

import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Per-process hook helper. Wraps the LibXposed [XposedInterface] with a couple of convenience
 * helpers so individual hooks stay short.
 *
 * Note on the LibXposed API: [XposedInterface.Chain.getArgs] returns an **immutable** list, so
 * changing arguments means building a new array and calling `chain.proceed(newArgs)`.
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

    /** Hooks every declared method of [className] matching [filter]. Returns the hit count. */
    fun hookAllMethods(
        className: String,
        filter: (Method) -> Boolean,
        hooker: (XposedInterface.Chain) -> Any?,
    ): Int {
        val cls = findClass(className) ?: return 0
        var n = 0
        for (m in cls.declaredMethods) {
            if (!filter(m)) continue
            try {
                m.isAccessible = true
                xposed.hook(m).intercept { chain -> hooker(chain) }
                n++
            } catch (t: Throwable) {
                HookLog.w("hook $className#${m.name} failed: $t")
            }
        }
        if (n > 0) HookLog.i("hooked $className ($n methods)")
        return n
    }

    /** Hooks every declared constructor of [className]. Returns the hit count. */
    fun hookAllConstructors(
        className: String,
        hooker: (XposedInterface.Chain) -> Any?,
    ): Int {
        val cls = findClass(className) ?: return 0
        var n = 0
        for (c in cls.declaredConstructors) {
            try {
                c.isAccessible = true
                xposed.hook(c).intercept { chain -> hooker(chain) }
                n++
            } catch (t: Throwable) {
                HookLog.w("hook ctor $className failed: $t")
            }
        }
        if (n > 0) HookLog.i("hooked ctor $className ($n)")
        return n
    }

    /** Sets a (possibly inherited, private) instance field. */
    fun setField(obj: Any?, name: String, value: Any?): Boolean {
        if (obj == null) return false
        val f = findField(obj.javaClass, name) ?: return false
        return try {
            f.isAccessible = true
            f.set(obj, value)
            true
        } catch (t: Throwable) {
            false
        }
    }

    fun getField(obj: Any?, name: String): Any? {
        if (obj == null) return null
        val f = findField(obj.javaClass, name) ?: return null
        return try {
            f.isAccessible = true
            f.get(obj)
        } catch (t: Throwable) {
            null
        }
    }

    private fun findField(cls: Class<*>, name: String): Field? {
        var c: Class<*>? = cls
        while (c != null) {
            try {
                return c.getDeclaredField(name)
            } catch (_: NoSuchFieldException) {
                c = c.superclass
            }
        }
        return null
    }
}
