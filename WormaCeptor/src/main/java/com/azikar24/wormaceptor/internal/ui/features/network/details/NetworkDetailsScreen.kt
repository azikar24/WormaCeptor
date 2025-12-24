/*
 * Copyright AziKar24 28/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.network.details

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import com.azikar24.wormaceptor.internal.support.ColorUtil
import com.azikar24.wormaceptor.internal.ui.WormaCeptorViewModelFactory
import com.azikar24.wormaceptor.internal.ui.features.network.NetworkTransactionViewModel
import com.azikar24.wormaceptor.ui.components.WormaCeptorToolbar
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.internal.support.FormatUtils
import com.azikar24.wormaceptor.internal.support.share
import com.azikar24.wormaceptor.internal.ui.ToolbarViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NetworkDetailsScreen(
    navController: NavController,
    transactionId: Long,
    viewModel: NetworkTransactionViewModel = koinViewModel(),
    toolbarViewModel: ToolbarViewModel = koinViewModel(),
) {
    var networkTransaction: NetworkTransaction? by remember {
        mutableStateOf(null)
    }
    LaunchedEffect(key1 = transactionId) {
        viewModel
            .getTransactionWithId(transactionId)
            ?.collectLatest {
                networkTransaction = it
            }
    }

    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    val color = networkTransaction?.let {
        ColorUtil.getInstance(context).getTransactionColor(it)
    }?.let { Color(it) } ?: MaterialTheme.colorScheme.primary

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, networkTransaction, color, expanded) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                networkTransaction?.let { transaction ->
                    toolbarViewModel.title = "[${transaction.method}] ${transaction.path}"
                    toolbarViewModel.color = color
                    toolbarViewModel.showSearch = false
                    toolbarViewModel.menuActions = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                tint = MaterialTheme.colorScheme.onPrimary,
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_share_white_24dp),
                                contentDescription = ""
                            )
                            val options = listOf(
                                context.getString(R.string.share_as_text),
                                context.getString(R.string.share_as_curl)
                            )
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                options.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(text = option) },
                                        onClick = {
                                            if (option == context.getString(R.string.share_as_text)) {
                                                context.share(FormatUtils.getShareText(context, transaction))
                                            } else {
                                                context.share(FormatUtils.getShareCurlCommand(transaction))
                                            }
                                            expanded = false
                                        })
                                }
                            }
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


    Column(
        modifier = Modifier.imePadding()
    ) {
        val tabData = listOf(
            "Overview",
            "Request",
            "Response",
        )

        val pagerState = rememberPagerState(0) { tabData.size }
        Column(modifier = Modifier.fillMaxSize()) {
            TabLayout(tabData, pagerState, color)
            TabContent(pagerState, networkTransaction)
        }

    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabContent(pagerState: PagerState, networkTransaction: NetworkTransaction?) {
    HorizontalPager(
        state = pagerState,
    ) { index ->
        when (index) {
            0 -> {
                Column(Modifier.fillMaxSize()) {
                    OverviewScreen(networkTransaction)
                }

            }

            1 -> {
                Column(Modifier.fillMaxSize()) {
                    PayloadScreen(
                        headers = networkTransaction?.requestHeaders,//.getRequestHeadersString(true),
                        body = if (networkTransaction?.requestBodyIsPlainText == true)
                            networkTransaction.getFormattedRequestBody()
                        else
                            buildAnnotatedString { stringResource(id = R.string.body_omitted) },
                        color = networkTransaction?.let { ColorUtil.getTransactionColor(it) }
                            ?: MaterialTheme.colorScheme.primary
                    )
                }
            }

            2 -> {
                Column(Modifier.fillMaxSize()) {
                    PayloadScreen(
                        headers = networkTransaction?.responseHeaders,//.getResponseHeadersString(true),
                        body = if (networkTransaction?.responseBodyIsPlainText == true)
                            networkTransaction.getFormattedResponseBody()
                        else
                            buildAnnotatedString { stringResource(id = R.string.body_omitted) },
                        color = networkTransaction?.let { ColorUtil.getTransactionColor(it) }
                            ?: MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabLayout(
    tabData: List<String>,
    pagerState: PagerState,
    color: Color
) {

    val scope = rememberCoroutineScope()
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        divider = {
            Spacer(modifier = Modifier.height(5.dp))
        },
        indicator = { tabPositions ->
            if (pagerState.currentPage < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    height = 3.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        contentColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = color,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color)
    ) {
        tabData.forEachIndexed { index, s ->
            Tab(selected = pagerState.currentPage == index, onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(index)
                }
            }, text = {
                Text(text = s)
            })
        }
    }
}
