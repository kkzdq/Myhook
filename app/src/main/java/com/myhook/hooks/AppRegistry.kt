package com.myhook.hooks

import com.myhook.core.ConfigKeys
import com.myhook.core.ConfigStore
import com.myhook.features.Feature
import com.myhook.features.FeatureRegistry

/** Central registry of every hook. Add new app hooks here. */
object AppRegistry {

    private val hooks: List<AppHook> = listOf(
        OppoHealthSteps,
        DuolingoTimezone,
        DuolingoVip,
        NeteaseMusic,
        BiliDkplat,
        BaiduInput,
        BaiduNetdisk,
        MobileTicket,
        Pixiv,
        AppShare,
        Cainiao,
        Etalien,
        Doubao,
        Thanox,
        EsExplorer,
        ShunYi,
        DeviceInfo,
        Wps,
        AliCloud,
        Douban,
        AdbHelper,
        Pydroid,
    )

    /** True when the per-app master switch is on. Nothing MyHook does applies while it is off. */
    fun enabled(pkg: String): Boolean = ConfigStore.bool(ConfigKeys.masterKey(pkg), false)

    fun hooksFor(pkg: String): List<AppHook> =
        if (enabled(pkg)) hooks.filter { pkg in it.packages } else emptyList()

    fun featuresFor(pkg: String): List<Feature> =
        if (enabled(pkg)) FeatureRegistry.enabledFor(pkg) else emptyList()

    /** True when at least one real hook targets [pkg] (i.e. the app is actually supported). */
    fun hasHook(pkg: String): Boolean = hooks.any { pkg in it.packages }

    /** How many hook classes target [pkg]. */
    fun hookCountFor(pkg: String): Int = hooks.count { pkg in it.packages }

    /** Every package that currently has an implemented hook. */
    fun supportedPackages(): Set<String> = hooks.flatMapTo(HashSet()) { it.packages }

    /**
     * Packages that have a hook but are NOT in the generated [HookCatalog] (the catalog only
     * contains HookVip's apps, so ports from StepShook / duotimefaker / Fuck-for-VIP are missing).
     * Value = display name to feature description.
     */
    private val EXTRA_META: Map<String, Pair<String, String>> = mapOf(
        "com.heytap.health" to ("OPPO健康" to "步数修改（保底/封顶）"),
        "com.duolingo" to ("多邻国" to "解锁订阅 / 时区伪造"),
        "com.alicloud.databox" to ("阿里云盘" to "解锁会员/试用"),
        "com.douban.frodo" to ("豆瓣" to "去信息流广告"),
        "com.didjdk.adbhelper" to ("甲壳虫ADB助手" to "解锁会员"),
        "ru.iiec.pydroid3" to ("Pydroid 3" to "解锁高级版"),
    )

    private val catalogByPkg: Map<String, HookCatalog.Entry> by lazy {
        HookCatalog.ENTRIES.associateBy { it.pkg }
    }

    /** Display name: HookCatalog -> EXTRA_META -> caller-provided fallback -> package name. */
    fun displayName(pkg: String, fallback: String? = null): String =
        catalogByPkg[pkg]?.name ?: EXTRA_META[pkg]?.first ?: fallback ?: pkg

    /** Feature summary shown under the app name. */
    fun featureSummary(pkg: String): String =
        catalogByPkg[pkg]?.features ?: EXTRA_META[pkg]?.second ?: ""

    /** Compatibility note shown under the feature summary. */
    fun compatFor(pkg: String): String = catalogByPkg[pkg]?.compat ?: "全版本通杀"
}
