package com.myhook.hooks

import com.myhook.core.HookContext
import java.lang.reflect.Method

/**
 * 菜鸟 (com.cainiao.wireless) — 去除开屏广告 / 精简广告布局。
 *
 * Hook:
 * reproduced):
 *   HomePageFragment 里名字同时含 "init" 和 "RecommendView" 的方法 -> 返回 null
 *   NavigationSupport 构造: 只保留 key ∈ {homepage, im, personal_center} 的项
 *   LogisticDetailNewRecycleAdapter#setData(List, List): 移除 getIndex() == 50 的项
 */
object Cainiao : AppHook {
    override val packages = setOf("com.cainiao.wireless")

    private val KEEP_KEYS = setOf("homepage", "im", "personal_center")

    override fun apply(ctx: HookContext) {
        ctx.hookAllMethods(
            "com.cainiao.wireless.homepage.view.fragment.HomePageFragment",
            { m: Method -> m.name.contains("init") && m.name.contains("RecommendView") },
        ) { null }

        ctx.hookAllConstructors("com.cainiao.commonlibrary.navigation.NavigationSupport") { chain ->
            chain.args.forEach { a ->
                if (a is MutableList<*>) {
                    @Suppress("UNCHECKED_CAST")
                    (a as MutableList<Any?>).removeAll { item -> keyOf(item) !in KEEP_KEYS }
                }
            }
            chain.proceed()
        }

        ctx.hookMethod(
            "com.taobao.cainiao.logistic.ui.newview.LogisticDetailNewRecycleAdapter",
            "setData", List::class.java, List::class.java,
        ) { chain ->
            val a0 = chain.args.getOrNull(0) as? MutableList<Any?>
            val a1 = chain.args.getOrNull(1) as? MutableList<Any?>
            if (a0 != null && a1 != null) {
                var i = a1.size - 1
                while (i >= 0) {
                    val idx = runCatching {
                        a1[i]!!.javaClass.getMethod("getIndex").invoke(a1[i]) as Int
                    }.getOrDefault(-1)
                    if (idx == 50) {
                        runCatching { a1.removeAt(i) }
                        runCatching { if (i < a0.size) a0.removeAt(i) }
                    }
                    i--
                }
            }
            chain.proceed()
        }
    }

    private fun keyOf(item: Any?): String? = runCatching {
        item!!.javaClass.getMethod("getKey").invoke(item) as? String
    }.getOrNull()
}
