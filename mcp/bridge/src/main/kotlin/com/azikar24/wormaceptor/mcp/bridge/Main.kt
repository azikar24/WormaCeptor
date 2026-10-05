package com.azikar24.wormaceptor.mcp.bridge

import com.azikar24.wormaceptor.mcp.bridge.adb.AdbClient
import com.azikar24.wormaceptor.mcp.bridge.adb.DeviceDiscovery
import com.azikar24.wormaceptor.mcp.bridge.config.BridgeConfig
import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import com.azikar24.wormaceptor.mcp.bridge.mcp.McpServer
import com.azikar24.wormaceptor.mcp.bridge.mcp.tools.ToolRegistry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    val config = BridgeConfig.fromArgs(args)

    System.err.println("WormaCeptor MCP Bridge v1.0.0")
    if (config.verbose) {
        System.err.println("Config: port=${config.port}, device=${config.deviceSerial ?: "auto"}")
    }

    runBlocking {
        val adbClient = AdbClient(config.adbPath)

        val device = try {
            DeviceDiscovery(adbClient).findDevice(config.deviceSerial)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            System.err.println("Error: ${e.message}")
            kotlin.system.exitProcess(1)
        }
        System.err.println("Found device: ${device.serial} (${device.model ?: "unknown"})")

        if (!adbClient.forwardPort(device.serial, config.port, config.port)) {
            System.err.println("Error: Failed to set up ADB port forwarding")
            kotlin.system.exitProcess(1)
        }
        System.err.println("Port forwarded: localhost:${config.port} -> device:${config.port}")

        val connection = DeviceConnection(config, adbClient)
        try {
            connection.connect()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            System.err.println("Error: ${e.message}")
            kotlin.system.exitProcess(1)
        }
        System.err.println("Connected to WormaCeptor server")

        Runtime.getRuntime().addShutdownHook(
            Thread {
                System.err.println("Shutting down...")
                connection.close()
            },
        )

        System.err.println("MCP server ready. Listening for requests on stdin...")
        val mcpServer = McpServer(connection, ToolRegistry.allTools(), config.verbose)
        mcpServer.run()
    }
}
