# Keep the LibXposed module entry classes reachable from META-INF/xposed/java_init.list.
-keep,allowoptimization,allowobfuscation public class * extends io.github.libxposed.api.XposedModule {
    public <init>();
}
# Keep the framework service provider + helper (referenced by name from the manifest / framework).
-keep class io.github.libxposed.service.** { *; }
-keep class io.github.libxposed.api.** { *; }
# Keep our ContentProvider (referenced by name from the manifest).
-keep class com.myhook.core.ConfigProvider { *; }
