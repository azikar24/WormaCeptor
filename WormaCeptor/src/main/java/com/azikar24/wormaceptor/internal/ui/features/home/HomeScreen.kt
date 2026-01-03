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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.azikar24.wormaceptor.annotations.ScreenPreviews
import com.azikar24.wormaceptor.internal.ui.ToolbarViewModel
import com.azikar24.wormaceptor.internal.ui.features.crashes.CrashDetailsScreen
import com.azikar24.wormaceptor.internal.ui.features.crashes.CrashesListScreen
import com.azikar24.wormaceptor.internal.ui.features.home.bottomnav.BottomBar
import com.azikar24.wormaceptor.internal.ui.features.home.bottomnav.BottomBarDestination
import com.azikar24.wormaceptor.internal.ui.features.network.NetworkListScreen
import com.azikar24.wormaceptor.internal.ui.features.network.details.NetworkDetailsScreen
import com.azikar24.wormaceptor.internal.ui.navigation.Route
import com.azikar24.wormaceptor.ui.components.WormaCeptorToolbar
import com.azikar24.wormaceptor.ui.theme.WormaCeptorMainTheme
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    toolbarViewModel: ToolbarViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val showBottomNavBar = remember(navBackStackEntry) {
        BottomBarDestination.entries.any {
            navBackStackEntry?.destination?.hasRoute(it.route::class) == true
        }
    }

    Scaffold(
        topBar = {
            WormaCeptorToolbar.WormaCeptorToolbar(
                title = toolbarViewModel.title,
                subtitle = toolbarViewModel.subtitle,
                navController = navController,
                showSearch = toolbarViewModel.showSearch,
                searchListener = {
                    toolbarViewModel.searchKey = it ?: ""
                },
                color = toolbarViewModel.color ?: MaterialTheme.colorScheme.primaryContainer,
                onColor = toolbarViewModel.onColor ?: MaterialTheme.colorScheme.onPrimaryContainer,
                menuActions = {
                    toolbarViewModel.menuActions?.invoke()
                }
            )
        },
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
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Route.NetworkList,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }

        ) {
            composable<Route.NetworkList> {
                NetworkListScreen(
                    navController = navController,
                )
            }
            composable<Route.CrashesList> {
                CrashesListScreen(
                    navController = navController,
                )
            }
            composable<Route.NetworkDetails> { backStackEntry ->
                val details: Route.NetworkDetails = backStackEntry.toRoute()
                NetworkDetailsScreen(transactionId = details.id)
            }
            composable<Route.CrashDetails> { backStackEntry ->
                val details: Route.CrashDetails = backStackEntry.toRoute()
                CrashDetailsScreen(crashId = details.id)
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