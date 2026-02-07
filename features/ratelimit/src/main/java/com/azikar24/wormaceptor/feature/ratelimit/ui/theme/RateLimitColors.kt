package com.azikar24.wormaceptor.feature.ratelimit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Colors for the Network Rate Limiting feature.
 *
 * Uses a network-themed color scheme:
 * - Primary: Purple tones for network/connectivity
 * - Download: Blue tones
 * - Upload: Green tones
 * - Latency: Orange/Amber tones for delay indication
 * - PacketLoss: Red tones for dropped packets
 */
@Immutable
data class RateLimitColors(
    // Feature accent colors
    val primary: Color,
    val download: Color,
    val upload: Color,
    val latency: Color,
    val packetLoss: Color,

    // Status colors
    val enabled: Color,
    val disabled: Color,

    // Preset badge colors
    val presetWifi: Color,
    val preset3G: Color,
    val preset2G: Color,
    val presetEdge: Color,
    val presetOffline: Color,

    // Background colors
    val cardBackground: Color,
    val sliderTrack: Color,
    val sliderThumb: Color,

    // Text colors
    val labelPrimary: Color,
    val labelSecondary: Color,
    val valuePrimary: Color,
)

/**
 * Light theme rate limit colors.
 */
val LightRateLimitColors = RateLimitColors(
    // Feature accent colors
    primary = Color(0xFF7C4DFF), // Deep Purple A200
    download = Color(0xFF2196F3), // Blue 500
    upload = Color(0xFF4CAF50), // Green 500
    latency = Color(0xFFFF9800), // Orange 500
    packetLoss = Color(0xFFF44336), // Red 500

    // Status colors
    enabled = Color(0xFF4CAF50), // Green 500
    disabled = Color(0xFF9E9E9E), // Grey 500

    // Preset badge colors
    presetWifi = Color(0xFF4CAF50), // Green
    preset3G = Color(0xFF2196F3), // Blue
    preset2G = Color(0xFFFF9800), // Orange
    presetEdge = Color(0xFFFF5722), // Deep Orange
    presetOffline = Color(0xFFF44336), // Red

    // Background colors
    cardBackground = Color(0xFFFAFAFA),
    sliderTrack = Color(0xFFE0E0E0),
    sliderThumb = Color(0xFF7C4DFF),

    // Text colors
    labelPrimary = Color(0xFF212121),
    labelSecondary = Color(0xFF757575),
    valuePrimary = Color(0xFF424242),
)

/**
 * Dark theme rate limit colors.
 */
val DarkRateLimitColors = RateLimitColors(
    // Feature accent colors
    primary = Color(0xFFB388FF), // Deep Purple A100
    download = Color(0xFF64B5F6), // Blue 300
    upload = Color(0xFF81C784), // Green 300
    latency = Color(0xFFFFB74D), // Orange 300
    packetLoss = Color(0xFFE57373), // Red 300

    // Status colors
    enabled = Color(0xFF81C784), // Green 300
    disabled = Color(0xFF757575), // Grey 600

    // Preset badge colors
    presetWifi = Color(0xFF81C784), // Green 300
    preset3G = Color(0xFF64B5F6), // Blue 300
    preset2G = Color(0xFFFFB74D), // Orange 300
    presetEdge = Color(0xFFFF8A65), // Deep Orange 300
    presetOffline = Color(0xFFE57373), // Red 300

    // Background colors
    cardBackground = Color(0xFF1E1E1E),
    sliderTrack = Color(0xFF424242),
    sliderThumb = Color(0xFFB388FF),

    // Text colors
    labelPrimary = Color(0xFFE0E0E0),
    labelSecondary = Color(0xFF9E9E9E),
    valuePrimary = Color(0xFFBDBDBD),
)

/**
 * Returns the appropriate rate limit colors based on the current theme.
 */
@Composable
fun rateLimitColors(darkTheme: Boolean = isSystemInDarkTheme()): RateLimitColors {
    return if (darkTheme) DarkRateLimitColors else LightRateLimitColors
}
