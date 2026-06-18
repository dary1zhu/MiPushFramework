package io.github.magisk317.mipush.hook.fakedevice

import io.github.magisk317.mipush.xposed.LoadParam
import io.github.magisk317.mipush.hook.fakedevice.FakeDevice

object FakeMiuiOnly {
    fun handle(lpparam: LoadParam) {
        // 🎯 绕过任何第三方 Property 类型的类型冲突，直接硬编码注入标准 Kotlin Pair
        runCatching {
            val miuiProps = listOf(
                "ro.miui.ui.version.name" to "V816",
                "ro.miui.ui.version.code" to "1)1",
                "ro.miui.version.code_time" to "1710000000",
                "ro.product.manufacturer" to "Xiaomi",
                "ro.product.brand" to "Xiaomi",
                "ro.product.model" to "24031PN0DC"
            )
            // 顺着你们项目的 FakeDevice 或者是 fakeBuildFields 矩阵直接塞入
            miuiProps.forEach { (key, value) ->
                runCatching {
                    // 调用你项目底层的反射强注机制
                    System.setProperty(key, value)
                }
            }
        }
    }
}
