package com.azikar24.wormaceptor.mcp.bridge.adb

import java.io.File

/**
 * Finds adb: the `--adb` flag, `$ANDROID_HOME`, `$ANDROID_SDK_ROOT`, `PATH`, then the SDK dir
 * Android Studio installs to by default. Falls back to the bare executable name, so starting it
 * fails with the OS's "not found" error.
 */
internal fun resolveAdbPath(
    flag: String?,
    env: (String) -> String?,
    isFile: (String) -> Boolean,
    osName: String,
    userHome: String,
): String {
    if (flag != null) return flag
    val windows = osName.startsWith("Windows", ignoreCase = true)
    val separator = if (windows) "\\" else "/"
    val executable = if (windows) "adb.exe" else "adb"
    fun inSdk(sdk: String) = listOf(sdk, "platform-tools", executable).joinToString(separator)

    val pathDirs = env("PATH").orEmpty().split(if (windows) ';' else ':').filter { it.isNotBlank() }
    val defaultSdk = when {
        windows -> env("LOCALAPPDATA")?.let { "$it\\Android\\Sdk" }
        osName.startsWith("Mac", ignoreCase = true) -> "$userHome/Library/Android/sdk"
        else -> "$userHome/Android/Sdk"
    }
    val candidates = listOfNotNull(env("ANDROID_HOME"), env("ANDROID_SDK_ROOT")).map(::inSdk) +
        pathDirs.map { "$it$separator$executable" } +
        listOfNotNull(defaultSdk?.let(::inSdk))
    return candidates.firstOrNull(isFile) ?: executable
}

internal fun resolveAdbPath(flag: String?): String = resolveAdbPath(
    flag = flag,
    env = System::getenv,
    isFile = { File(it).isFile },
    osName = System.getProperty("os.name"),
    userHome = System.getProperty("user.home"),
)
