package com.azikar24.wormaceptor.mcp.bridge.adb

import org.junit.Assert.assertEquals
import org.junit.Test

class McpPackagesTest {

    // Captured from `adb shell dumpsys package providers` on an API 36 emulator.
    private val dump = """
        |    Provider{dcf72cd com.google.android.apps.safetyhub/.common.debug.DebugProvider}
        |  com.azikar24.wormaceptorapp/com.azikar24.wormaceptor.mcp.server.WormaCeptorServerInitializer:
        |    Provider{ddc8782 com.azikar24.wormaceptorapp/com.azikar24.wormaceptor.mcp.server.WormaCeptorServerInitializer}
        |  com.heytap.browser/com.cdo.oaps.api.host.CallbackProvider:
        |  [com.azikar24.wormaceptorapp.wormaceptor.mcp]:
        |    Provider{ddc8782 com.azikar24.wormaceptorapp/com.azikar24.wormaceptor.mcp.server.WormaCeptorServerInitializer}
    """.trimMargin()

    @Test
    fun `finds the app that registers the MCP server once`() {
        assertEquals(listOf("com.azikar24.wormaceptorapp"), parseMcpPackages(dump))
    }

    @Test
    fun `lists every app when several include the server`() {
        val two = dump + "\n  com.example.shop/com.azikar24.wormaceptor.mcp.server.WormaCeptorServerInitializer:"
        assertEquals(listOf("com.azikar24.wormaceptorapp", "com.example.shop"), parseMcpPackages(two))
    }
}
