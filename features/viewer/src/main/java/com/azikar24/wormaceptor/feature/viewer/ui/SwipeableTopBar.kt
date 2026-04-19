package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Swipeable container for the TopAppBar that handles horizontal swipes
 * to navigate between transactions.
 */
@Composable
internal fun SwipeableTopBar(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    canSwipeLeft: Boolean,
    canSwipeRight: Boolean,
    content: @Composable () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(canSwipeLeft, canSwipeRight) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val threshold = size.width * 0.15f
                        when {
                            dragOffset < -threshold && canSwipeLeft -> {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSwipeLeft()
                            }

                            dragOffset > threshold && canSwipeRight -> {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSwipeRight()
                            }
                        }
                        dragOffset = 0f
                    },
                    onDragCancel = { dragOffset = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        dragOffset += dragAmount
                    },
                )
            },
    ) {
        content()
    }
}
