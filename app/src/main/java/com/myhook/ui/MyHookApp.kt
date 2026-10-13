package com.myhook.ui

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.myhook.core.ConfigKeys
import com.myhook.features.AcceleratorWhitelist
import com.myhook.features.FeatureRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppEntry(val label: String, val pkg: String, val system: Boolean)

private class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences(ConfigKeys.PREFS, Context.MODE_PRIVATE)
    fun bool(k: String, d: Boolean) = sp.getBoolean(k, d)
    fun putBool(k: String, v: Boolean) = sp.edit().putBoolean(k, v).apply()
    fun int(k: String, d: Int) = sp.getInt(k, d)
    fun putInt(k: String, v: Int) = sp.edit().putInt(k, v).apply()
    fun str(k: String, d: String) = sp.getString(k, d) ?: d
    fun putStr(k: String, v: String) = sp.edit().putString(k, v).apply()
}

private suspend fun loadApps(ctx: Context): List<AppEntry> = withContext(Dispatchers.IO) {
    val pm = ctx.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    @Suppress("DEPRECATION")
    val resolved = pm.queryIntentActivities(intent, 0)
    resolved.map {
        val ai = it.activityInfo.applicationInfo
        AppEntry(
            label = pm.getApplicationLabel(ai).toString(),
            pkg = ai.packageName,
            system = (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
        )
    }.distinctBy { it.pkg }.sortedBy { it.label }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyHookApp(activity: ComponentActivity) {
    val ctx: Context = activity
    val prefs = remember { Prefs(ctx) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var tab by remember { mutableIntStateOf(0) }
    var apps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showWhitelist by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { apps = loadApps(ctx) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("MyHook") }) },
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
                    apps = apps,
                    selected = selected,
                    onToggle = { pkg ->
                        selected = if (pkg in selected) selected - pkg else selected + pkg
                    },
                    onAddScope = {
                        if (selected.isEmpty()) {
                            scope.launch { snackbar.showSnackbar("先勾选要加入作用域的应用") }
                        } else {
                            ScopeManager.requestScope(selected.toList()) { ok, msg ->
                                scope.launch { snackbar.showSnackbar(msg) }
                            }
                        }
                    },
                )
                1 -> FeaturesTab(prefs, apps, { showWhitelist = it })
                else -> AboutTab()
            }
        }
    }

    if (showWhitelist) {
        val initial = prefs.str(ConfigKeys.ACCEL_WHITELIST, "")
            .split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        WhitelistDialog(
            apps = apps,
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
    apps: List<AppEntry>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onAddScope: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Text(
            "勾选应用后点“加入作用域”，在通知栏确认即可（无需重启 LSPosed 管理器）。",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp),
        )
        Button(
            onClick = onAddScope,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        ) { Text("加入作用域 (${selected.size})") }
        HorizontalDivider()
        LazyColumn(Modifier.fillMaxSize()) {
            items(apps, key = { it.pkg }) { app ->
                ListItem(
                    headlineContent = { Text(app.label) },
                    supportingContent = { Text(app.pkg) },
                    trailingContent = {
                        Checkbox(checked = app.pkg in selected, onCheckedChange = { onToggle(app.pkg) })
                    },
                    modifier = Modifier.clickable { onToggle(app.pkg) },
                )
            }
        }
    }
}

@Composable
private fun FeaturesTab(
    prefs: Prefs,
    apps: List<AppEntry>,
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
    apps: List<AppEntry>,
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
                items(apps, key = { it.pkg }) { app ->
                    ListItem(
                        headlineContent = { Text(app.label) },
                        supportingContent = { Text(app.pkg) },
                        trailingContent = {
                            Checkbox(checked = app.pkg in picked, onCheckedChange = {
                                picked = if (app.pkg in picked) picked - app.pkg else picked + app.pkg
                            })
                        },
                        modifier = Modifier.clickable {
                            picked = if (app.pkg in picked) picked - app.pkg else picked + app.pkg
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
        Text("日志: adb logcat -s MyHook", style = MaterialTheme.typography.bodySmall)
    }
}
