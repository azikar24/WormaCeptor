package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Visual variant for [WormaCeptorButton].
 */
enum class ButtonVariant {
    /** High-emphasis filled button for primary actions. */
    Primary,

    /** Medium-emphasis filled button with subtle background. */
    Secondary,

    /** Red-toned button for destructive actions (delete, clear). */
    Destructive,

    /** Button with border and no fill for secondary actions. */
    Outlined,

    /** Minimal button with no background for tertiary actions (cancel, dismiss). */
    Text,
}

/**
 * Unified button component for WormaCeptor.
 *
 * Provides consistent styling across all features with predefined variants.
 * Supports an optional loading state and leading icon.
 *
 * @param text Button label
 * @param onClick Click callback
 * @param modifier Modifier for the button
 * @param variant Visual style variant
 * @param enabled Whether the button is interactive
 * @param loading When true, shows a progress indicator and disables interaction
 * @param containerColor Optional override for the container color (for feature-specific buttons)
 * @param contentColor Optional override for the content color
 * @param leadingIcon Optional composable displayed before the text
 */
@Composable
fun WormaCeptorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color? = null,
    contentColor: Color? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val isEnabled = enabled && !loading

    when (variant) {
        ButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = isEnabled,
                shape = WormaCeptorDesignSystem.Shapes.button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor ?: MaterialTheme.colorScheme.primary,
                    contentColor = contentColor ?: MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                ButtonContent(text = text, loading = loading, leadingIcon = leadingIcon)
            }
        }

        ButtonVariant.Secondary -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = isEnabled,
                shape = WormaCeptorDesignSystem.Shapes.button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor
                        ?: MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = contentColor ?: MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                ButtonContent(text = text, loading = loading, leadingIcon = leadingIcon)
            }
        }

        ButtonVariant.Destructive -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = isEnabled,
                shape = WormaCeptorDesignSystem.Shapes.button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor ?: MaterialTheme.colorScheme.error,
                    contentColor = contentColor ?: MaterialTheme.colorScheme.onError,
                ),
            ) {
                ButtonContent(text = text, loading = loading, leadingIcon = leadingIcon)
            }
        }

        ButtonVariant.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier,
                enabled = isEnabled,
                shape = WormaCeptorDesignSystem.Shapes.button,
                border = BorderStroke(
                    width = WormaCeptorDesignSystem.BorderWidth.regular,
                    color = if (isEnabled) {
                        containerColor ?: MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.outline.copy(
                            alpha = WormaCeptorDesignSystem.Alpha.MODERATE,
                        )
                    },
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = contentColor ?: MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                ButtonContent(text = text, loading = loading, leadingIcon = leadingIcon)
            }
        }

        ButtonVariant.Text -> {
            TextButton(
                onClick = onClick,
                modifier = modifier,
                enabled = isEnabled,
                shape = WormaCeptorDesignSystem.Shapes.button,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = contentColor ?: MaterialTheme.colorScheme.primary,
                ),
                contentPadding = PaddingValues(
                    horizontal = WormaCeptorDesignSystem.Spacing.md,
                    vertical = WormaCeptorDesignSystem.Spacing.sm,
                ),
            ) {
                ButtonContent(text = text, loading = loading, leadingIcon = leadingIcon)
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    loading: Boolean,
    leadingIcon: (@Composable () -> Unit)?,
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
            strokeWidth = WormaCeptorDesignSystem.BorderWidth.thick,
        )
        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
    } else if (leadingIcon != null) {
        leadingIcon()
        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
    }
    Text(text = text)
}
