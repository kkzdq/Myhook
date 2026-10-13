package com.myhook.core

/** Shared config keys. The UI writes them; hooked processes read them. */
object ConfigKeys {
    const val PREFS = "myhook_config"
    const val AUTHORITY = "com.myhook.config"
    const val CONFIG_URI = "content://$AUTHORITY/config"

    // OPPO Health step editing (ported from StepShook)
    const val OPPO_ENABLE = "oppo_enable"
    const val OPPO_TARGET = "oppo_target_steps" // 0 = disabled
    const val OPPO_MODE = "oppo_mode"           // 0 = floor (raise), 1 = cap (lower)

    // Duolingo timezone faker (ported from duotimefaker)
    const val DUO_ENABLE = "duo_enable"
    const val DUO_TIMEZONE = "duo_timezone"

    // Extension features
    const val ACCEL_ENABLE = "accel_whitelist_enable"
    const val ACCEL_WHITELIST = "accel_whitelist_apps" // comma separated

    const val HIDE_ROOT_ENABLE = "hide_root_enable"
    const val HIDE_DEV_ENABLE = "hide_dev_enable"
    const val FLAG_SECURE_ENABLE = "flag_secure_enable"
    const val HIDE_VPN_ENABLE = "hide_vpn_enable"
    const val TOAST_FILTER_ENABLE = "toast_filter_enable"
    const val TOAST_FILTER_KEYWORDS = "toast_filter_keywords" // comma separated
}
