package com.myhook.hooks

import com.myhook.core.HookContext
import com.myhook.core.HookLog
import java.lang.reflect.Modifier

/**
 * 豆包 (com.larus.nova) — 无水印保存图片。
 *
 * Hook:
 *   com.larus.im.bean.message.block.BlockContent#getCreationBlock() 后置:
 *       遍历 creations，对 type==1 且 image.status==2 的项，取 image.imageOriRaw 的
 *       原始 url/width/height，再用该原始 url 覆盖 image 里所有同尺寸子对象的 url
 *       （原图 URL 无出水印；urlFormats.heic 同步覆盖）。
 */
object Doubao : AppHook {
    override val packages = setOf("com.larus.nova")

    override fun apply(ctx: HookContext) {
        ctx.hookMethod(
            "com.larus.im.bean.message.block.BlockContent",
            "getCreationBlock",
        ) { chain ->
            val r = chain.proceed()
            runCatching { stripWatermark(ctx, r) }
                .onFailure { HookLog.w("doubao strip failed: $it") }
            r
        }
    }

    private fun stripWatermark(ctx: HookContext, result: Any?) {
        val creations = ctx.getField(result, "creations") as? List<*> ?: return
        for (c in creations) {
            val type = (ctx.getField(c, "type") as? Number)?.toInt() ?: continue
            if (type != 1) continue
            val image = ctx.getField(c, "image") ?: continue
            if ((ctx.getField(image, "status") as? Number)?.toInt() != 2) continue
            val ori = ctx.getField(image, "imageOriRaw") ?: continue
            val url = ctx.getField(ori, "url") as? String ?: continue
            val w = (ctx.getField(ori, "width") as? Number)?.toInt() ?: continue
            val h = (ctx.getField(ori, "height") as? Number)?.toInt() ?: continue
            for (node in walk(image, 0)) {
                if (node === image) continue
                val nw = (ctx.getField(node, "width") as? Number)?.toInt()
                val nh = (ctx.getField(node, "height") as? Number)?.toInt()
                if (nw == w && nh == h) {
                    ctx.setField(node, "url", url)
                    val fm = ctx.getField(node, "urlFormats")
                    if (fm is MutableMap<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        (fm as MutableMap<Any?, Any?>)["heic"] = url
                    }
                }
            }
        }
    }

    /** Bounded reflective walk of an object's non-static, non-primitive fields (depth ≤ 3). */
    private fun walk(root: Any?, depth: Int): List<Any> {
        if (root == null || depth > 3) return emptyList()
        val out = ArrayList<Any>()
        val seen = HashSet<Int>()
        var c: Class<*>? = root.javaClass
        while (c != null && c != Any::class.java) {
            for (f in c.declaredFields) {
                if (Modifier.isStatic(f.modifiers)) continue
                if (f.type.isPrimitive) continue
                f.isAccessible = true
                val v = runCatching { f.get(root) }.getOrNull() ?: continue
                if (v is String || v is Number || v is Boolean) continue
                if (!seen.add(System.identityHashCode(v))) continue
                out.add(v)
                if (v !is Collection<*> && !v.javaClass.isArray) {
                    out.addAll(walk(v, depth + 1))
                }
            }
            c = c.superclass
        }
        return out
    }
}
