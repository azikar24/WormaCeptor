/*
 * Copyright AziKar24 26/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.network.details

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.annotations.ScreenPreviews
import com.azikar24.wormaceptor.internal.data.HttpHeader
import com.azikar24.wormaceptor.internal.support.FormatUtils
import com.azikar24.wormaceptor.internal.support.event.multipleEventsCutter
import com.azikar24.wormaceptor.ui.components.SearchUI
import com.azikar24.wormaceptor.ui.components.WormaCeptorToolbar
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcArrowDown
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcArrowUp
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcSearch
import com.azikar24.wormaceptor.ui.theme.WormaCeptorMainTheme
import com.azikar24.wormaceptor.ui.theme.mSearchHighlightBackgroundColor
import com.azikar24.wormaceptor.ui.theme.mSearchHighlightTextColor
import com.example.wormaceptor.ui.drawables.MyIconPack
import kotlinx.coroutines.*


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PayloadScreen(headers: List<HttpHeader>?, body: AnnotatedString?, color: Color) {
    var showFloatingActionBar by remember {
        mutableStateOf(true)
    }

    var showSearchBar by remember {
        mutableStateOf(false)
    }

    var searchKey: String? by remember {
        mutableStateOf(null)
    }
    val scrollState = remember { ScrollState(0) }
    var textHeight by remember { mutableStateOf(0) }
    var currentIndex by remember { mutableStateOf(0) }
    var textLayoutResult1: TextLayoutResult? by remember { mutableStateOf(null) }

    val headersText = remember(headers) { FormatUtils.formatHeaders(headers, true).toString() }
    val bodyText = remember(body) { body.toString() }
    val fullText = remember(headersText, bodyText) { headersText + bodyText }

    var content by remember(fullText, searchKey, currentIndex) {
        mutableStateOf(
            if (searchKey.isNullOrEmpty()) {
                AnnotatedString(fullText)
            } else {
                val highlighted = FormatUtils.getHighlightedText(fullText, searchKey)
                val indexes = FormatUtils.indexOf(fullText, searchKey)
                if (indexes.isNotEmpty() && currentIndex in indexes.indices) {
                    AnnotatedString.Builder(highlighted).apply {
                        addStyle(
                            style = SpanStyle(
                                background = mSearchHighlightTextColor,
                                color = mSearchHighlightBackgroundColor
                            ),
                            start = indexes[currentIndex],
                            end = indexes[currentIndex] + (searchKey?.length ?: 0)
                        )
                    }.toAnnotatedString()
                } else {
                    highlighted
                }
            }
        )
    }

    val coroutineScope = rememberCoroutineScope()
    val indexes: List<Int> by remember(fullText, searchKey) {
        mutableStateOf(FormatUtils.indexOf(fullText, searchKey))
    }



    println(indexes)
    Scaffold(
        floatingActionButton = {
            Column {
                AnimatedVisibility(
                    visible = showFloatingActionBar,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    FloatingActionButton(
                        onClick = {},
                        shape = RoundedCornerShape(360.dp),
                        containerColor = color
                    ) {
                        IconButton(
                            onClick = {
                                showFloatingActionBar = false
                                showSearchBar = true
                            },
                        ) {
                            Icon(
                                imageVector = MyIconPack.IcSearch,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showSearchBar && indexes.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {


                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                        FloatingActionButton(
                            onClick = {
                                if (scrollToIndex(
                                        coroutineScope,
                                        indexes,
                                        currentIndex - 1,
                                        textLayoutResult1,
                                        scrollState
                                    )
                                ) {
                                    currentIndex--
                                } else {
                                    Toast.makeText(
                                        WormaCeptorToolbar.activity,
                                        "$currentIndex",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            shape = RoundedCornerShape(360.dp),
                            containerColor = color,
                            modifier = Modifier
                                .size(if (currentIndex == 0) 0.dp else 50.dp)
                        ) {
                            Icon(
                                imageVector = MyIconPack.IcArrowUp,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(10.dp)

                            )
                        }

                        FloatingActionButton(
                            onClick = {
                                if (scrollToIndex(
                                        coroutineScope,
                                        indexes,
                                        currentIndex + 1,
                                        textLayoutResult1,
                                        scrollState
                                    )
                                ) {
                                    currentIndex++
                                } else {
                                    Toast.makeText(
                                        WormaCeptorToolbar.activity,
                                        "$currentIndex",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            shape = RoundedCornerShape(360.dp),

                            containerColor = color,
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .size(if (currentIndex == indexes.lastIndex) 0.dp else 50.dp)

                        ) {
                            Icon(
                                imageVector = MyIconPack.IcArrowDown,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }

                    }
//                    FloatingActionButton(
//                        onClick = {},
//                        shape = RoundedCornerShape(360.dp),
//                        backgroundColor = color
//                    ) {
//                        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
//                            IconButton(
//                                onClick = {
//                                    if (scrollToIndex(coroutineScope, indexes, currentIndex + 1, textLayoutResult1, scrollState)) {
//                                        currentIndex++
//                                    } else {
//                                        Toast.makeText(WormaCeptorToolbar.activity, "$currentIndex", Toast.LENGTH_SHORT).show()
//                                    }
//                                },
//                                modifier = Modifier.padding(10.dp)
//                            ) {
//                                Icon(
//                                    imageVector = MyIconPack.IcArrowDown,
//                                    contentDescription = "",
//                                    tint = MaterialTheme.colors.onPrimary,
//                                )
//                            }
//                            Box(Modifier
//                                .fillMaxHeight()
//                                .width(1.dp)
//                                .background(MaterialTheme.colors.onPrimary.copy(0.5f))
//                            )
//                            IconButton(
//                                onClick = {
//                                    if (scrollToIndex(coroutineScope, indexes, currentIndex - 1, textLayoutResult1, scrollState)) {
//                                        currentIndex--
//                                    } else {
//                                        Toast.makeText(WormaCeptorToolbar.activity, "$currentIndex", Toast.LENGTH_SHORT).show()
//                                    }
//                                },
//                                modifier = Modifier.padding(10.dp)
//                            ) {
//                                Icon(
//                                    imageVector = MyIconPack.IcArrowUp,
//                                    contentDescription = "",
//                                    tint = MaterialTheme.colors.onPrimary,
//                                )
//                            }
//                        }
//                    }
                }


            }
        }

    ) {
        Column {
            AnimatedVisibility(
                visible = showSearchBar,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                SearchUI(
                    color = color,
                    counter = currentIndex,
                    maxCounter = indexes.size,
                    searchListener = multipleEventsCutter { manager ->
                        { searchStr ->
                            if (searchStr != null) {
                                manager.processEvent {
                                    searchKey = searchStr
                                }
                            }
                            if (searchStr.isNullOrBlank()) {
                                showFloatingActionBar = true
                                showSearchBar = false
                            }
                        }
                    })
            }
            Text(
                text = content,
                onTextLayout = { textLayoutResult ->
                    val lineHeight = textLayoutResult.firstBaseline
                    val lineCount = textLayoutResult.lineCount
                    textHeight = ((lineHeight * lineCount).toInt())
                    textLayoutResult1 = textLayoutResult
                },
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            )

        }
    }
}

fun scrollToIndex(
    coroutineScope: CoroutineScope,
    indexes: List<Int>,
    index: Int,
    textLayoutResult: TextLayoutResult?,
    scrollState: ScrollState
): Boolean {
    return try {
        val lineNumber = textLayoutResult?.getLineForOffset(indexes[index]) ?: 0
        val lineTopPx = textLayoutResult?.getLineTop(lineNumber)
        coroutineScope.launch {
            if (lineTopPx != null) {
                scrollState.animateScrollTo(lineTopPx.toInt())
            }
        }
        lineTopPx != null
    } catch (_: java.lang.Exception) {
        false
    }
}


@ScreenPreviews
@Composable
private fun PreviewPayloadScreen() {
    WormaCeptorMainTheme {
        PayloadScreen(
            headers = listOf(HttpHeader("header", "value")),
            body = buildAnnotatedString { append("body") },
            color = MaterialTheme.colorScheme.primary
        )
    }
}