package com.myhook.hooks

import com.myhook.core.HookContext

/**
 * Thanox (github.tornaco.android.thanos) — 解锁高级版。
 *
 * Hook:
 *   CommonApiResWrapper#getResult() -> 0
 *   CommonApiResWrapper#getK()      -> "7F8C..."   (f10977)
 *   CommonApiResWrapper#getMsg()    -> {"validation":"Valid", ...} (f10976)
 *
 * runtime dex search is not reproduced here.
 */
object Thanox : AppHook {
    override val packages = setOf("github.tornaco.android.thanos")

    private const val CLS =
        "github.tornaco.android.thanos.support.subscribe.CommonApiResWrapper"

    private const val K =
        "7F8CDE6C281FD3DA19809ADCD3BB409BCAD0877A1F3870BE274F6C49B3E31A0C" +
            "6728957F27CB2E71DB775E1B9461C229295CB905199C74BA8FF7956A7D97195E" +
            "9FB1E4CE45085A27371CA2ABE08C6112C02009A7"

    private const val MSG =
        "{\"validation\":\"Valid\",\"remainingHours\":48661800," +
            "\"remainingMillis\":175182480000000}"

    override fun apply(ctx: HookContext) {
        ctx.hookMethod(CLS, "getResult") { 0 }
        ctx.hookMethod(CLS, "getK") { K }
        ctx.hookMethod(CLS, "getMsg") { MSG }
    }
}
