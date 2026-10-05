package com.azikar24.wormaceptor.mcp.bridge.adb

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

internal data class CommandResult(
    val exitCode: Int,
    val output: String,
    val error: String,
)

internal data class DeviceInfo(
    val serial: String,
    val state: String,
    val model: String?,
    val product: String?,
)

internal class AdbClient(private val adbPath: String = "adb") {

    fun listDevices(): List<DeviceInfo> {
        val result = runCommand(listOf(adbPath, "devices", "-l"))
        if (result.exitCode != 0) return emptyList()

        return result.output.lines()
            .drop(1)
            .filter { it.isNotBlank() && !it.startsWith("*") }
            .mapNotNull { line ->
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.size < 2) return@mapNotNull null
                val serial = parts[0]
                val state = parts[1]
                val model = parts.find { it.startsWith("model:") }?.removePrefix("model:")
                val product = parts.find { it.startsWith("product:") }?.removePrefix("product:")
                DeviceInfo(serial, state, model, product)
            }
    }

    fun forwardPort(
        serial: String?,
        localPort: Int,
        remotePort: Int,
    ): Boolean {
        val args = buildList {
            add(adbPath)
            if (serial != null) {
                add("-s")
                add(serial)
            }
            add("forward")
            add("tcp:$localPort")
            add("tcp:$remotePort")
        }
        return runCommand(args).exitCode == 0
    }

    fun removeForward(localPort: Int): Boolean {
        return runCommand(listOf(adbPath, "forward", "--remove", "tcp:$localPort")).exitCode == 0
    }

    private fun runCommand(
        args: List<String>,
        timeoutSeconds: Long = COMMAND_TIMEOUT_SECONDS,
    ): CommandResult {
        var process: Process? = null
        return try {
            process = ProcessBuilder(args)
                .redirectErrorStream(false)
                .start()

            val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val error = BufferedReader(InputStreamReader(process.errorStream)).readText()
            val completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)

            if (!completed) {
                CommandResult(-1, "", "Command timed out")
            } else {
                CommandResult(process.exitValue(), output, error)
            }
        } catch (e: Exception) {
            CommandResult(-1, "", e.message ?: "Unknown error")
        } finally {
            process?.destroyForcibly()
        }
    }

    companion object {
        internal const val COMMAND_TIMEOUT_SECONDS = 10L
    }
}
