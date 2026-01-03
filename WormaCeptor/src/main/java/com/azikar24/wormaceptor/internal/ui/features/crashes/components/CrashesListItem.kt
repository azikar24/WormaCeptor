package com.azikar24.wormaceptor.internal.ui.features.crashes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.annotations.ComponentPreviews
import com.azikar24.wormaceptor.internal.data.CrashTransaction
import com.azikar24.wormaceptor.internal.support.FormatUtils
import com.azikar24.wormaceptor.internal.support.formatted
import com.azikar24.wormaceptor.ui.theme.WormaCeptorMainTheme
import java.util.Date

@Composable
fun CrashesListItem(
    data: CrashTransaction,
    searchKey: String? = null,
    onClick: (CrashTransaction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(data) }
            .background(MaterialTheme.colorScheme.surface)
            .height(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(6.dp)
                .background(MaterialTheme.colorScheme.error)
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.extraSmall,
                ) {
                    Text(
                        text = "CRASH",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = data.crashDate?.formatted() ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }

            val fileName = data.getClassNameAndLineNumber()
            Text(
                text = if (searchKey.isNullOrEmpty()) {
                    FormatUtils.getAnnotatedString(fileName ?: stringResource(R.string.unknown))
                } else {
                    FormatUtils.getHighlightedText(
                        fileName ?: stringResource(R.string.unknown),
                        searchKey
                    )
                },
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.error,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = if (searchKey.isNullOrEmpty()) {
                    FormatUtils.getAnnotatedString(data.throwable ?: "")
                } else {
                    FormatUtils.getHighlightedText(data.throwable ?: "", searchKey)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@ComponentPreviews
@Composable
private fun PreviewCrashesListItem() {
    WormaCeptorMainTheme {
        Surface {
            CrashesListItem(
                data = CrashTransaction(
                    id = 1,
                    crashList = listOf(),
                    crashDate = Date(),
                    throwable = "java.lang.NullPointerException: Attempt to invoke virtual method on a null object reference"
                )
            ) {

            }
        }
    }
}
