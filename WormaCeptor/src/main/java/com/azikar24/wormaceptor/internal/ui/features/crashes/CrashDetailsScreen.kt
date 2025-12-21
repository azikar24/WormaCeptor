/*
 * Copyright AziKar24 3/3/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.crashes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.internal.data.CrashTransaction
import com.azikar24.wormaceptor.internal.data.stacktraceData
import com.azikar24.wormaceptor.internal.support.formatted
import com.azikar24.wormaceptor.internal.ui.navigation.NavGraphTypes
import com.azikar24.wormaceptor.ui.components.WormaCeptorToolbar
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator


@Destination(navGraph = NavGraphTypes.HOME_NAV_GRAPH)
@Composable
fun CrashDetailsScreen(
    navigator: DestinationsNavigator,
    crashTransaction: CrashTransaction,
) {
    var title: String? = null
    var subtitle: String? = null
    crashTransaction.throwable?.let {
        val colonPosition = it.indexOf(":")
        title = if (colonPosition > -1) it.substring(0, colonPosition) else it
        subtitle = title.substring(title.lastIndexOf(".").plus(1))
    }

    Column {
        WormaCeptorToolbar.WormaCeptorToolbar(
            title = title.toString(),
            subtitle = subtitle.toString(),
            navController = navigator
        )
        LazyColumn() {
            item {
                Text(
                    text = stringResource(
                        R.string.bracket_string_newline_string,
                        crashTransaction.crashDate?.formatted() ?: "",
                        crashTransaction.throwable ?: ""
                    ),
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(vertical = 10.dp)
                )
            }
            items(crashTransaction.crashList ?: emptyList()) {
                Column {
                    Text(
                        text = it.stacktraceData(LocalContext.current),
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(vertical = 10.dp)
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(0.2f),
                        thickness = 1.dp
                    )
                }
            }
        }

    }
}