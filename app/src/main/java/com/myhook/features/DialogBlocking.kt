package com.myhook.features

import android.content.DialogInterface
import com.myhook.core.ConfigKeys
import com.myhook.core.HookContext
import com.myhook.core.HookLog

/**
 * Hooks `Dialog.show()` so the dialog never appears, and neutralises the show/cancel/dismiss
 * listeners.
 */
object DialogBlocking : Feature {
    override val key = ConfigKeys.FEATURE_DIALOG_BLOCK
    override val title = "弹窗拦截"
    override val desc = "拦掉 app 弹出的对话框（Dialog.show 不执行）"

    override fun apply(ctx: HookContext) {
        ctx.hookMethod("android.app.Dialog", "show") { null }
        ctx.hookMethod("android.app.Dialog", "setOnShowListener", DialogInterface.OnShowListener::class.java) { null }
        ctx.hookMethod("android.app.Dialog", "setOnCancelListener", DialogInterface.OnCancelListener::class.java) { null }
        ctx.hookMethod("android.app.Dialog", "setOnDismissListener", DialogInterface.OnDismissListener::class.java) { null }
        HookLog.i("DialogBlocking installed for ${ctx.packageName}")
    }
}
