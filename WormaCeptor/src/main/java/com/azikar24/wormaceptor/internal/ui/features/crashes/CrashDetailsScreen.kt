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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.internal.data.stacktraceData
import com.azikar24.wormaceptor.internal.support.formatted
import com.azikar24.wormaceptor.internal.ui.ToolbarViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CrashDetailsScreen(
    crashId: Long,
    viewModel: CrashTransactionViewModel = koinViewModel(),
    toolbarViewModel: ToolbarViewModel = koinViewModel(),
) {
    val crashTransactionState = viewModel.getCrashWithId(crashId)?.observeAsState()
    val crashTransaction = crashTransactionState?.value ?: return

    val (title, subtitle) = crashTransaction.throwable?.let {
        val colonPosition = it.indexOf(":")
        val t = if (colonPosition > -1) it.substring(0, colonPosition) else it
        val s = t.substring(t.lastIndexOf(".") + 1)
        t to s
    } ?: (null to null)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, title, subtitle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                toolbarViewModel.title = title.toString()
                toolbarViewModel.subtitle = subtitle.toString()
                toolbarViewModel.color = null
                toolbarViewModel.onColor = null
                toolbarViewModel.showSearch = false
                toolbarViewModel.menuActions = null
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column {
        LazyColumn {
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
