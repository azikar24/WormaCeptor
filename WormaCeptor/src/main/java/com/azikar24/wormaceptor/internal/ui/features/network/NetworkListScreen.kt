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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.azikar24.wormaceptor.annotations.ScreenPreviews
import com.azikar24.wormaceptor.internal.ui.ToolbarViewModel
import com.azikar24.wormaceptor.internal.ui.features.network.components.NetworkList
import com.azikar24.wormaceptor.internal.ui.navigation.Route
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcDelete
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcSearch
import com.azikar24.wormaceptor.ui.theme.WormaCeptorMainTheme
import com.example.wormaceptor.ui.drawables.MyIconPack
import org.koin.androidx.compose.koinViewModel

@Composable
fun NetworkListScreen(
    navController: NavController,
    viewModel: NetworkTransactionViewModel = koinViewModel(),
    toolbarViewModel: ToolbarViewModel = koinViewModel(),
) {
    var showLocalSearch by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, showLocalSearch) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                toolbarViewModel.title = "Network Calls"
                toolbarViewModel.subtitle = ""
                toolbarViewModel.color = null
                toolbarViewModel.onColor = null
                toolbarViewModel.showSearch = showLocalSearch
                toolbarViewModel.menuActions = {
                    Row {
                        IconButton(
                            onClick = {
                                showLocalSearch = !showLocalSearch
                                if (!showLocalSearch) toolbarViewModel.searchKey = ""
                            }
                        ) {
                            Icon(
                                imageVector = MyIconPack.IcSearch,
                                contentDescription = "",
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.clearAll()
                            }
                        ) {
                            Icon(
                                imageVector = MyIconPack.IcDelete,
                                contentDescription = "",
                            )
                        }
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(toolbarViewModel.searchKey) {
        if (!toolbarViewModel.showSearch) {
            viewModel.fetchData("")
        } else {
            viewModel.fetchData(toolbarViewModel.searchKey)
        }
    }

    val data = viewModel.pageEventFlow.collectAsLazyPagingItems()
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(Modifier.fillMaxSize()) {
            NetworkList(data, toolbarViewModel.searchKey) {
                navController.navigate(Route.NetworkDetails(it.id))
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