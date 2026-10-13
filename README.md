# MyHook

一个基于 **LibXposed API 101** 的 LSPosed 模块骨架。目标是把你日常用的两个 hook 模块合并成一个：

- 应用 hook 方法与动态作用域 → 参考 **HookVip**（新 API，无“已废弃”警告，支持应用内勾选加入作用域）
- 拓展功能 → 参考 **Fuck for VIP**（旧 API，触发“此模块使用了已废弃且即将移除的功能”）
- 内置两个小模块：**StepShook**（OPPO 健康步数）与 **duotimefaker**（多邻国时区伪造）

## 为什么用 LibXposed 新 API（101）

LSPosed v2.2.0 的 daemon 会扫描模块：**模块 API 版本 < 100（旧 API）+ 声明了 `xposedminversion` + 存在 world-readable 偏好文件** 三者同时成立，才标记“已废弃”。这正是 `XSharedPreferences` 的机制。

- Fuck for VIP：`XSharedPreferences` 用了 112 处 → 被标记。
- HookVip：`META-INF/xposed/module.prop` = `minApiVersion=101 / targetApiVersion=101 / staticScope=false` → 直接放过。

本模块 `minApiVersion=101 / targetApiVersion=101`，配置走自建 `ContentProvider`（`com.myhook.config`），**不碰 `XSharedPreferences`**。

> **为什么不用 102**：libxposed 的 `api`/`service` AAR 在 102 版本声明 `minCompileSdk=37`（Android 17，目前还是预览平台）；101 版本只要求 `minCompileSdk=36`。所以用 101 的 AAR + `compileSdk=36` + AGP 8.11.2 + Gradle 8.13 + **Kotlin 2.2.10**（`service` AAR 依赖 `kotlin-stdlib:2.2.10`，Kotlin 插件必须同版本，否则编译器内部错误）。等 102 的 AAR 落到稳定 SDK 再切。
>
> **注意**：`targetApiVersion>=102` 时禁止调用旧的 `de.robv.android.xposed` API；本模块只用新 API，将来切 102 也不用改代码。

## 构建（GitHub Actions）

仓库自带 `.github/workflows/build.yml`：push 到 `main`/`master` 或手动 `workflow_dispatch` 即编译，产物在 Actions 的 `MyHook-debug-apk` artifact 里。

本地（可选，需要 Android SDK）：

```bash
gradle :app:assembleDebug
# 或
./gradlew :app:assembleDebug
```

依赖（已在 `app/build.gradle.kts` 固定）：

```kotlin
compileOnly("io.github.libxposed:api:101.0.1")          // 框架运行时提供
implementation("io.github.libxposed:service:101.0.0")    // 随 APK 打包，用于作用域请求
```

## 安装与使用

1. 安装 CI 产出的 `app-debug.apk`。
2. 在 LSPosed 管理器里启用模块（作用域可以只留空，靠应用内勾选添加）。
3. 打开 MyHook：
   - **应用** 栏勾选目标 app → 点“加入作用域” → 通知栏确认。
   - **拓展功能** 栏开关各项功能、设置参数、选择加速器白名单。
4. 重启目标 app 生效（作用域变更后 LSPosed 会自行处理）。

日志：`adb logcat -s MyHook`

## 目录结构

```
app/src/main/
├── AndroidManifest.xml                     # XposedProvider + ConfigProvider + MainActivity
├── resources/META-INF/xposed/
│   ├── module.prop                         # minApiVersion=101 / staticScope=false
│   ├── java_init.list                      # com.myhook.MyHookEntry
│   └── scope.list                          # 初始作用域
└── java/com/myhook/
    ├── MyHookEntry.kt                      # XposedModule 入口，分发 hook / feature
    ├── core/                               # 日志、上下文、配置存储与 Provider、HookContext
    ├── hooks/                              # AppHook 接口 + 注册表 + 具体 app hook
    │   ├── OppoHealthSteps.kt              # OPPO 步数（StepShook 移植）
    │   └── DuolingoTimezone.kt             # 多邻国时区（duotimefaker 移植）
    ├── features/                           # Feature 接口 + 注册表 + 拓展功能
    │   └── AcceleratorWhitelist.kt         # 加速器代理白名单（FFV 实现）
    └── ui/                                 # Compose UI（FFV 三栏风格）
```

## 阶段一范围（当前）

- [x] LibXposed API 101 骨架、Gradle、CI
- [x] 动态作用域请求（应用内勾选 → 通知栏确认）
- [x] 配置存储（自建 ContentProvider，避开废弃 API）
- [x] OPPO 健康步数（保底 / 封顶，只改当天）
- [x] 多邻国时区伪造
- [x] 加速器代理白名单（用户自定义白名单 + 排除列表 + 逐项容错）
- [x] Compose UI（应用 / 拓展功能 / 关于）

## 阶段二（待办）

- [ ] 移植 HookVip 的逐 app hook（com.netease.cloudmusic、tv.danmaku.bili 等）
- [ ] 补全拓展功能：JustTrustMe、隐藏 root/su、隐藏开发者模式、传感器伪造、协议降级、Toast 拦截
- [ ] 补齐 FFV 独有应用（清洗 `xposed_scope` 里的占位包名）
- [ ] 目标 app 版本变化时的自适配

## 说明

阶段一为**骨架**：hook 目标类名/方法名按 OPPO 健康 / 多邻国当前版本编写，OPPO 健康升级后类名可能变动（`OppoHealthSteps` 会在 logcat 打出 `no target class matched`）。逐 app 的 VIP hook 是长期活，按优先级增量补。
