/*
 * Copyright AziKar24 3/3/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.crashes.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.azikar24.wormaceptor.annotations.ComponentPreviews
import com.azikar24.wormaceptor.internal.data.CrashTransaction
import com.azikar24.wormaceptor.ui.theme.WormaCeptorMainTheme
import kotlinx.coroutines.flow.flowOf
import java.util.*


@Composable
fun CrashesList(data: LazyPagingItems<CrashTransaction>, onClick: (CrashTransaction) -> Unit) {

    LazyColumn() {
        items(data.itemSnapshotList.items) { item ->
            Column {
                CrashesListItem(data = item, onClick = onClick)
                Divider(color = MaterialTheme.colors.onSurface.copy(0.2f), thickness = 1.dp)
            }

        }
        item { Box(Modifier.height(120.dp)) }
    }
}


@ComponentPreviews
@Composable
private fun PreviewCrashesList() {
    val data = mutableListOf(
        CrashTransaction(
            id = 1,
            crashList = listOf(StackTraceElement("declaring class", "methodName", "fileName", 100)),
            crashDate = Date(),
            throwable = "throwable"
        ),
        CrashTransaction(
            id = 1,
            crashList = listOf(StackTraceElement("declaring class", "methodName", "fileName", 100)),
            crashDate = Date(),
            throwable = "throwable"
        ),
        CrashTransaction(
            id = 1,
            crashList = listOf(StackTraceElement("declaring class", "methodName", "fileName", 100)),
            crashDate = Date(),
            throwable = "throwable"
        ),
        CrashTransaction(
            id = 1,
            crashList = listOf(StackTraceElement("declaring class", "methodName", "fileName", 100)),
            crashDate = Date(),
            throwable = "throwable"
        ),
        CrashTransaction(
            id = 1,
            crashList = listOf(StackTraceElement("declaring class", "methodName", "fileName", 100)),
            crashDate = Date(),
            throwable = "throwable"
        ),
        CrashTransaction(
            id = 1,
            crashList = listOf(StackTraceElement("declaring class", "methodName", "fileName", 100)),
            crashDate = Date(),
            throwable = "throwable"
        )

    )

    WormaCeptorMainTheme {
        Surface(Modifier.fillMaxSize()) {
            CrashesList(data = flowOf(PagingData.from(data)).collectAsLazyPagingItems()) {

            }
        }
    }
}