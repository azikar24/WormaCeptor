/*
 * Copyright AziKar24 28/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.network.details

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import com.azikar24.wormaceptor.internal.support.ColorUtil
import com.azikar24.wormaceptor.internal.support.FormatUtils
import com.azikar24.wormaceptor.internal.support.share
import com.azikar24.wormaceptor.internal.ui.ToolbarViewModel
import com.azikar24.wormaceptor.internal.ui.features.network.NetworkTransactionViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NetworkDetailsScreen(
    transactionId: Long,
    viewModel: NetworkTransactionViewModel = koinViewModel(),
    toolbarViewModel: ToolbarViewModel = koinViewModel(),
) {
    var networkTransaction: NetworkTransaction? by remember { mutableStateOf(null) }

    LaunchedEffect(key1 = transactionId) {
        viewModel.getTransactionWithId(transactionId)?.collectLatest { networkTransaction = it }
    }
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    val transactionColors = networkTransaction?.let { ColorUtil.getTransactionColors(it) }
    val color = transactionColors?.container ?: MaterialTheme.colorScheme.primaryContainer
    val onColor = transactionColors?.onContainer ?: MaterialTheme.colorScheme.onPrimaryContainer

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, networkTransaction, color, expanded) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                networkTransaction?.let { transaction ->
                    toolbarViewModel.title = transaction.path ?: ""
                    toolbarViewModel.subtitle = "${transaction.method} • ${transaction.responseCode ?: "..."}"
                    toolbarViewModel.color = color
                    toolbarViewModel.onColor = onColor
                    toolbarViewModel.showSearch = false
                    toolbarViewModel.menuActions = {
                        IconButton(onClick = {
                            expanded = true
                        }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_share_white_24dp),
                                contentDescription = "Share",
                            )
                            val options = listOf(
                                context.getString(R.string.share_as_text),
                                context.getString(R.string.share_as_curl)
                            )
                            DropdownMenu(expanded = expanded, onDismissRequest = {
                                expanded = false
                            }) {
                                options.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(text = option) },
                                        onClick = {
                                            if (option == context.getString(R.string.share_as_text)) {
                                                context.share(
                                                    FormatUtils.getShareText(
                                                        context,
                                                        transaction
                                                    )
                                                )
                                            } else {
                                                context.share(
                                                    FormatUtils.getShareCurlCommand(
                                                        transaction
                                                    )
                                                )
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
    
    Column(modifier = Modifier.imePadding().fillMaxSize()) {
        val tabData = listOf("OVERVIEW", "REQUEST", "RESPONSE")
        val pagerState = rememberPagerState(0) { tabData.size }
        
        TabLayout(
            tabData,
            pagerState,
            color,
            onColor
        )
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { index ->
            when (index) {
                0 -> OverviewScreen(networkTransaction)
                1 -> PayloadScreen(
                    headers = networkTransaction?.requestHeaders,
                    body = if (networkTransaction?.requestBodyIsPlainText == true)
                        networkTransaction?.getFormattedRequestBody()
                    else
                        buildAnnotatedString { append(stringResource(id = R.string.body_omitted)) },
                    color = color,
                    onColor = onColor
                )
                2 -> PayloadScreen(
                    headers = networkTransaction?.responseHeaders,
                    body = if (networkTransaction?.responseBodyIsPlainText == true)
                        networkTransaction?.getFormattedResponseBody()
                    else
                        buildAnnotatedString { append(stringResource(id = R.string.body_omitted)) },
                    color = color,
                    onColor = onColor
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabLayout(
    tabData: List<String>,
    pagerState: PagerState,
    color: Color,
    onColor: Color
) {
    val scope = rememberCoroutineScope()
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        divider = {},
        indicator = { tabPositions ->
            if (pagerState.currentPage < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    height = 3.dp,
                    color = onColor,
                )
            }
        },
        contentColor = onColor,
        containerColor = color,
        modifier = Modifier.fillMaxWidth()
    ) {
        tabData.fastForEachIndexed { index, s ->
            Tab(
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                text = {
                    Text(
                        text = s,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal,
                            letterSpacing = 1.sp
                        )
                    )
                }
            )
        }
    }
}
