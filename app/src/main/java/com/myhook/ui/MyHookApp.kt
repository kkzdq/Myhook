package com.myhook.ui

import android.content.Context
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.myhook.features.AcceleratorWhitelist
import com.myhook.features.FeatureRegistry
import com.myhook.hooks.HookCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** One row of the app list: a catalog entry that is actually installed. */
private data class AppRow(
    val pkg: String,
    val name: String,
    val features: String,
    val compat: String,
    val version: String,
    val icon: android.graphics.drawable.Drawable?,
)

private class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences(ConfigKeys.PREFS, Context.MODE_PRIVATE)
    fun bool(k: String, d: Boolean) = sp.getBoolean(k, d)
    fun putBool(k: String, v: Boolean) = sp.edit().putBoolean(k, v).apply()
    fun int(k: String, d: Int) = sp.getInt(k, d)
    fun putInt(k: String, v: Int) = sp.edit().putInt(k, v).apply()
    fun str(k: String, d: String) = sp.getString(k, d) ?: d
    fun putStr(k: String, v: String) = sp.edit().putString(k, v).apply()
}

/** Only apps that (a) appear in HookCatalog and (b) are installed on this device. */
private suspend fun loadCatalog(ctx: Context): List<AppRow> = withContext(Dispatchers.IO) {
    val pm = ctx.packageManager
    HookCatalog.ENTRIES.mapNotNull { e ->
        try {
            val pi = pm.getPackageInfo(e.pkg, 0)
            val ai = pi.applicationInfo ?: return@mapNotNull null
            AppRow(
                pkg = e.pkg,
                name = e.name,
                features = e.features,
                compat = e.compat,
                version = pi.versionName ?: "",
                icon = try {
                    ai.loadIcon(pm)
                } catch (t: Throwable) {
                    null
                },
            )
        } catch (t: Throwable) {
            null // not installed
        }
    }
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
    var rows by remember { mutableStateOf<List<AppRow>>(emptyList()) }
    var scopeSet by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showWhitelist by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        rows = loadCatalog(ctx)
        // keep the scope state in sync with LSPosed (apps added outside MyHook show as checked too)
        while (true) {
            scopeSet = ScopeManager.currentScope().toSet()
            delay(2000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MyHook") },
                actions = {
                    if (tab == 0) {
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
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(title) })
                }
            }
            when (tab) {
                0 -> AppsTab(
                    rows = rows,
                    scopeSet = scopeSet,
                    searchOpen = searchOpen,
                    query = query,
                    onQuery = { query = it },
                    onToggle = { pkg, want ->
                        if (want) {
                            ScopeManager.requestScope(listOf(pkg)) { _, msg ->
                                scope.launch { snackbar.showSnackbar(msg) }
                            }
                        } else {
                            ScopeManager.removeScope(listOf(pkg)) { _, msg ->
                                scope.launch { snackbar.showSnackbar(msg) }
                            }
                        }
                    },
                )
                1 -> FeaturesTab(prefs, rows, { showWhitelist = it })
                else -> AboutTab()
            }
        }
    }

    if (showWhitelist) {
        val initial = prefs.str(ConfigKeys.ACCEL_WHITELIST, "")
            .split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        WhitelistDialog(
            rows = rows,
            initial = initial,
            onDismiss = { showWhitelist = false },
            onConfirm = { picked ->
                prefs.putStr(ConfigKeys.ACCEL_WHITELIST, picked.joinToString(","))
                showWhitelist = false
            },
        )
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
    // only apps with hooks (already ensured by loadCatalog), filter by search,
    // then sort: scoped apps first, then by name.
    val shown = remember(rows, scopeSet, query) {
        rows.asSequence()
            .filter {
                query.isBlank() ||
                    it.name.contains(query, ignoreCase = true) ||
                    it.pkg.contains(query, ignoreCase = true)
            }
            .sortedWith(
                compareByDescending<AppRow> { it.pkg in scopeSet }
                    .thenBy { it.name }
            )
            .toList()
    }

    Column(Modifier.fillMaxSize()) {
        if (searchOpen) {
            OutlinedTextField(
                value = query,
                onValueChange = onQuery,
                label = { Text("搜索应用名 / 包名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
        if (shown.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (rows.isEmpty()) "未检测到已安装的支持应用" else "没有匹配的应用",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            return
        }
        LazyColumn(Modifier.fillMaxSize()) {
            items(shown, key = { it.pkg }) { row ->
                AppCard(
                    row = row,
                    checked = row.pkg in scopeSet,
                    onCheckedChange = { onToggle(row.pkg, it) },
                )
            }
        }
    }
}

@Composable
private fun AppCard(row: AppRow, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val bmp = remember(row.pkg) {
                row.icon?.let { runCatching { it.toBitmap(96, 96).asImageBitmap() }.getOrNull() }
            }
            if (bmp != null) {
                Image(
                    bitmap = bmp,
                    contentDescription = null,
                    modifier = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)),
                )
            } else {
                Box(Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surface))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (row.version.isBlank()) row.name else "${row.name} - ${row.version}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (row.features.isNotBlank()) {
                    Text(
                        row.features,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (row.compat.isNotBlank()) {
                    Text(
                        "适配版本: ${row.compat}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    row.pkg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun FeaturesTab(
    prefs: Prefs,
    rows: List<AppRow>,
    setShowWhitelist: (Boolean) -> Unit,
) {
    var oppoEnable by remember { mutableStateOf(prefs.bool(ConfigKeys.OPPO_ENABLE, false)) }
    var oppoTarget by remember { mutableStateOf(prefs.int(ConfigKeys.OPPO_TARGET, 30000).toString()) }
    var oppoMode by remember { mutableIntStateOf(prefs.int(ConfigKeys.OPPO_MODE, 0)) }
    var duoEnable by remember { mutableStateOf(prefs.bool(ConfigKeys.DUO_ENABLE, false)) }
    var duoZone by remember { mutableStateOf(prefs.str(ConfigKeys.DUO_TIMEZONE, "Asia/Tokyo")) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        FeatureHeader("OPPO 健康 · 步数修改")
        ListItem(
            headlineContent = { Text("启用") },
            supportingContent = { Text("hook OPPO 数据引擎 getTotalSteps，可保底/封顶") },
            trailingContent = {
                Switch(checked = oppoEnable, onCheckedChange = { oppoEnable = it; prefs.putBool(ConfigKeys.OPPO_ENABLE, it) })
            },
        )
        OutlinedTextField(
            value = oppoTarget,
            onValueChange = { v ->
                oppoTarget = v.filter { it.isDigit() }
                oppoTarget.toIntOrNull()?.let { prefs.putInt(ConfigKeys.OPPO_TARGET, it) }
            },
            label = { Text("目标步数") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        )
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = oppoMode == 0, onClick = { oppoMode = 0; prefs.putInt(ConfigKeys.OPPO_MODE, 0) }, label = { Text("保底(低则升)") })
            FilterChip(selected = oppoMode == 1, onClick = { oppoMode = 1; prefs.putInt(ConfigKeys.OPPO_MODE, 1) }, label = { Text("封顶(高则降)") })
        }

        HorizontalDivider()
        FeatureHeader("多邻国 · 时区伪造")
        ListItem(
            headlineContent = { Text("启用") },
            supportingContent = { Text("把进程时区伪装成指定值（默认 Asia/Tokyo）") },
            trailingContent = {
                Switch(checked = duoEnable, onCheckedChange = { duoEnable = it; prefs.putBool(ConfigKeys.DUO_ENABLE, it) })
            },
        )
        OutlinedTextField(
            value = duoZone,
            onValueChange = { duoZone = it; prefs.putStr(ConfigKeys.DUO_TIMEZONE, it) },
            label = { Text("时区 ID") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        )

        HorizontalDivider()
        FeatureHeader("拓展功能")
        FeatureRegistry.all.forEach { feature ->
            key(feature.key) {
                var on by remember { mutableStateOf(prefs.bool(feature.key, false)) }
                ListItem(
                    headlineContent = { Text(feature.title) },
                    supportingContent = { Text(feature.desc) },
                    trailingContent = {
                        Switch(checked = on, onCheckedChange = { on = it; prefs.putBool(feature.key, it) })
                    },
                )
                if (feature is AcceleratorWhitelist) {
                    Button(
                        onClick = { setShowWhitelist(true) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    ) {
                        val n = prefs.str(ConfigKeys.ACCEL_WHITELIST, "").split(",").count { it.trim().isNotEmpty() }
                        Text("选择白名单应用 ($n)")
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FeatureHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun WhitelistDialog(
    rows: List<AppRow>,
    initial: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit,
) {
    var picked by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("加速器代理白名单") },
        text = {
            LazyColumn(Modifier.height(400.dp)) {
                items(rows, key = { it.pkg }) { row ->
                    ListItem(
                        headlineContent = { Text(row.name) },
                        supportingContent = { Text(row.pkg) },
                        trailingContent = {
                            Checkbox(checked = row.pkg in picked, onCheckedChange = {
                                picked = if (row.pkg in picked) picked - row.pkg else picked + row.pkg
                            })
                        },
                        modifier = Modifier.clickable {
                            picked = if (row.pkg in picked) picked - row.pkg else picked + row.pkg
                        },
                    )
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(picked.toList()) }) { Text("保存") } },
        dismissButton = { Button(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun AboutTab() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("MyHook", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("LibXposed API 101 模块。基于 HookVip（应用 hook / 动态作用域）+ Fuck for VIP（拓展功能）的思路重写，并内置 StepShook（OPPO 步数）与 duotimefaker（多邻国时区）。")
        Spacer(Modifier.height(8.dp))
        Text("勾选应用 = 直接请求加入作用域（通知栏确认）。已加入的置顶显示。")
        Spacer(Modifier.height(8.dp))
        Text("日志: adb logcat -s MyHook", style = MaterialTheme.typography.bodySmall)
    }
}
