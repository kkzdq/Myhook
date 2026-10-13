package com.myhook.core

/** Shared config keys. The UI writes them; hooked processes read them. */
object ConfigKeys {
    const val PREFS = "myhook_config"
    const val AUTHORITY = "com.myhook.config"
    const val CONFIG_URI = "content://$AUTHORITY/config"

    // OPPO Health step editing
    const val OPPO_ENABLE = "oppo_enable"
    const val OPPO_TARGET = "oppo_target_steps" // 0 = disabled
    const val OPPO_MODE = "oppo_mode"           // 0 = floor (raise), 1 = cap (lower)

    // Duolingo timezone faker
    const val DUO_ENABLE = "duo_enable"
    const val DUO_TIMEZONE = "duo_timezone"

    // Per-app extension features. The real config key is "f_<featureKey>_<pkg>" (see Feature.enabledKey).
    const val ACCEL = "accelerator_whitelist"
    const val ACCEL_WHITELIST = "accel_whitelist_apps" // comma separated (derived from the ACCEL toggles)
    const val FEATURE_SENSOR = "disable_sensor"
    const val FEATURE_BLANK_PASS = "blank_pass"
    const val FEATURE_DIALOG_BLOCK = "dialog_blocking"
    const val FEATURE_CONTROL_INTERCEPT = "control_interception"
    const val FEATURE_AD_BLOCK = "ad_blocking"

    /**
     * Per-app master switch. While it is off **nothing** MyHook does applies to that app — neither
     * the app hook nor any extension feature, even if their own toggles are on. Turning it on is
     * also what asks the framework to add the app to the module scope.
     */
    fun masterKey(pkg: String): String = "master_$pkg"
}
