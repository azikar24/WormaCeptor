/*
 * Copyright AziKar24 28/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.home.bottomnav

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.azikar24.wormaceptor.annotations.ComponentPreviews
import com.azikar24.wormaceptor.ui.theme.WormaCeptorMainTheme

@Composable
fun BottomBar(
    navController: NavHostController,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    BottomNavigation(
        backgroundColor = MaterialTheme.colors.primary,
        modifier = Modifier
            .height(75.dp)

    ) {
        BottomBarDestination.values().forEach { destination ->

            val isCurrentDestOnBackStack =
                navBackStackEntry?.destination?.route == destination.direction.route

            BottomNavigationItem(
                selected = isCurrentDestOnBackStack,
                onClick = {
                    if (isCurrentDestOnBackStack) {
                        navController.popBackStack(destination.direction.route, false)
                        return@BottomNavigationItem
                    }

                    navController.navigate(destination.direction.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = stringResource(destination.label),
                        tint = MaterialTheme.colors.onPrimary.copy(if (isCurrentDestOnBackStack) 1f else 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(destination.label),
                        color = MaterialTheme.colors.onPrimary.copy(if (isCurrentDestOnBackStack) 1f else 0.5f),
                    )
                },
                modifier = Modifier.padding(bottom = 15.dp),
            )
        }
    }
}

@ComponentPreviews
@Composable
private fun PreviewBottomBar() {
    WormaCeptorMainTheme {
        Column {
            BottomBar(navController = rememberNavController())
        }
    }

}