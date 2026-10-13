package com.myhook

import com.myhook.core.HookContext
import com.myhook.core.HookLog
import com.myhook.hooks.AppRegistry
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

/**
 * LibXposed API entry point (see META-INF/xposed/java_init.list).
 *
 * Declared with minApiVersion=101 / targetApiVersion=101 / staticScope=false so:
 *  - the framework does NOT flag it with the "deprecated feature" warning (that only fires for
 *    modules still on the legacy de.robv.android.xposed API with world-readable prefs), and
 *  - users can add apps to the scope at runtime from the in-app picker.
 */
class MyHookEntry : XposedModule() {

    override fun onModuleLoaded(param: XposedModuleInterface.ModuleLoadedParam) {
        HookLog.i("MyHook loaded: process=${param.processName} systemServer=${param.isSystemServer}")
    }

    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        val pkg = param.packageName
        val classLoader = param.defaultClassLoader
        val ctx = HookContext(this, classLoader, pkg)

        for (hook in AppRegistry.hooksFor(pkg)) {
            try {
                hook.apply(ctx)
            } catch (t: Throwable) {
                HookLog.e("app hook for $pkg failed", t)
            }
        }
        for (feature in AppRegistry.featuresFor(pkg)) {
            try {
                feature.apply(ctx)
            } catch (t: Throwable) {
                HookLog.e("feature ${feature.key} in $pkg failed", t)
            }
        }
    }
}
