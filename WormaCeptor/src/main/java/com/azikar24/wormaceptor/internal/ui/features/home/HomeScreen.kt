/*
 * Copyright AziKar24 28/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.home

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.azikar24.wormaceptor.annotations.ScreenPreviews
import com.azikar24.wormaceptor.internal.ui.features.NavGraphs
import com.azikar24.wormaceptor.internal.ui.features.home.bottomnav.BottomBar
import com.azikar24.wormaceptor.internal.ui.features.home.bottomnav.BottomBarDestination
import com.azikar24.wormaceptor.internal.ui.navigation.NavGraphTypes
import com.azikar24.wormaceptor.ui.theme.WormaCeptorMainTheme
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator


@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)
@Destination(start = true, navGraph = NavGraphTypes.HOME_NAV_GRAPH)
@Composable
fun HomeScreen(
    navigator: DestinationsNavigator,
) {
    val engine = rememberAnimatedNavHostEngine(
        navHostContentAlignment = Alignment.TopStart,
    )
    val navController = engine.rememberNavController()
    var showBottomNavBar by remember {
        mutableStateOf(true)
    }
    navController.addOnDestinationChangedListener { _, destination, _ ->
        showBottomNavBar = BottomBarDestination.entries.find {
            it.direction.route == destination.route
        } != null
    }
    Scaffold(
        bottomBar = {
            if (showBottomNavBar) BottomBar(navController = navController)
        }
    ) {
        DestinationsNavHost(
            engine = engine,
            navController = navController,
            navGraph = NavGraphs.HOMENAVGRAPH,
            startRoute = BottomBarDestination.Network.direction,
        )
    }
}

@Destination(start = true)
@Composable
fun StartPoint(navigator: DestinationsNavigator) {
    DestinationsNavHost(navGraph = NavGraphs.HOMENAVGRAPH)
}

@ScreenPreviews
@Composable
private fun DefaultPreview() {
    WormaCeptorMainTheme() {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen(navigator = EmptyDestinationsNavigator)
        }
    }
}