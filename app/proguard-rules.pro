# --- LibXposed module entry -------------------------------------------------------------------
# The framework loads the module class by the exact name listed in META-INF/xposed/java_init.list
# (com.myhook.MyHookEntry), so it must NOT be renamed or removed.
-keep class com.myhook.MyHookEntry { *; }
-keep public class * extends io.github.libxposed.api.XposedModule { public <init>(); }

# io.github.libxposed:api is compileOnly (the framework provides it at runtime) — do not warn
# about the classes missing from the output.
-dontwarn io.github.libxposed.**

# --- framework service provider (referenced by name from the manifest) -------------------------
-keep class io.github.libxposed.service.** { *; }
-keep class com.myhook.core.ConfigProvider { *; }

# --- DexKit -----------------------------------------------------------------------------------
# Native JNI surface; the AAR also ships its own consumer rules.
-keep class org.luckypray.dexkit.** { *; }
-dontwarn org.luckypray.dexkit.**

# --- hooks / features -------------------------------------------------------------------------
# Reached through AppRegistry, but keep them verbatim so adb logcat output stays readable and any
# future reflective lookup keeps working.
-keep class com.myhook.hooks.** { *; }
-keep class com.myhook.features.** { *; }
-keep class com.myhook.core.** { *; }
