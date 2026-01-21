/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.securestorage.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Colors for the Secure Storage Viewer feature.
 *
 * Uses a security-themed color scheme:
 * - Primary: Deep purple/indigo for security context
 * - Encrypted: Green tones for encrypted data
 * - Unencrypted: Amber tones for unencrypted data
 * - KeyStore: Blue tones for cryptographic keys
 * - DataStore: Teal tones for DataStore files
 */
@Immutable
data class SecureStorageColors(
    // Feature accent colors
    val primary: Color,
    val encrypted: Color,
    val unencrypted: Color,

    // Storage type colors
    val encryptedPrefs: Color,
    val keystore: Color,
    val datastore: Color,

    // Background colors
    val cardBackground: Color,
    val searchBackground: Color,
    val chipBackground: Color,
    val chipBackgroundSelected: Color,

    // Text colors
    val labelPrimary: Color,
    val labelSecondary: Color,
    val valuePrimary: Color,
    val valueSecondary: Color,
)

/**
 * Light theme secure storage colors.
 */
val LightSecureStorageColors = SecureStorageColors(
    // Feature accent colors
    primary = Color(0xFF5C6BC0),        // Indigo 400
    encrypted = Color(0xFF4CAF50),      // Green 500
    unencrypted = Color(0xFFFF9800),    // Orange 500

    // Storage type colors
    encryptedPrefs = Color(0xFF7C4DFF), // Deep Purple A200
    keystore = Color(0xFF2196F3),       // Blue 500
    datastore = Color(0xFF009688),      // Teal 500

    // Background colors
    cardBackground = Color(0xFFFAFAFA),
    searchBackground = Color(0xFFF5F5F5),
    chipBackground = Color(0xFFE8EAF6),
    chipBackgroundSelected = Color(0xFFC5CAE9),

    // Text colors
    labelPrimary = Color(0xFF212121),
    labelSecondary = Color(0xFF757575),
    valuePrimary = Color(0xFF424242),
    valueSecondary = Color(0xFF9E9E9E),
)

/**
 * Dark theme secure storage colors.
 */
val DarkSecureStorageColors = SecureStorageColors(
    // Feature accent colors
    primary = Color(0xFF7986CB),        // Indigo 300
    encrypted = Color(0xFF81C784),      // Green 300
    unencrypted = Color(0xFFFFB74D),    // Orange 300

    // Storage type colors
    encryptedPrefs = Color(0xFFB388FF), // Deep Purple A100
    keystore = Color(0xFF64B5F6),       // Blue 300
    datastore = Color(0xFF4DB6AC),      // Teal 300

    // Background colors
    cardBackground = Color(0xFF1E1E1E),
    searchBackground = Color(0xFF2D2D2D),
    chipBackground = Color(0xFF303F9F).copy(alpha = 0.2f),
    chipBackgroundSelected = Color(0xFF303F9F).copy(alpha = 0.4f),

    // Text colors
    labelPrimary = Color(0xFFE0E0E0),
    labelSecondary = Color(0xFF9E9E9E),
    valuePrimary = Color(0xFFBDBDBD),
    valueSecondary = Color(0xFF757575),
)

/**
 * Returns the appropriate secure storage colors based on the current theme.
 */
@Composable
fun secureStorageColors(darkTheme: Boolean = isSystemInDarkTheme()): SecureStorageColors {
    return if (darkTheme) DarkSecureStorageColors else LightSecureStorageColors
}
