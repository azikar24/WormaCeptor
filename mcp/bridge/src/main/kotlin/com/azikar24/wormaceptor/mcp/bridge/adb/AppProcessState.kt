package com.azikar24.wormaceptor.mcp.bridge.adb

internal enum class AppProcessState(val hint: String?) {
    Running(null),
    Frozen(
        "The app is frozen in the background. Bring it to the foreground (or call bring_app_to_front), then retry.",
    ),
    NotRunning("The app isn't running. Launch it (or call bring_app_to_front), then retry."),
}

internal fun appProcessState(
    pid: Int?,
    frozen: Boolean,
): AppProcessState = when {
    pid == null -> AppProcessState.NotRunning
    frozen -> AppProcessState.Frozen
    else -> AppProcessState.Running
}

/** `adb shell pidof <pkg>` prints space-separated pids, or nothing when the process is gone. */
internal fun parsePid(pidofOutput: String): Int? = pidofOutput.trim().split(Whitespace).firstOrNull()?.toIntOrNull()

/**
 * Reads `dumpsys activity processes <pkg>`: finds the block for the main process (named exactly
 * [packageName], not `pkg:remote`) and checks its `isFrozen=` flag (Android 14+ cached-app freezer).
 */
internal fun isMainProcessFrozen(
    dumpsys: String,
    packageName: String,
): Boolean {
    val header = Regex("""^\s*\*\w+\* .*ProcessRecord\{\S+ \d+:${Regex.escape(packageName)}/""")
    val block = dumpsys.lines()
        .dropWhile { !header.containsMatchIn(it) }
        .drop(1)
        .takeWhile { !ProcessHeader.containsMatchIn(it) && !it.trimStart().startsWith("PID mappings") }
    return block.any { FrozenFlag.containsMatchIn(it) }
}

/** `monkey` exits 0 even when it finds no launcher activity, so its output is checked too. */
internal fun launchFailure(
    exitCode: Int,
    output: String,
): String? {
    val aborted = output.lines().firstOrNull { it.contains("monkey aborted") }
    return when {
        aborted != null -> aborted.trim().removePrefix("**").trim()
        exitCode != 0 -> output.trim()
        else -> null
    }
}

private val Whitespace = Regex("\\s+")
private val ProcessHeader = Regex("""^\s*\*\w+\* """)
private val FrozenFlag = Regex("""\bisFrozen=true\b""")
