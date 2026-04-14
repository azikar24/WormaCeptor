package com.azikar24.wormaceptor.core.ui.components.button

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import com.azikar24.wormaceptor.core.ui.theme.tokens.scaled

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
    val sizedModifier = modifier.defaultMinSize(
        minHeight = WormaCeptorTokens.TouchTarget.comfortable.scaled(),
    )
    val filledContentPadding = PaddingValues(
        horizontal = WormaCeptorTokens.Spacing.xl.scaled(),
        vertical = WormaCeptorTokens.Spacing.md.scaled(),
    )
    val textContentPadding = PaddingValues(
        horizontal = WormaCeptorTokens.Spacing.md.scaled(),
        vertical = WormaCeptorTokens.Spacing.sm.scaled(),
    )
    // M3 state-layer opacities applied consistently to disabled containers/content
    // across every variant so "disabled" looks identical regardless of emphasis level.
    val disabledContainer = MaterialTheme.colorScheme.onSurface.copy(
        alpha = WormaCeptorTokens.StateLayer.DISABLED_CONTAINER,
    )
    val disabledContent = MaterialTheme.colorScheme.onSurface.copy(
        alpha = WormaCeptorTokens.StateLayer.DISABLED_CONTENT,
    )

    when (variant) {
        ButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = sizedModifier,
                enabled = isEnabled,
                shape = WormaCeptorTokens.Shapes.button,
                contentPadding = filledContentPadding,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor ?: MaterialTheme.colorScheme.primary,
                    contentColor = contentColor ?: MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = disabledContainer,
                    disabledContentColor = disabledContent,
                ),
            ) {
                ButtonContent(text = text, loading = loading, leadingIcon = leadingIcon)
            }
        }

        ButtonVariant.Secondary -> {
            Button(
                onClick = onClick,
                modifier = sizedModifier,
                enabled = isEnabled,
                shape = WormaCeptorTokens.Shapes.button,
                contentPadding = filledContentPadding,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor
                        ?: MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = contentColor ?: MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = disabledContainer,
                    disabledContentColor = disabledContent,
                ),
            ) {
                ButtonContent(text = text, loading = loading, leadingIcon = leadingIcon)
            }
        }

        ButtonVariant.Destructive -> {
            Button(
                onClick = onClick,
                modifier = sizedModifier,
                enabled = isEnabled,
                shape = WormaCeptorTokens.Shapes.button,
                contentPadding = filledContentPadding,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor ?: MaterialTheme.colorScheme.error,
                    contentColor = contentColor ?: MaterialTheme.colorScheme.onError,
                    disabledContainerColor = disabledContainer,
                    disabledContentColor = disabledContent,
                ),
            ) {
                ButtonContent(text = text, loading = loading, leadingIcon = leadingIcon)
            }
        }

        ButtonVariant.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                modifier = sizedModifier,
                enabled = isEnabled,
                shape = WormaCeptorTokens.Shapes.button,
                contentPadding = filledContentPadding,
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
                    disabledContentColor = disabledContent,
                ),
            ) {
                ButtonContent(text = text, loading = loading, leadingIcon = leadingIcon)
            }
        }

        ButtonVariant.Text -> {
            TextButton(
                onClick = onClick,
                modifier = sizedModifier,
                enabled = isEnabled,
                shape = WormaCeptorTokens.Shapes.button,
                contentPadding = textContentPadding,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = contentColor ?: MaterialTheme.colorScheme.primary,
                    disabledContentColor = disabledContent,
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
        Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm.scaled()))
    } else if (leadingIcon != null) {
        leadingIcon()
        Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm.scaled()))
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
