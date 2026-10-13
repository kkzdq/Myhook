package com.myhook.ui

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.myhook.core.ConfigKeys
import com.myhook.features.FeatureRegistry
import com.myhook.hooks.HookCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** One row of an app list. */
private data class AppRow(
    val pkg: String,
    val name: String,
    val features: String,
    val compat: String,
    val version: String,
    val icon: android.graphics.drawable.Drawable?,
)

private class Prefs(ctx: Context) {
    val sp = ctx.getSharedPreferences(ConfigKeys.PREFS, Context.MODE_PRIVATE)
    fun bool(k: String, d: Boolean) = sp.getBoolean(k, d)
    fun putBool(k: String, v: Boolean) = sp.edit().putBoolean(k, v).apply()
    fun int(k: String, d: Int) = sp.getInt(k, d)
    fun putInt(k: String, v: Int) = sp.edit().putInt(k, v).apply()
    fun str(k: String, d: String) = sp.getString(k, d) ?: d
    fun putStr(k: String, v: String) = sp.edit().putString(k, v).apply()
    fun keys(): Set<String> = sp.all.keys
}

/** Apps that appear in HookCatalog (i.e. have a hook) and are installed. */
private suspend fun loadCatalog(ctx: Context): List<AppRow> = withContext(Dispatchers.IO) {
    val pm = ctx.packageManager
    HookCatalog.ENTRIES.mapNotNull { e ->
        val row = try {
            val pi = pm.getPackageInfo(e.pkg, 0)
            val ai = pi.applicationInfo ?: return@mapNotNull null
            AppRow(
                pkg = e.pkg, name = e.name, features = e.features, compat = e.compat,
                version = pi.versionName ?: "",
                icon = try { ai.loadIcon(pm) } catch (t: Throwable) { null },
            )
        } catch (t: Throwable) {
            null
        }
        row
    }
}

