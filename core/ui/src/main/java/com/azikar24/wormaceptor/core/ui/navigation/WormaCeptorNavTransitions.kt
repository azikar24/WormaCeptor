package com.azikar24.wormaceptor.core.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Shared page transition specs for NavHost composables.
 * Provides consistent slide+fade transitions across all feature navigation graphs.
 */
object WormaCeptorNavTransitions {

    val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.page),
        ) + fadeIn(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.page))
    }

    val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.page),
        ) + fadeOut(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.page))
    }

    val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.page),
        ) + fadeIn(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.page))
    }

    val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.page),
        ) + fadeOut(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.page))
    }
}
