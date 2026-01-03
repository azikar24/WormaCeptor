/*
 * Copyright AziKar24 2024.
 */

package com.azikar24.wormaceptor.internal.ui.features.crashes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val crashTransactionState = viewModel.getCrashWithId(crashId)?.collectAsStateWithLifecycle(null)
    val crashTransaction = crashTransactionState?.value ?: return

    val (title, subtitle) = crashTransaction.throwable?.let {
        val colonPosition = it.indexOf(":")
        val t = if (colonPosition > -1) it.substring(0, colonPosition) else it
        val s = t.substring(t.lastIndexOf(".") + 1)
        t to s
    } ?: (null to null)

    val lifecycleOwner = LocalLifecycleOwner.current
    val errorContainer = MaterialTheme.colorScheme.errorContainer
    val onErrorContainer = MaterialTheme.colorScheme.onErrorContainer

    DisposableEffect(lifecycleOwner, title, subtitle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                toolbarViewModel.reset()
                toolbarViewModel.title = title.toString()
                toolbarViewModel.subtitle = subtitle.toString()
                toolbarViewModel.color = errorContainer
                toolbarViewModel.onColor = onErrorContainer
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Header Section
            item {
                Surface(
                    color = errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = crashTransaction.crashDate?.formatted() ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            color = onErrorContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = crashTransaction.throwable ?: "",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 24.sp
                            ),
                            color = onErrorContainer
                        )
                    }
                }
            }

            // Stack Trace Section Title
            item {
                Text(
                    text = "STACK TRACE",
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
                )
            }

            // Stack Trace Items
            items(crashTransaction.crashList ?: emptyList()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = it.stacktraceData(LocalContext.current),
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
