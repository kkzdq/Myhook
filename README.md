# MyHook

一个基于 **LibXposed API 101** 的 LSPosed 模块。给指定应用解锁会员 / 去广告，并附带一组通用的拓展功能。

## 功能

**应用解锁（按应用生效）**

覆盖 30 个常用应用，例如：

| 应用 | 功能 |
|---|---|
| 铁路12306 | 去除开屏广告 |
| 网易云音乐 | 禁用下载音乐加密 |
| 小X分身 / ES文件浏览器 / Thanox / 阿里云盘 / 瞬译 / 设备信息 / 冰箱 / 醒图 | 解锁会员 / 高级版 / Pro |
| 百度输入法 | 解除剪切板数量与字数上限 |
| 百度网盘 / 菜鸟 / 酷安 / 外星仔 | 去除开屏 / 信息流广告 |
| pixiv / 豆瓣 | 解锁高级版 / 去信息流广告 |
| 多邻国 | 解锁订阅 + 时区伪造 |
| OPPO健康 | 步数修改（保底 / 封顶，只改当天） |
| WPS Office | 解锁 Pro |
| 豆包 | 无水印保存图片 |
| 企业微信 | 强制开启平板模式 |
| QQ音乐 | 超品音质 / 去下载限制 |
| Pydroid 3 / 甲壳虫ADB助手 / MacroDroid | 解锁高级版 / 会员 / Pro |
| Via 浏览器 | 屏蔽内置广告域名 |
| 外部存储设备 | 放开 Android/data 等目录访问限制 |

**拓展功能（每个应用单独设置）**

- 加速器白名单 —— 让加速器为指定应用提供 VPN 代理
- 禁用传感器 —— 应用收不到陀螺仪 / 加速度计等数据
- 空白通行证 —— 联系人等隐私查询返回空白
- 弹窗拦截 —— 拦截应用弹出的对话框
- 控件拦截 —— 拦掉 ViewStub 懒加载的广告控件
- 广告拦截 —— 自动关闭优量汇 / 穿山甲 / AdMob / Mbridge 的广告页

**每个应用顶部有总开关**：关闭时该应用的一切功能都不生效；打开时会请求加入模块作用域。

## 使用

1. 安装 Release 里的 APK，在 LSPosed 管理器里启用模块。
2. 打开 MyHook：
   - **应用** 页只列出已支持的应用，勾选 = 请求加入作用域（通知栏确认），已加入的置顶。
   - **拓展功能** 页列出全部应用，点进去按应用单独设置，顶部是总开关。
3. 重启目标应用生效。

日志：`adb logcat -s MyHook`

## 构建 / 发版

`.github/workflows/build.yml` 手动触发：**Actions → Build MyHook → Run workflow**。

每次运行会读已有 tag 算出下一个版本（从 `v0.1` 起，每次 **+0.1**），用 R8 混淆打包 release APK（debug key 签名），然后创建 GitHub Release 并把 APK 挂上去。

本地构建（需要 Android SDK）：

```bash
gradle :app:assembleRelease
```

## 技术要点

- **LibXposed API 101**（不是 102）：102 的 AAR 声明 `minCompileSdk=37`（Android 17，预览平台），101 只要求 `36`。配置走自建 `ContentProvider`，不使用 `XSharedPreferences`（那是会被 LSPosed 标记为「已废弃」的旧机制）。
- **Hook 延迟安装**：`onPackageLoaded` 触发时 `Application` 还没创建，配置读不出来。所以先挂 `Application.attach(Context)` / `onCreate()`，等应用就绪再装 hook。
- **DexKit**：部分目标类名被完全混淆，用运行时 dex 搜索（按「返回类型 + 字符串常量」匹配方法）来定位。
- 构建栈：AGP 8.11.2 / Gradle 8.13 / Kotlin 2.2.10 / compileSdk 36 / minSdk 27。

## 目录结构

```
app/src/main/
├── AndroidManifest.xml                 # XposedProvider + ConfigProvider + MainActivity
├── resources/META-INF/xposed/
│   ├── module.prop                     # minApiVersion=101 / staticScope=false
│   ├── java_init.list                  # com.myhook.MyHookEntry
│   └── scope.list                      # 初始作用域
└── java/com/myhook/
    ├── MyHookEntry.kt                  # XposedModule 入口，延迟安装 hook / feature
    ├── core/                           # 日志、上下文、配置存储与 Provider、HookContext、DexSearch
    ├── hooks/                          # AppHook 接口 + 注册表 + 具体 app hook
    ├── features/                       # Feature 接口 + 注册表 + 拓展功能
    └── ui/                             # Compose UI（应用 / 拓展功能 / 关于）
```
