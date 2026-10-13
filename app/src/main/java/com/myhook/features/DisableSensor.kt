package com.myhook.features

import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.myhook.core.ConfigKeys
import com.myhook.core.HookContext
import com.myhook.core.HookLog

/**
 * Hooks every `SensorManager.registerListener` overload so the app never receives sensor events,
 * and blanks `getDefaultSensor` / `getSensorList`.
 */
object DisableSensor : Feature {
    override val key = ConfigKeys.FEATURE_SENSOR
    override val title = "禁用传感器"
    override val desc = "让 app 收不到传感器数据（陀螺仪/加速度计等）"

    override fun apply(ctx: HookContext) {
        val intT = Int::class.java
        ctx.hookMethod(
            "android.hardware.SensorManager", "registerListener",
            SensorEventListener::class.java, Sensor::class.java, intT,
        ) { false }
        ctx.hookMethod(
            "android.hardware.SensorManager", "registerListener",
            SensorEventListener::class.java, Sensor::class.java, intT, android.os.Handler::class.java,
        ) { false }
        ctx.hookMethod(
            "android.hardware.SensorManager", "registerListener",
            SensorEventListener::class.java, Sensor::class.java, intT, intT,
        ) { false }
        ctx.hookMethod(
            "android.hardware.SensorManager", "registerListener",
            SensorEventListener::class.java, Sensor::class.java, intT, intT, android.os.Handler::class.java,
        ) { false }

        ctx.hookMethod("android.hardware.SensorManager", "getDefaultSensor", intT) { null }
        ctx.hookMethod("android.hardware.SensorManager", "getDefaultSensor", intT, Boolean::class.java) { null }
        ctx.hookMethod("android.hardware.SensorManager", "getSensorList", intT) { emptyList<Sensor>() }
        HookLog.i("DisableSensor installed for ${ctx.packageName}")
    }
}
