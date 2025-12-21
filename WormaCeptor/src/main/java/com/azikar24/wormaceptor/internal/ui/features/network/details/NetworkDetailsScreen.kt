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
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import com.azikar24.wormaceptor.internal.support.ColorUtil
import com.azikar24.wormaceptor.internal.ui.features.network.NetworkTransactionViewModel
import com.azikar24.wormaceptor.internal.ui.navigation.NavGraphTypes
import com.azikar24.wormaceptor.ui.components.WormaCeptorToolbar
import com.google.accompanist.pager.*
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.internal.support.FormatUtils
import com.azikar24.wormaceptor.internal.support.share
import kotlinx.coroutines.flow.collectLatest


@Destination(navGraph = NavGraphTypes.HOME_NAV_GRAPH)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NetworkDetailsScreen(
    navigator: DestinationsNavigator,
    viewModel: NetworkTransactionViewModel = viewModel(),
    mNetworkTransaction: NetworkTransaction,
) {
    var networkTransaction: NetworkTransaction? by remember {
        mutableStateOf(null)
    }
    LaunchedEffect(key1 = mNetworkTransaction.id.toString()) {
        viewModel
            .getTransactionWithId(mNetworkTransaction.id)
            ?.collectLatest {
                networkTransaction = it
            }
    }


    Column() {
        val color = networkTransaction?.let {
            ColorUtil.getInstance(LocalContext.current).getTransactionColor(it)
        }?.let { Color(it) } ?: MaterialTheme.colors.primary
        var expanded by remember { mutableStateOf(false) }
        val title = "[${networkTransaction?.method}] ${networkTransaction?.path}"
        WormaCeptorToolbar.WormaCeptorToolbar(
            title = title,
            color = color,
            navController = navigator
        ) {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    tint = MaterialTheme.colors.onPrimary,
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_share_white_24dp),
                    contentDescription = ""
                )
                val options = listOf(
                    stringResource(id = R.string.share_as_text),
                    stringResource(id = R.string.share_as_curl)
                )
                val context = LocalContext.current
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach() { option ->
                        DropdownMenuItem(onClick = {
                            if (option == context.getString(R.string.share_as_text)) {
                                networkTransaction?.let {
                                    context.share(
                                        FormatUtils.getShareText(
                                            context,
                                            it
                                        )
                                    )
                                }
                            } else {
                                networkTransaction?.let {
                                    context.share(
                                        FormatUtils.getShareCurlCommand(
                                            it
                                        )
                                    )
                                }
                            }
                            expanded = false
                        }) {
                            Text(text = option)
                        }
                    }
                }
            }
        }


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
                            ?: MaterialTheme.colors.primary
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
                            ?: MaterialTheme.colors.primary
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
            TabRowDefaults.Indicator(
                modifier = Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                height = 3.dp,
                color = MaterialTheme.colors.onPrimary,
            )
        },
        contentColor = MaterialTheme.colors.onPrimary,
        backgroundColor = color,
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
