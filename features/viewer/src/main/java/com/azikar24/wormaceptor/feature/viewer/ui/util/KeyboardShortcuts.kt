package com.azikar24.wormaceptor.feature.viewer.ui.util

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

/**
 * Keyboard shortcut definitions for the viewer.
 */
object KeyboardShortcuts {
    // Ctrl/Cmd + R: Refresh
    val REFRESH = ShortcutKey(Key.R, ctrl = true)

    // Ctrl/Cmd + F: Search/Filter
    val SEARCH = ShortcutKey(Key.F, ctrl = true)

    // Ctrl/Cmd + A: Select All
    val SELECT_ALL = ShortcutKey(Key.A, ctrl = true)

    // Delete: Delete selected
    val DELETE = ShortcutKey(Key.Delete)

    // Escape: Clear selection/cancel
    val ESCAPE = ShortcutKey(Key.Escape)

    // Ctrl/Cmd + E: Export
    val EXPORT = ShortcutKey(Key.E, ctrl = true)

    // Ctrl/Cmd + Shift + C: Copy as cURL
    val COPY_CURL = ShortcutKey(Key.C, ctrl = true, shift = true)
}

/**
 * Represents a keyboard shortcut with modifiers.
 */
data class ShortcutKey(
    val key: Key,
    val ctrl: Boolean = false,
    val shift: Boolean = false,
    val alt: Boolean = false,
)

/**
 * Callbacks for keyboard shortcut handling.
 */
data class KeyboardShortcutCallbacks(
    val onRefresh: () -> Unit = {},
    val onSearch: () -> Unit = {},
    val onSelectAll: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onClear: () -> Unit = {},
    val onExport: () -> Unit = {},
    val onCopyCurl: () -> Unit = {},
)

/**
 * A composable wrapper that handles keyboard shortcuts for the entire viewer.
 * Works with external keyboards on Android tablets and Chrome OS.
 *
 * @param callbacks The callbacks to invoke when shortcuts are triggered
 * @param enabled Whether keyboard handling is enabled
 * @param content The content to wrap
 */
@Composable
fun KeyboardShortcutHandler(
    callbacks: KeyboardShortcutCallbacks,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        // Request focus to receive key events
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            // Focus request may fail in some configurations
        }
    }

    Box(
        modifier = Modifier
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (!enabled || event.type != KeyEventType.KeyDown) {
                    return@onKeyEvent false
                }

                val isCtrl = event.isCtrlPressed || event.isMetaPressed

                when {
                    // Ctrl/Cmd + R: Refresh
                    isCtrl && event.key == Key.R -> {
                        callbacks.onRefresh()
                        true
                    }

                    // Ctrl/Cmd + F: Search
                    isCtrl && event.key == Key.F -> {
                        callbacks.onSearch()
                        true
                    }

                    // Ctrl/Cmd + A: Select All
                    isCtrl && event.key == Key.A -> {
                        callbacks.onSelectAll()
                        true
                    }

                    // Ctrl/Cmd + E: Export
                    isCtrl && event.key == Key.E -> {
                        callbacks.onExport()
                        true
                    }

                    // Delete: Delete selected
                    event.key == Key.Delete || event.key == Key.Backspace -> {
                        callbacks.onDelete()
                        true
                    }

                    // Escape: Clear/cancel
                    event.key == Key.Escape -> {
                        callbacks.onClear()
                        true
                    }

                    else -> false
                }
            },
    ) {
        content()
    }
}

/**
 * Extension function to create a modifier that handles keyboard shortcuts.
 * Use this when you need more control over the modifier chain.
 */
fun Modifier.handleKeyboardShortcuts(callbacks: KeyboardShortcutCallbacks, enabled: Boolean = true): Modifier =
    this.then(
        Modifier.onKeyEvent { event ->
            if (!enabled || event.type != KeyEventType.KeyDown) {
                return@onKeyEvent false
            }

            val isCtrl = event.isCtrlPressed || event.isMetaPressed

            when {
                isCtrl && event.key == Key.R -> {
                    callbacks.onRefresh()
                    true
                }

                isCtrl && event.key == Key.F -> {
                    callbacks.onSearch()
                    true
                }

                isCtrl && event.key == Key.A -> {
                    callbacks.onSelectAll()
                    true
                }

                isCtrl && event.key == Key.E -> {
                    callbacks.onExport()
                    true
                }

                event.key == Key.Delete || event.key == Key.Backspace -> {
                    callbacks.onDelete()
                    true
                }

                event.key == Key.Escape -> {
                    callbacks.onClear()
                    true
                }

                else -> false
            }
        },
    )
