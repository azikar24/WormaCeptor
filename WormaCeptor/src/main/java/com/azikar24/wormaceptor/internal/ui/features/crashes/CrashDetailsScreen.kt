/*
 * Copyright AziKar24 3/3/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.crashes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.internal.data.CrashTransaction
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
        subtitle = title?.substring(title?.lastIndexOf(".")?.plus(1) ?: 0)
    }

    Column {
        WormaCeptorToolbar.WormaCeptorToolbar(title = title.toString(), subtitle = subtitle.toString(), navController = navigator)

        Column(Modifier.padding(20.dp)) {


            Text(stringResource(R.string.bracket_string_newline_string, crashTransaction.crashDate?.formatted() ?: "", crashTransaction.throwable ?: ""))
            crashTransaction.crashList?.let {
                LazyColumn(modifier = Modifier.padding(top = 20.dp)) {
                    items(it) {
                        Column {
                            Text(stringResource(id = R.string.stack_trace_string,
                                it.className,
                                it.methodName,
                                it.fileName,
                                it.lineNumber
                            ), Modifier.padding(10.dp))
                            Divider(color = MaterialTheme.colors.onSurface.copy(0.2f), thickness = 1.dp)
                        }

                    }
                }
            }

        }

    }
}