/*
 * Copyright AziKar24 28/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.network

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.azikar24.wormaceptor.annotations.ScreenPreviews
import com.azikar24.wormaceptor.internal.ui.features.destinations.NetworkDetailsScreenDestination
import com.azikar24.wormaceptor.internal.ui.features.network.components.NetworkList
import com.azikar24.wormaceptor.internal.ui.navigation.NavGraphTypes
import com.azikar24.wormaceptor.ui.components.WormaCeptorToolbar
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcDelete
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcSearch
import com.azikar24.wormaceptor.ui.theme.WormaCeptorMainTheme
import com.example.wormaceptor.ui.drawables.MyIconPack
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator

@Destination(navGraph = NavGraphTypes.HOME_NAV_GRAPH)
@Composable
fun NetworkListScreen(
    navigator: DestinationsNavigator,
    viewModel: NetworkTransactionViewModel = viewModel(),
) {
    var showSearch by remember {
        mutableStateOf(false)
    }
    var searchKey: String? by remember {
        mutableStateOf(null)
    }

    LaunchedEffect(key1 = searchKey, block = {
        viewModel.fetchData(searchKey)
    })


    val data = viewModel.pageEventFlow.collectAsLazyPagingItems()
    Column {
        WormaCeptorToolbar.WormaCeptorToolbar(
            title = "Network Calls",
            subtitle = "",
            showSearch = showSearch,
            navController = navigator,
            searchListener = {
                if (it != null) {
                    searchKey = it
                }
            }) {
            Row {
                IconButton(onClick = {
                    showSearch = !showSearch
                }) {
                    Icon(imageVector = MyIconPack.IcSearch, contentDescription = "")
                }
                IconButton(onClick = {
                    viewModel.clearAll()
                }) {
                    Icon(imageVector = MyIconPack.IcDelete, contentDescription = "")
                }
            }
        }
        Column() {
            Box(Modifier.weight(0.5f)) {
                NetworkList(data, searchKey) {
                    navigator.navigate(NetworkDetailsScreenDestination(it))
                }
            }
        }
    }
}


@ScreenPreviews
@Composable
private fun PreviewNetworkListScreen() {
    WormaCeptorMainTheme() {
        Surface(modifier = Modifier.fillMaxSize()) {
            NetworkListScreen(EmptyDestinationsNavigator)
        }
    }
}