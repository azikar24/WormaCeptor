package com.azikar24.wormaceptor.core.ui.theme.tokens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

/**
 * CompositionLocal carrying the current [TokenDensity] preference.
 *
 * Defaults to [TokenDensity.Default]. Override at the theme boundary to
 * switch the entire subtree between compact / default / expanded layouts
 * without forking component APIs. Propagating density implicitly is the
 * whole point -- callers shouldn't have to thread it through every Composable.
 */
@Suppress("CompositionLocalAllowlist")
val LocalWormaCeptorDensity = staticCompositionLocalOf { TokenDensity.Default }

/**
 * Returns this Dp scaled by the current [LocalWormaCeptorDensity] multiplier.
 * Apply to padding and sizing at call sites that should adapt to density.
 */
@Composable
@ReadOnlyComposable
fun Dp.scaled(): Dp = this * LocalWormaCeptorDensity.current.scale
