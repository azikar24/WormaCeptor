/*
 * Copyright AziKar24 28/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.network.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.azikar24.wormaceptor.internal.NetworkTransactionUIHelper
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
@OptIn(ExperimentalPagerApi::class)
@Composable
fun NetworkDetailsScreen(
    navigator: DestinationsNavigator,
    viewModel: NetworkTransactionViewModel = viewModel(),
    mNetworkTransactionUIHelper: NetworkTransactionUIHelper,
) {
    var networkTransactionUIHelper: NetworkTransactionUIHelper? by remember {
        mutableStateOf(null)
    }
    LaunchedEffect(key1 = mNetworkTransactionUIHelper.networkTransaction.id.toString()) {
        viewModel
            .getTransactionWithId(mNetworkTransactionUIHelper.networkTransaction.id)
            ?.collectLatest {
                networkTransactionUIHelper = it
            }
    }


    Column() {
        val color = networkTransactionUIHelper?.let { ColorUtil.getInstance(LocalContext.current).getTransactionColor(it) }?.let { Color(it) } ?: MaterialTheme.colors.primary
        var expanded by remember { mutableStateOf(false) }
        val title = "[${networkTransactionUIHelper?.networkTransaction?.method}] ${networkTransactionUIHelper?.networkTransaction?.path}"
        WormaCeptorToolbar.WormaCeptorToolbar(title = title, color = color, navController = navigator) {
            IconButton(onClick = { expanded = true }) {
                Icon(tint = MaterialTheme.colors.onPrimary, imageVector = ImageVector.vectorResource(id = R.drawable.ic_share_white_24dp), contentDescription = "")
                val options = listOf(
                    stringResource(id = R.string.share_as_text),
                    stringResource(id = R.string.share_as_curl)
                )
                val context = LocalContext.current
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach() { option ->
                        DropdownMenuItem(onClick = {
                            if(option ==context.getString(R.string.share_as_text)){
                                networkTransactionUIHelper?.let { context.share(FormatUtils.getShareText(context, it)) }
                            } else {
                                networkTransactionUIHelper?.let { context.share(FormatUtils.getShareCurlCommand(it)) }
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

        val pagerState = rememberPagerState(pageCount = tabData.size)
        Column(modifier = Modifier.fillMaxSize()) {
            TabLayout(tabData, pagerState, color)
            TabContent(pagerState, networkTransactionUIHelper)
        }

    }
}


@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabContent(pagerState: PagerState, networkTransactionUIHelper: NetworkTransactionUIHelper?) {
    HorizontalPager(state = pagerState) { index ->
        when (index) {
            0 -> {
                Column(Modifier.fillMaxSize()) {
                    OverviewScreen(networkTransactionUIHelper)
                }

            }

            1 -> {
                Column(Modifier.fillMaxSize()) {
                    PayloadScreen(
                        headers = networkTransactionUIHelper?.networkTransaction?.requestHeaders,//.getRequestHeadersString(true),
                        body = if (networkTransactionUIHelper?.networkTransaction?.requestBodyIsPlainText == true)
                            networkTransactionUIHelper.getFormattedRequestBody()
                        else
                            buildAnnotatedString { stringResource(id = R.string.body_omitted) },
                        color = networkTransactionUIHelper?.let { ColorUtil.getTransactionColor(it) } ?: MaterialTheme.colors.primary
                    )
                }
            }

            2 -> {
                Column(Modifier.fillMaxSize()) {
                    PayloadScreen(
                        headers = networkTransactionUIHelper?.networkTransaction?.responseHeaders,//.getResponseHeadersString(true),
                        body = if (networkTransactionUIHelper?.networkTransaction?.responseBodyIsPlainText == true)
                            networkTransactionUIHelper.getFormattedResponseBody()
                        else
                            buildAnnotatedString { stringResource(id = R.string.body_omitted) },
                        color = networkTransactionUIHelper?.let { ColorUtil.getTransactionColor(it) } ?: MaterialTheme.colors.primary
                    )
                }
            }
        }

    }

}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabLayout(tabData: List<String>, pagerState: PagerState, color: Color) {

    val scope = rememberCoroutineScope()
    TabRow(selectedTabIndex = pagerState.currentPage, divider = {
        Spacer(modifier = Modifier.height(5.dp))
    }, indicator = { tabPositions ->
        TabRowDefaults.Indicator(
            modifier = Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
            height = 3.dp,
            color = MaterialTheme.colors.onPrimary,
        )
    }, contentColor = MaterialTheme.colors.onPrimary, backgroundColor = color, modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .background(color)) {
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
