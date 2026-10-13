package com.myhook.hooks

import com.myhook.core.HookContext

/**
 * 网易云音乐 — 禁用下载音乐加密。
 * Hook: com.netease.cloudmusic.meta.MusicInfo.isEncrptDldPayMusic() -> false
 */
object NeteaseMusic : AppHook {
    override val packages = setOf("com.netease.cloudmusic")

    override fun apply(ctx: HookContext) {
        ctx.hookMethod("com.netease.cloudmusic.meta.MusicInfo", "isEncrptDldPayMusic") { false }
    }
}
