/*
 * Copyright AziKar24 28/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.crashes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.annotations.ScreenPreviews
import com.azikar24.wormaceptor.internal.ui.features.crashes.components.CrashesList
import com.azikar24.wormaceptor.internal.ui.features.destinations.CrashDetailsScreenDestination
import com.azikar24.wormaceptor.internal.ui.navigation.NavGraphTypes
import com.azikar24.wormaceptor.ui.components.WormaCeptorToolbar
import com.azikar24.wormaceptor.ui.theme.WormaCeptorMainTheme
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator

@Destination(navGraph = NavGraphTypes.HOME_NAV_GRAPH)
@Composable
fun CrashesListScreen(
    navigator: DestinationsNavigator,
    viewModel: CrashTransactionViewModel = viewModel(),
) {
    LaunchedEffect(key1 = "searchKey", block = {
        viewModel.fetchData()
    })

    val data = viewModel.pageEventFlow.collectAsLazyPagingItems()

    Column {
        WormaCeptorToolbar.WormaCeptorToolbar(title = stringResource(id = R.string.crashes), subtitle =  "", navController = navigator)
        Column() {
            Box(Modifier.weight(0.5f)) {
                CrashesList(data) {
                    navigator.navigate(CrashDetailsScreenDestination(it))
                }
            }
        }
    }
}

@Composable
@ScreenPreviews
private fun PreviewCrasheslistScreen() {
    WormaCeptorMainTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            CrashesListScreen(navigator = EmptyDestinationsNavigator)
        }
    }
}