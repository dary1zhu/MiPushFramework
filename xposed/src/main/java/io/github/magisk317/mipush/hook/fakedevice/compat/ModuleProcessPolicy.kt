package io.github.magisk317.mipush.hook.fakedevice.compat

object ModuleProcessPolicy {
    private val defaultDeniedProcessPrefixes = listOf(
        ":sandboxed_process",
        ":widgetProcess",
        ":webview",
        ":renderer",
        ":gpu",
        ":isolated",
    )

    fun shouldHandleProcess(packageName: String, processName: String): Boolean {
        // 🔥 强制提权：让第一入口对齐我们的动态拦截方法，防止原作者绕道走 getProfile()
        return shouldHandleProcess(ModuleCompatRegistry.resolveProfile(packageName, processName, null), packageName, processName)
    }

    internal fun shouldHandleProcess(
        profile: ModuleCompatProfile?,
        packageName: String,
        processName: String,
    ): Boolean {
        // ====================================================================
        // 🔥 降维打击：如果是抖音，强行给它的所有后台核心进程（特别是 :push）开闸放行！
        // ====================================================================
        if (packageName == "com.ss.android.ugc.aweme") {
            if (processName == packageName) return true // 放行主进程
            val suffix = processName.removePrefix(packageName)
            // 仅排除无意义的沙盒、渲染进程，其余子进程（:push等）一律死保放行！
            if (defaultDeniedProcessPrefixes.any { suffix.startsWith(it) }) {
                return false
            }
            return true 
        }
        // ====================================================================

        if (profile == null) {
            return false
        }
        if (profile.isAutoDetected) {
            return processName.isNotBlank()
        }
        if (processName == packageName) {
            return true
        }
        if (!processName.startsWith("$packageName:")) {
            return false
        }
        val suffix = processName.removePrefix(packageName)
        val deniedPrefixes = profile.deniedProcessPrefixes ?: defaultDeniedProcessPrefixes
        if (deniedPrefixes.any { suffix.startsWith(it) }) {
            return false
        }
        val allowedSuffixes = profile.allowedProcessSuffixes
        if (allowedSuffixes != null) {
            return suffix in allowedSuffixes
        }
        return true
    }
}
