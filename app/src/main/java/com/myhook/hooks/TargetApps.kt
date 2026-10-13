package com.myhook.hooks

/**
 * The user's main scope apps (name -> package), confirmed by the user.
 * Kept here so the app registry and future per-app hooks can refer to them by a stable name.
 */
object TargetApps {

    val ALL: Map<String, String> = mapOf(
        "OPPO健康" to "com.heytap.health",
        "appshare" to "info.muge.appshare",
        "百度输入法" to "com.baidu.input",
        "百度网盘" to "com.baidu.netdisk",
        "小X分身" to "com.bly.dkplat",
        "菜鸟" to "com.cainiao.wireless",
        "设备信息" to "com.liuzh.deviceinfo",
        "豆包" to "com.larus.nova",
        "多邻国" to "com.duolingo",
        "ES文件浏览器" to "com.estrongs.android.pop",
        "外星仔" to "com.etalien.booster",
        "冰箱" to "com.catchingnow.icebox",
        "铁路12306" to "com.MobileTicket",
        "网易云音乐" to "com.netease.cloudmusic",
        "pixiv" to "jp.pxv.android",
        "存储空间隔离" to "moe.shizuku.redirectstorage",
        "瞬译" to "com.spaceship.screen.textcopy",
        "thanox" to "github.tornaco.android.thanos",
        "wps" to "cn.wps.moffice_eng",
        "醒图" to "com.xt.retouch",
        "阿里云盘" to "com.alicloud.databox",
        "豆瓣" to "com.douban.frodo",
        "企业微信" to "com.tencent.wework",
        "pydroid" to "ru.iiec.pydroid3",
        "QQ音乐" to "com.tencent.qqmusic",
        "via" to "mark.via",
        "macrodroid" to "com.arlosoft.macrodroid",
        "甲壳虫adb助手" to "com.didjdk.adbhelper",
        "外部存储设备" to "com.android.externalstorage",
        "酷安" to "com.coolapk.market",
    )

    val allPackages: Set<String> get() = ALL.values.toSet()
}
