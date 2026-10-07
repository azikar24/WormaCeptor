package com.azikar24.wormaceptor.mcp.server.serialization

import com.azikar24.wormaceptor.core.engine.PushSimulatorEngine
import com.azikar24.wormaceptor.mcp.protocol.MockLocationDto
import com.azikar24.wormaceptor.mcp.protocol.SimulatedNotificationDto
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/** The exact bodies the bridge sends must decode; missing fields used to fail the whole call. */
class BridgeRequestDecodingTest {

    private val json = JsonConfig.instance

    @Test
    fun `push body with only title, body and priority decodes`() {
        val dto = json.decodeFromString<SimulatedNotificationDto>(
            """{"title":"Order Update","body":"Shipped","priority":"high"}""",
        )
        dto.title shouldBe "Order Update"
        dto.priority shouldBe "high"
        dto.id.isNotBlank() shouldBe true
    }

    @Test
    fun `push body without channelId targets the engine's default channel`() {
        val dto = json.decodeFromString<SimulatedNotificationDto>("""{"title":"t","body":"b"}""")
        dto.channelId shouldBe PushSimulatorEngine.DEFAULT_CHANNEL_ID
    }

    @Test
    fun `location body with only coordinates and name decodes`() {
        val dto = json.decodeFromString<MockLocationDto>("""{"latitude":48.8584,"longitude":2.2945,"name":"Eiffel"}""")
        dto.latitude shouldBe 48.8584
        dto.accuracy shouldBe 1.0f
        dto.name shouldBe "Eiffel"
    }
}
