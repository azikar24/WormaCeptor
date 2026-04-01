package com.azikar24.wormaceptor.core.ui

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * Modifier that records a recomposition event every time the composable
 * it is applied to recomposes.
 *
 * Usage:
 * ```kotlin
 * Card(modifier = Modifier.trackRecomposition("ProductCard")) {
 *     // ...
 * }
 * ```
 *
 * @param name A human-readable identifier for the composable being tracked.
 */
fun Modifier.trackRecomposition(name: String): Modifier = composed {
    SideEffect { RecompositionTracker.record(name) }
    this
}
