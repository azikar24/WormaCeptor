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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.azikar24.wormaceptor.annotations.ScreenPreviews
import com.azikar24.wormaceptor.internal.ui.features.network.components.NetworkList
import com.azikar24.wormaceptor.internal.ui.navigation.Route
import com.azikar24.wormaceptor.ui.components.WormaCeptorToolbar
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcDelete
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcSearch
import com.azikar24.wormaceptor.ui.theme.WormaCeptorMainTheme
import com.example.wormaceptor.ui.drawables.MyIconPack

@Composable
fun NetworkListScreen(
    navController: NavController,
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
            navController = navController,
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
                    navController.navigate(Route.NetworkDetails(it.id))
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
            NetworkListScreen(rememberNavController())
        }
    }
}