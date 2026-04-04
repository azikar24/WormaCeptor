package com.azikar24.wormaceptor.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

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
                shape = WormaCeptorTokens.Shapes.button,
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
                shape = WormaCeptorTokens.Shapes.button,
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
                shape = WormaCeptorTokens.Shapes.button,
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
                shape = WormaCeptorTokens.Shapes.button,
                border = BorderStroke(
                    width = WormaCeptorTokens.BorderWidth.regular,
                    color = if (isEnabled) {
                        containerColor ?: MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.outline.copy(
                            alpha = WormaCeptorTokens.Alpha.MODERATE,
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
                shape = WormaCeptorTokens.Shapes.button,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = contentColor ?: MaterialTheme.colorScheme.primary,
                ),
                contentPadding = PaddingValues(
                    horizontal = WormaCeptorTokens.Spacing.md,
                    vertical = WormaCeptorTokens.Spacing.sm,
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
            modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
            strokeWidth = WormaCeptorTokens.BorderWidth.thick,
        )
        Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))
    } else if (leadingIcon != null) {
        leadingIcon()
        Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))
    }
    Text(text = text)
}

@Preview(name = "Buttons - Light")
@Composable
private fun WormaCeptorButtonPreview() {
    WormaCeptorTheme {
        Surface {
            Column(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                WormaCeptorButton(text = "Primary", onClick = {})
                WormaCeptorButton(text = "Secondary", onClick = {}, variant = ButtonVariant.Secondary)
                WormaCeptorButton(text = "Destructive", onClick = {}, variant = ButtonVariant.Destructive)
                WormaCeptorButton(text = "Outlined", onClick = {}, variant = ButtonVariant.Outlined)
                WormaCeptorButton(text = "Text", onClick = {}, variant = ButtonVariant.Text)
                WormaCeptorButton(text = "Loading", onClick = {}, loading = true)
                WormaCeptorButton(text = "Disabled", onClick = {}, enabled = false)
            }
        }
    }
}

@Preview(name = "Buttons - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WormaCeptorButtonDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            Column(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                WormaCeptorButton(text = "Primary", onClick = {})
                WormaCeptorButton(text = "Secondary", onClick = {}, variant = ButtonVariant.Secondary)
                WormaCeptorButton(text = "Destructive", onClick = {}, variant = ButtonVariant.Destructive)
                WormaCeptorButton(text = "Outlined", onClick = {}, variant = ButtonVariant.Outlined)
                WormaCeptorButton(text = "Text", onClick = {}, variant = ButtonVariant.Text)
            }
        }
    }
}
