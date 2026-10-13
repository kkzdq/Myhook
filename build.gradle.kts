// Root build file. Versions are pinned for reproducibility on CI.
// AGP 8.11.x is required because the LibXposed AARs declare minCompileSdk=36.
plugins {
    id("com.android.application") version "8.11.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
