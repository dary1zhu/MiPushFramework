pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        // 🎯 修复卡点：给插件管理层补全 Detekt 专属快照源
        maven { url = java.net.URI("https://plugin-markers.detekt.dev/") }
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.greenrobot.greendao") {
                useModule("org.greenrobot:greendao-gradle-plugin:${requested.version}")
            }
        }
    }
}

fun requireExistingProjectDir(path: String) {
    val dir = file(path)
    check(dir.isDirectory) {
        buildString {
            appendLine("Missing required project directory: $path")
            appendLine("This repository uses git submodules. Run:")
            appendLine("  git submodule update --init --recursive")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 🎯 修复卡点：给全局项目依赖层补全 Detekt 专属快照源，彻底斩断 404 崩盘
        maven { url = java.net.URI("https://plugin-markers.detekt.dev/") }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "MiPushFramework"
requireExistingProjectDir("magisk-ui-kit")
requireExistingProjectDir("vendor")
requireExistingProjectDir("pinned")
include(":xmsf", ":mipush", ":xposed", ":common", ":core", ":settings", ":magisk-ui-kit", ":vendor", ":pinned", ":manager", ":app")
