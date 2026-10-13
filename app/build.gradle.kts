import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Version comes from the CI workflow (auto-bumped 0.1 each release); defaults for local builds.
val appVersionName = (project.findProperty("appVersionName") as String?) ?: "0.1"
val appVersionCode = ((project.findProperty("appVersionCode") as String?) ?: "10").toInt()

android {
    namespace = "com.myhook"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.myhook"
        minSdk = 27
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            // Shrink + obfuscate: the debug build ships ~42 MB of dex because nothing is stripped.
            // Release is what CI publishes; it is signed with the debug key so it installs directly.
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "META-INF/DEPENDENCIES",
        )
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // LibXposed API — provided by the framework at runtime.
    // The 101 artifacts require compileSdk 36; the 102 artifacts require compileSdk 37
    // (Android 17, currently a preview platform), so we stay on 101. HookVip does the same.
    compileOnly("io.github.libxposed:api:101.0.1")
    // libxposed service — talks to the framework (scope request). Must ship inside the APK.
    // NOTE: this AAR depends on kotlin-stdlib 2.2.10, which is why the Kotlin plugin is 2.2.10.
    implementation("io.github.libxposed:service:101.0.0")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    // NOTE: material-icons-extended is intentionally NOT used — it is ~7000 icons x 5 styles
    // shipped as Kotlin code (~40 MB of dex). Everything the UI needs (Search / Close / ArrowBack)
    // is in material-icons-core, which material3 pulls in already.
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.foundation:foundation")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // DexKit — runtime dex search. Some targets (and both source modules) only expose obfuscated
    // class names, so the hook has to find the method by "return type + string constant" at runtime.
    implementation("org.luckypray:dexkit:2.3.0")
}
