package com.myhook

import android.content.Context
import com.myhook.core.AppContext
import com.myhook.core.HookContext
import com.myhook.core.HookLog
import com.myhook.hooks.AppRegistry
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicBoolean

/**
 * LibXposed API entry point (see META-INF/xposed/java_init.list).
 *
 * Declared with minApiVersion=101 / targetApiVersion=101 / staticScope=false so:
 *  - the framework does NOT flag it with the "deprecated feature" warning (that only fires for
 *    modules still on the legacy de.robv.android.xposed API with world-readable prefs), and
 *  - users can add apps to the scope at runtime from the in-app picker.
 *
 * **Why hooks are installed lazily.** [onPackageLoaded] fires while the process is still binding —
 * the `Application` object does not exist yet, so `ActivityThread.currentApplication()` returns
 * null and the config store cannot be read. Reading it there yields `master = <default>` for every
 * app and silently skips everything. Both source modules work around this by hooking
 * `Application.attach(Context)`; we do the same and install exactly once.
 */
class MyHookEntry : XposedModule() {

    override fun onModuleLoaded(param: XposedModuleInterface.ModuleLoadedParam) {
        HookLog.i("MyHook loaded: process=${param.processName} systemServer=${param.isSystemServer}")
    }

    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        val pkg = param.packageName
        val classLoader = param.defaultClassLoader
        val ctx = HookContext(this, classLoader, pkg)

        val installed = AtomicBoolean(false)
        val install: () -> Unit = {
            if (installed.compareAndSet(false, true)) {
                try {
                    installAll(ctx)
                } catch (t: Throwable) {
                    HookLog.e("MyHook install failed for $pkg", t)
                }
            }
        }

        var hooked = 0
        for (m in entryPoints(classLoader)) {
            val isAttach = m.name == "attach"
            try {
                m.isAccessible = true
                hook(m).intercept { chain ->
                    val r = chain.proceed()
                    if (isAttach) {
                        // `attach` runs inside Instrumentation.newApplication, before ActivityThread
                        // assigns mInitialApplication — so currentApplication() is still null here.
                        // The argument is the only Context available this early.
                        AppContext.set(runCatching { chain.getArg(0) as? Context }.getOrNull())
                    }
                    install()
                    r
                }
                hooked++
            } catch (t: Throwable) {
                HookLog.w("MyHook: cannot hook Application#${m.name}: $t")
            }
        }
        if (hooked == 0) HookLog.w("MyHook: no Application entry point hooked for $pkg")
    }

    /**
     * `Application.attach(Context)` (earliest, before the app's own `onCreate`) plus
     * `Application.onCreate()` as a fallback for ROMs whose `attach` signature differs.
     */
    private fun entryPoints(classLoader: ClassLoader): List<Method> {
        val out = ArrayList<Method>(2)
        val appCls = try {
            Class.forName("android.app.Application", false, classLoader)
        } catch (t: Throwable) {
            return out
        }
        try {
            out.add(appCls.getDeclaredMethod("attach", Context::class.java))
        } catch (_: Throwable) {
            appCls.declaredMethods
                .filter { it.name == "attach" && it.parameterTypes.firstOrNull() == Context::class.java }
                .forEach { out.add(it) }
        }
        try {
            out.add(appCls.getDeclaredMethod("onCreate"))
        } catch (_: Throwable) {
            // ignore — attach alone is enough
        }
        return out
    }

    private fun installAll(ctx: HookContext) {
        val pkg = ctx.packageName
        val hooks = AppRegistry.hooksFor(pkg)
        val features = AppRegistry.featuresFor(pkg)
        HookLog.i("MyHook: $pkg -> ${hooks.size} app hook(s), ${features.size} feature(s)")
        for (hook in hooks) {
            try {
                hook.apply(ctx)
            } catch (t: Throwable) {
                HookLog.e("app hook for $pkg failed", t)
            }
        }
        for (feature in features) {
            try {
                feature.apply(ctx)
            } catch (t: Throwable) {
                HookLog.e("feature ${feature.key} in $pkg failed", t)
            }
        }
    }
}
