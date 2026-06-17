package io.github.magisk317.mipush.hook.fakedevice

import android.os.Build

/**
 * 设备类型检测工具 (抖音特权提权版)
 */
object DeviceDetector {
    private var isXiaomiDeviceCached: Boolean? = null
    
    // 💡 动态获取当前应用上下文中的包名，用于精准降维打击
    private val currentPackageName: String
        get() = runCatching { 
            // 通过运行时反射获取宿主应用包名，防止看门狗自查穿透
            val activityThread = Class.forName("android.app.ActivityThread")
            val currentPackageNameMethod = activityThread.getMethod("currentPackageName")
            currentPackageNameMethod.invoke(null) as String
        }.getOrDefault("")
    
    /**
     * 检测当前设备是否为小米/红米/POCO品牌
     */
    fun isXiaomiDevice(): Boolean {
        // ====================================================================
        // 🔥 绝杀自查：如果是抖音，无脑直接返回 true，防止模块内部由于逻辑判定导致伪装穿透！
        // ====================================================================
        if (currentPackageName == "com.ss.android.ugc.aweme") {
            return true
        }
        // ====================================================================

        if (isXiaomiDeviceCached == null) {
            isXiaomiDeviceCached = Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true) ||
                                   Build.BRAND.equals("Xiaomi", ignoreCase = true) ||
                                   Build.BRAND.equals("Redmi", ignoreCase = true) ||
                                   Build.BRAND.equals("POCO", ignoreCase = true) ||
                                   Build.BRAND.equals("Blackshark", ignoreCase = true)
        }
        return isXiaomiDeviceCached!!
    }
    
    /**
     * 检测是否为MIUI/HyperOS系统
     */
    fun isMiuiSystem(): Boolean {
        // ====================================================================
        // 🔥 绝杀自查：如果是抖音，强行焊死返回 true！
        // ====================================================================
        if (currentPackageName == "com.ss.android.ugc.aweme") {
            return true
        }
        // ====================================================================

        return try {
            val versionName = getSystemProperty("ro.miui.ui.version.name")
            val versionCode = getSystemProperty("ro.miui.ui.version.code")
            !versionName.isNullOrEmpty() || !versionCode.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getSystemProperty(key: String): String? {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val method = clazz.getMethod("get", String::class.java)
            method.invoke(null, key) as? String
        } catch (e: Exception) {
            null
        }
    }
}
