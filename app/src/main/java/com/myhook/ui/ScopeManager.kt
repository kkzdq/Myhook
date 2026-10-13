package com.myhook.ui

import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper

/**
 * Bridges the module app to the framework's IXposedService.
 *
 * This is what powers requirement #3: the user ticks apps in the UI and the framework shows a
 * notification to confirm adding them to the scope — no need to reopen the LSPosed manager.
 */
object ScopeManager {

    @Volatile
    var service: XposedService? = null
        private set

    private var registered = false

    fun ensureRegistered() {
        if (registered) return
        registered = true
        XposedServiceHelper.registerListener(object : XposedServiceHelper.OnServiceListener {
            override fun onServiceBind(service: XposedService) {
                this@ScopeManager.service = service
            }

            override fun onServiceDied(service: XposedService) {
                if (this@ScopeManager.service === service) this@ScopeManager.service = null
            }
        })
    }

    fun currentScope(): List<String> = try {
        service?.scope ?: emptyList()
    } catch (t: Throwable) {
        emptyList()
    }

    fun requestScope(packages: List<String>, onResult: (Boolean, String) -> Unit) {
        val svc = service
        if (svc == null) {
            onResult(false, "尚未连接 LSPosed 服务，请稍后重试")
            return
        }
        try {
            svc.requestScope(packages, object : XposedService.OnScopeEventListener {
                override fun onScopeRequestApproved(approved: List<String>) {
                    onResult(true, "已加入作用域: $approved")
                }

                override fun onScopeRequestFailed(message: String) {
                    onResult(false, message)
                }
            })
        } catch (t: Throwable) {
            onResult(false, "请求失败: $t")
        }
    }
}
