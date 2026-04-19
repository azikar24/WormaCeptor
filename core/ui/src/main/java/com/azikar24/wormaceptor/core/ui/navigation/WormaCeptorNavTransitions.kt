package com.azikar24.wormaceptor.core.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/**
 * Shared page transition specs for NavHost composables.
 * Provides consistent slide+fade transitions across all feature navigation graphs.
 */
object WormaCeptorNavTransitions {

    /** Slide-left-and-fade enter transition for forward navigation. */
    val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(WormaCeptorTokens.Animation.PAGE),
        ) + fadeIn(animationSpec = tween(WormaCeptorTokens.Animation.PAGE))
    }

    /** Slide-left-and-fade exit transition for forward navigation. */
    val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(WormaCeptorTokens.Animation.PAGE),
        ) + fadeOut(animationSpec = tween(WormaCeptorTokens.Animation.PAGE))
    }

    /** Slide-right-and-fade enter transition for back navigation. */
    val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(WormaCeptorTokens.Animation.PAGE),
        ) + fadeIn(animationSpec = tween(WormaCeptorTokens.Animation.PAGE))
    }

    /** Slide-right-and-fade exit transition for back navigation. */
    val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(WormaCeptorTokens.Animation.PAGE),
        ) + fadeOut(animationSpec = tween(WormaCeptorTokens.Animation.PAGE))
    }
}
