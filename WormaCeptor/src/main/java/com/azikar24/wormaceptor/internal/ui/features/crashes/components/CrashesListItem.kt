/*
 * Copyright AziKar24 3/3/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.crashes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.R

import com.azikar24.wormaceptor.annotations.ComponentPreviews
import com.azikar24.wormaceptor.internal.data.CrashTransaction
import com.azikar24.wormaceptor.internal.support.formatted
import com.azikar24.wormaceptor.internal.support.FormatUtils
import com.azikar24.wormaceptor.ui.theme.WormaCeptorMainTheme
import java.util.*

@Composable
fun CrashesListItem(
    data: CrashTransaction,
    searchKey: String? = null,
    onClick: (CrashTransaction) -> Unit
) {

    Column(modifier = Modifier
        .background(MaterialTheme.colorScheme.surface)
        .fillMaxWidth()
        .padding(20.dp)
        .clickable {
            onClick(data)
        }) {
        val fileName = data.getClassNameAndLineNumber()
        Text(
            text = if (searchKey.isNullOrEmpty()) {
                FormatUtils.getAnnotatedString(fileName ?: stringResource(R.string.unknown))
            } else {
                FormatUtils.getHighlightedText(fileName ?: stringResource(R.string.unknown), searchKey)
            },
            maxLines = 2
        )
        Text(
            text = if (searchKey.isNullOrEmpty()) {
                FormatUtils.getAnnotatedString(data.throwable ?: "")
            } else {
                FormatUtils.getHighlightedText(data.throwable ?: "", searchKey)
            },
            maxLines = 2,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(text = data.crashDate?.formatted() ?: "", maxLines = 2, modifier = Modifier.padding(top = 4.dp))
    }
}

@ComponentPreviews
@Composable
private fun PreviewCrashesListItem() {

    WormaCeptorMainTheme {
        Surface() {
            CrashesListItem(data = CrashTransaction(1, listOf(), Date(), "throwable")) {

            }
        }
    }
}