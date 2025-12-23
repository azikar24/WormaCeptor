/*
 * Copyright AziKar24 28/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.home

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.azikar24.wormaceptor.annotations.ScreenPreviews
import com.azikar24.wormaceptor.internal.ui.features.crashes.CrashDetailsScreen
import com.azikar24.wormaceptor.internal.ui.features.crashes.CrashesListScreen
import com.azikar24.wormaceptor.internal.ui.features.home.bottomnav.BottomBar
import com.azikar24.wormaceptor.internal.ui.features.home.bottomnav.BottomBarDestination
import com.azikar24.wormaceptor.internal.ui.features.network.NetworkListScreen
import com.azikar24.wormaceptor.internal.ui.features.network.details.NetworkDetailsScreen
import com.azikar24.wormaceptor.internal.ui.navigation.Route
import com.azikar24.wormaceptor.ui.theme.WormaCeptorMainTheme


@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val showBottomNavBar = remember(navBackStackEntry) {
        BottomBarDestination.entries.any {
            navBackStackEntry?.destination?.hasRoute(it.route::class) == true
        }
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomNavBar,
                enter = slideInVertically(
                    initialOffsetY = { it } // from bottom
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it } // to bottom
                ) + fadeOut()
            ) {
                BottomBar(navController = navController)
            }
        }
    ) { _ ->
        NavHost(
            navController = navController,
            startDestination = Route.NetworkList,
            modifier = Modifier
                .fillMaxSize()/*
                .padding(paddingValues)*/,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(700)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(700)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(700)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(700)) }

        ) {
            composable<Route.NetworkList> {
                NetworkListScreen(navController = navController)
            }
            composable<Route.CrashesList> {
                CrashesListScreen(navController = navController)
            }
            composable<Route.NetworkDetails> { backStackEntry ->
                val details: Route.NetworkDetails = backStackEntry.toRoute()
                NetworkDetailsScreen(navController = navController, transactionId = details.id)
            }
            composable<Route.CrashDetails> { backStackEntry ->
                val details: Route.CrashDetails = backStackEntry.toRoute()
                CrashDetailsScreen(navController = navController, crashId = details.id)
            }
        }
    }
}


@ScreenPreviews
@Composable
private fun DefaultPreview() {
    WormaCeptorMainTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen()
        }
    }
}