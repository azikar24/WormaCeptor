package com.azikar24.wormaceptor.mcp.server.serialization

import com.azikar24.wormaceptor.domain.entities.FileContent
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig.NetworkPreset
import com.azikar24.wormaceptor.mcp.server.serialization.dto.SetRateLimitRequestDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RequestMappersTest {

    private val current = RateLimitConfig(
        enabled = false,
        downloadSpeedKbps = 1000,
        uploadSpeedKbps = 500,
        latencyMs = 50,
        packetLossPercent = 0f,
        preset = null,
    )

    private fun SetRateLimitRequestDto.validConfig(): RateLimitConfig {
        val update = resolve(current)
        assertTrue(update is RateLimitUpdate.Valid, "expected Valid but was $update")
        return (update as RateLimitUpdate.Valid).config
    }

    @Test
    fun `enabled false disables and ignores other fields`() {
        val active = current.copy(enabled = true)
        val update = SetRateLimitRequestDto(enabled = false, preset = "EDGE").resolve(active)
        assertEquals(RateLimitUpdate.Valid(active.copy(enabled = false)), update)
    }

    @Test
    fun `preset is matched case-insensitively and replaces the config`() {
        val config = SetRateLimitRequestDto(preset = "slow_3g").validConfig()
        assertEquals(RateLimitConfig.fromPreset(NetworkPreset.SLOW_3G), config)
    }

    @Test
    fun `unknown preset is rejected with the list of valid presets`() {
        val update = SetRateLimitRequestDto(preset = "satellite").resolve(current)
        assertTrue(update is RateLimitUpdate.Invalid)
        val message = (update as RateLimitUpdate.Invalid).message
        assertTrue(message.contains("satellite"))
        NetworkPreset.entries.forEach { assertTrue(message.contains(it.name)) }
    }

    @Test
    fun `custom values merge onto current config and enable throttling`() {
        val config = SetRateLimitRequestDto(latencyMs = 300).validConfig()
        assertTrue(config.enabled)
        assertEquals(300L, config.latencyMs)
        assertEquals(current.downloadSpeedKbps, config.downloadSpeedKbps)
        assertEquals(current.uploadSpeedKbps, config.uploadSpeedKbps)
        assertNull(config.preset)
    }

    @Test
    fun `packet loss is clamped to 0-100 percent`() {
        assertEquals(100f, SetRateLimitRequestDto(packetLossPercent = 250f).validConfig().packetLossPercent)
        assertEquals(0f, SetRateLimitRequestDto(packetLossPercent = -5f).validConfig().packetLossPercent)
    }

    @Test
    fun `empty request enables current custom config`() {
        val config = SetRateLimitRequestDto().validConfig()
        assertEquals(current.copy(enabled = true), config)
    }

    @Test
    fun `text json and xml return content with a mime type`() {
        assertEquals("hello", FileContent.Text("hello").toReadFileDto().content)
        assertEquals("text/plain", FileContent.Text("hello").toReadFileDto().mimeType)
        val json = FileContent.Json(rawContent = "{}", formattedContent = "{ }", isValid = true).toReadFileDto()
        assertEquals("{ }", json.content)
        assertEquals("application/json", json.mimeType)
        val xml = FileContent.Xml(rawContent = "<a/>", formattedContent = "<a />", isValid = true).toReadFileDto()
        assertEquals("application/xml", xml.mimeType)
    }

    @Test
    fun `non-text content returns a summary instead of the bytes`() {
        val binary = FileContent.Binary(ByteArray(3)).toReadFileDto()
        assertEquals("[Binary content: 3 bytes]", binary.content)
        assertNull(binary.mimeType)

        val image = FileContent.Image(ByteArray(0), width = 4, height = 2, mimeType = "image/png").toReadFileDto()
        assertEquals("[Image: 4x2 image/png]", image.content)
        assertEquals("image/png", image.mimeType)

        val tooLarge = FileContent.TooLarge(sizeBytes = 10, maxSize = 5).toReadFileDto()
        assertEquals("[File too large: 10 bytes, max: 5]", tooLarge.content)
        assertFalse(tooLarge.content.isEmpty())
    }
}