/** Every launchable app on the device (for the extension-feature page). */
private suspend fun loadAllApps(ctx: Context): List<AppRow> = withContext(Dispatchers.IO) {
    val pm = ctx.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    @Suppress("DEPRECATION")
    val resolved = pm.queryIntentActivities(intent, 0)
    resolved.mapNotNull { ri ->
        try {
            val ai = ri.activityInfo.applicationInfo
            val pi = pm.getPackageInfo(ai.packageName, 0)
            AppRow(
                pkg = ai.packageName,
                name = pm.getApplicationLabel(ai).toString(),
                features = "", compat = "",
                version = pi.versionName ?: "",
                icon = try { ai.loadIcon(pm) } catch (t: Throwable) { null },
            )
        } catch (t: Throwable) {
            null
        }
    }.distinctBy { it.pkg }.sortedBy { it.name }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyHookApp(activity: ComponentActivity) {
    val ctx: Context = activity
    val prefs = remember { Prefs(ctx) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var tab by remember { mutableIntStateOf(0) }
    var searchOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var catalog by remember { mutableStateOf<List<AppRow>>(emptyList()) }
    var allApps by remember { mutableStateOf<List<AppRow>>(emptyList()) }
    var scopeSet by remember { mutableStateOf<Set<String>>(emptySet()) }
    var settingsApp by remember { mutableStateOf<AppRow?>(null) }

    LaunchedEffect(Unit) {
        catalog = loadCatalog(ctx)
        allApps = loadAllApps(ctx)
        while (true) {
            scopeSet = ScopeManager.currentScope().toSet()
            delay(2000)
        }
    }

    // per-app feature settings screen
    val open = settingsApp
    if (open != null) {
        AppFeatureScreen(app = open, prefs = prefs, onBack = { settingsApp = null })
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MyHook") },
                actions = {
                    if (tab == 0 || tab == 1) {
                        IconButton(onClick = {
                            searchOpen = !searchOpen
                            if (!searchOpen) query = ""
                        }) {
                            Icon(
                                if (searchOpen) Icons.Filled.Close else Icons.Filled.Search,
                                contentDescription = "搜索应用",
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            TabRow(selectedTabIndex = tab) {
                listOf("应用", "拓展功能", "关于").forEachIndexed { i, title ->
                    Tab(selected = tab == i, onClick = { tab = i; query = "" }, text = { Text(title) })
                }
            }
            when (tab) {
                0 -> AppsTab(
                    rows = catalog, scopeSet = scopeSet,
                    searchOpen = searchOpen, query = query, onQuery = { query = it },
                    onToggle = { pkg, want ->
                        if (want) {
                            ScopeManager.requestScope(listOf(pkg)) { _, msg -> scope.launch { snackbar.showSnackbar(msg) } }
                        } else {
                            ScopeManager.removeScope(listOf(pkg)) { _, msg -> scope.launch { snackbar.showSnackbar(msg) } }
                        }
                    },
                )
                1 -> AllAppsTab(
                    rows = allApps, prefs = prefs,
                    searchOpen = searchOpen, query = query, onQuery = { query = it },
                    onOpen = { settingsApp = it },
                )
                else -> AboutTab()
            }
        }
    }
}

@Composable
private fun AppsTab(
    rows: List<AppRow>,
    scopeSet: Set<String>,
    searchOpen: Boolean,
    query: String,
    onQuery: (String) -> Unit,
    onToggle: (String, Boolean) -> Unit,
) {
    val shown = remember(rows, scopeSet, query) {
        rows.asSequence()
            .filter { query.isBlank() || it.name.contains(query, true) || it.pkg.contains(query, true) }
            .sortedWith(compareByDescending<AppRow> { it.pkg in scopeSet }.thenBy { it.name })
            .toList()
    }
    Column(Modifier.fillMaxSize()) {
        if (searchOpen) {
            OutlinedTextField(
                value = query, onValueChange = onQuery,
                label = { Text("搜索应用名 / 包名") }, singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
        if (shown.isEmpty()) {
            EmptyHint(if (rows.isEmpty()) "未检测到已安装的支持应用" else "没有匹配的应用")
            return
        }
        LazyColumn(Modifier.fillMaxSize()) {
            items(shown, key = { it.pkg }) { row ->
                AppCard(row = row, checked = row.pkg in scopeSet, onClick = null) { onToggle(row.pkg, it) }
            }
        }
    }
}

@Composable
private fun AllAppsTab(
    rows: List<AppRow>,
    prefs: Prefs,
    searchOpen: Boolean,
    query: String,
    onQuery: (String) -> Unit,
    onOpen: (AppRow) -> Unit,
) {
    val shown = remember(rows, query) {
        rows.filter { query.isBlank() || it.name.contains(query, true) || it.pkg.contains(query, true) }
    }
    Column(Modifier.fillMaxSize()) {
        if (searchOpen) {
            OutlinedTextField(
                value = query, onValueChange = onQuery,
                label = { Text("搜索应用名 / 包名") }, singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
        Text(
            "点应用进去单独设置拓展功能（${FeatureRegistry.all.size} 项）",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
        )
        if (shown.isEmpty()) {
            EmptyHint("没有匹配的应用")
            return
        }
        LazyColumn(Modifier.fillMaxSize()) {
            items(shown, key = { it.pkg }) { row ->
                val n = remember(row.pkg, prefs) {
                    FeatureRegistry.all.count { prefs.bool(it.enabledKey(row.pkg), false) }
                }
                AppCard(
                    row = row,
                    checked = n > 0,
                    checkedReadOnly = true,
                    onClick = { onOpen(row) },
                    trailingLabel = if (n > 0) "已开 $n" else null,
                ) { }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppFeatureScreen(app: AppRow, prefs: Prefs, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState())) {
            Text(
                "${app.pkg}${if (app.version.isBlank()) "" else "  ·  ${app.version}"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )

            // app-specific settings
            if (app.pkg == "com.heytap.health") {
                OppoSettings(prefs)
                HorizontalDivider()
            }
            if (app.pkg == "com.duolingo") {
                DuoSettings(prefs)
                HorizontalDivider()
            }

            FeatureRegistry.all.forEach { feature ->
                key(feature.key) {
                    var on by remember { mutableStateOf(prefs.bool(feature.enabledKey(app.pkg), false)) }
                    ListItem(
                        headlineContent = { Text(feature.title) },
                        supportingContent = { Text(feature.desc) },
                        trailingContent = {
                            Switch(checked = on, onCheckedChange = {
                                on = it
                                prefs.putBool(feature.enabledKey(app.pkg), it)
                                rebuildWhitelist(prefs)
                            })
                        },
                    )
                    HorizontalDivider()
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OppoSettings(prefs: Prefs) {
    var enable by remember { mutableStateOf(prefs.bool(ConfigKeys.OPPO_ENABLE, false)) }
    var target by remember { mutableStateOf(prefs.int(ConfigKeys.OPPO_TARGET, 30000).toString()) }
    var mode by remember { mutableIntStateOf(prefs.int(ConfigKeys.OPPO_MODE, 0)) }
    ListItem(
        headlineContent = { Text("步数修改") },
        supportingContent = { Text("hook OPPO 数据引擎 getTotalSteps，可保底/封顶") },
        trailingContent = { Switch(checked = enable, onCheckedChange = { enable = it; prefs.putBool(ConfigKeys.OPPO_ENABLE, it) }) },
    )
    OutlinedTextField(
        value = target,
        onValueChange = { v -> target = v.filter { it.isDigit() }; target.toIntOrNull()?.let { prefs.putInt(ConfigKeys.OPPO_TARGET, it) } },
        label = { Text("目标步数") },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
    )
    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = mode == 0, onClick = { mode = 0; prefs.putInt(ConfigKeys.OPPO_MODE, 0) }, label = { Text("保底(低则升)") })
        FilterChip(selected = mode == 1, onClick = { mode = 1; prefs.putInt(ConfigKeys.OPPO_MODE, 1) }, label = { Text("封顶(高则降)") })
    }
}

@Composable
private fun DuoSettings(prefs: Prefs) {
    var enable by remember { mutableStateOf(prefs.bool(ConfigKeys.DUO_ENABLE, false)) }
    var zone by remember { mutableStateOf(prefs.str(ConfigKeys.DUO_TIMEZONE, "Asia/Tokyo")) }
    ListItem(
        headlineContent = { Text("时区伪造") },
        supportingContent = { Text("把进程时区伪装成指定值（默认 Asia/Tokyo）") },
        trailingContent = { Switch(checked = enable, onCheckedChange = { enable = it; prefs.putBool(ConfigKeys.DUO_ENABLE, it) }) },
    )
    OutlinedTextField(
        value = zone,
        onValueChange = { zone = it; prefs.putStr(ConfigKeys.DUO_TIMEZONE, it) },
        label = { Text("时区 ID") },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
    )
}

/** The accelerator whitelist is derived from every app that has the accelerator toggle enabled. */
private fun rebuildWhitelist(prefs: Prefs) {
    val prefix = "f_${ConfigKeys.ACCEL}_"
    val pkgs = prefs.keys().filter { it.startsWith(prefix) && prefs.bool(it, false) }
        .map { it.removePrefix(prefix) }
    prefs.putStr(ConfigKeys.ACCEL_WHITELIST, pkgs.joinToString(","))
}

@Composable
private fun AppCard(
    row: AppRow,
    checked: Boolean,
    onClick: (() -> Unit)?,
    checkedReadOnly: Boolean = false,
    trailingLabel: String? = null,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val bmp = remember(row.pkg) {
            row.icon?.let { runCatching { it.toBitmap(96, 96).asImageBitmap() }.getOrNull() }
        }
        if (bmp != null) {
            Image(bitmap = bmp, contentDescription = null, modifier = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)))
        } else {
            Box(Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surface))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (row.version.isBlank()) row.name else "${row.name} - ${row.version}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            if (row.features.isNotBlank()) {
                Text(row.features, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (row.compat.isNotBlank()) {
                Text("适配版本: ${row.compat}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(row.pkg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (trailingLabel != null) {
                Text(trailingLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        if (!checkedReadOnly) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun AboutTab() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("MyHook", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("LibXposed API 101 模块。应用 hook 思路来自 HookVip，拓展功能思路来自 Fuck for VIP，内置 StepShook（OPPO 步数）与 duotimefaker（多邻国时区）。")
        Spacer(Modifier.height(8.dp))
        Text("· 应用页：勾选 = 直接请求加入作用域（通知栏确认），已加入的置顶。")
        Text("· 拓展功能页：列出全部应用，点进去按 app 单独开关。")
        Spacer(Modifier.height(8.dp))
        Text("日志: adb logcat -s MyHook", style = MaterialTheme.typography.bodySmall)
    }
}
