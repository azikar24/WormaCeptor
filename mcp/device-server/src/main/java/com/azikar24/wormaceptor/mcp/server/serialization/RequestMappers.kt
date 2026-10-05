package com.azikar24.wormaceptor.mcp.server.serialization

import com.azikar24.wormaceptor.domain.entities.FileContent
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig
import com.azikar24.wormaceptor.mcp.server.serialization.dto.ReadFileDto
import com.azikar24.wormaceptor.mcp.server.serialization.dto.SetRateLimitRequestDto

private const val MAX_PACKET_LOSS_PERCENT = 100f

/** Outcome of applying a [SetRateLimitRequestDto] to the current configuration. */
internal sealed class RateLimitUpdate {
    data class Valid(val config: RateLimitConfig) : RateLimitUpdate()
    data class Invalid(val message: String) : RateLimitUpdate()
}

/**
 * Resolves a partial rate-limit request against [current].
 *
 * Precedence: `enabled = false` disables throttling; otherwise a preset replaces the
 * whole configuration; otherwise custom values are merged onto [current] and enabled.
 */
internal fun SetRateLimitRequestDto.resolve(current: RateLimitConfig): RateLimitUpdate {
    if (enabled == false) return RateLimitUpdate.Valid(current.copy(enabled = false))

    if (preset != null) {
        val match = RateLimitConfig.NetworkPreset.entries.firstOrNull { it.name.equals(preset, ignoreCase = true) }
            ?: return RateLimitUpdate.Invalid(
                "Unknown preset '$preset'. Valid presets: " +
                    RateLimitConfig.NetworkPreset.entries.joinToString { it.name },
            )
        return RateLimitUpdate.Valid(RateLimitConfig.fromPreset(match))
    }

    return RateLimitUpdate.Valid(
        current.copy(
            enabled = true,
            downloadSpeedKbps = downloadSpeedKbps ?: current.downloadSpeedKbps,
            uploadSpeedKbps = uploadSpeedKbps ?: current.uploadSpeedKbps,
            latencyMs = latencyMs ?: current.latencyMs,
            packetLossPercent = (packetLossPercent ?: current.packetLossPercent)
                .coerceIn(0f, MAX_PACKET_LOSS_PERCENT),
            preset = null,
        ),
    )
}

/** Maps file content to the text payload returned by `/api/files/read`. */
internal fun FileContent.toReadFileDto(): ReadFileDto = when (this) {
    is FileContent.Text -> ReadFileDto(content, "text/plain")
    is FileContent.Json -> ReadFileDto(formattedContent, "application/json")
    is FileContent.Xml -> ReadFileDto(formattedContent, "application/xml")
    is FileContent.Binary -> ReadFileDto("[Binary content: $displaySize bytes]", null)
    is FileContent.Image -> ReadFileDto("[Image: ${width}x$height $mimeType]", mimeType)
    is FileContent.Pdf -> ReadFileDto("[PDF: $pageCount pages, $sizeBytes bytes]", "application/pdf")
    is FileContent.TooLarge -> ReadFileDto("[File too large: $sizeBytes bytes, max: $maxSize]", null)
    is FileContent.Error -> ReadFileDto("[Error: $message]", null)
}
