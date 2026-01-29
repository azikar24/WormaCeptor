/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.securestorage.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Colors for the Secure Storage Viewer feature.
 * Uses centralized colors from WormaCeptorColors.SecureStorage.
 */
@Immutable
data class SecureStorageColors(
    val primary: Color,
    val encrypted: Color,
    val unencrypted: Color,
    val encryptedPrefs: Color,
    val keystore: Color,
    val datastore: Color,
    val cardBackground: Color,
    val searchBackground: Color,
    val chipBackground: Color,
    val chipBackgroundSelected: Color,
    val labelPrimary: Color,
    val labelSecondary: Color,
    val valuePrimary: Color,
    val valueSecondary: Color,
)

/**
 * Returns the appropriate secure storage colors based on the current theme.
 */
@Composable
fun secureStorageColors(darkTheme: Boolean = isSystemInDarkTheme()): SecureStorageColors {
    val alpha = WormaCeptorDesignSystem.Alpha
    val surface = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    return SecureStorageColors(
        primary = WormaCeptorColors.SecureStorage.Primary,
        encrypted = WormaCeptorColors.SecureStorage.Encrypted,
        unencrypted = WormaCeptorColors.SecureStorage.Unencrypted,
        encryptedPrefs = WormaCeptorColors.SecureStorage.EncryptedPrefs,
        keystore = WormaCeptorColors.SecureStorage.Keystore,
        datastore = WormaCeptorColors.SecureStorage.Datastore,
        cardBackground = surface,
        searchBackground = surfaceVariant,
        chipBackground = WormaCeptorColors.SecureStorage.Primary.copy(alpha = alpha.subtle),
        chipBackgroundSelected = WormaCeptorColors.SecureStorage.Primary.copy(alpha = alpha.light),
        labelPrimary = onSurface,
        labelSecondary = onSurfaceVariant,
        valuePrimary = onSurface.copy(alpha = 0.87f),
        valueSecondary = onSurfaceVariant.copy(alpha = 0.6f),
    )
}
