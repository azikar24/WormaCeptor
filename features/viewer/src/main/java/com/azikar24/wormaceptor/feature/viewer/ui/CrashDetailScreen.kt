package com.azikar24.wormaceptor.feature.viewer.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.domain.entities.Crash
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashDetailScreen(
    crash: Crash,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crash Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { shareCrash(context, crash) }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Exception Type
            Text(
                text = crash.exceptionType,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Timestamp
            Text(
                text = dateFormat.format(Date(crash.timestamp)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Crash Location
            val location = remember(crash.stackTrace) { CrashUtils.extractCrashLocation(crash.stackTrace) }
            if (location != null) {
                DetailRow("File", location)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Message
            if (!crash.message.isNullOrBlank()) {
                SectionHeader(
                    "Message",
                    onCopy = {
                        copyToClipboard(
                            context,
                            "Crash Message",
                            crash.message ?: ""
                        )
                    }
                )
                SelectionContainer {
                    Text(
                        text = crash.message ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Stacktrace
            SectionHeader("Stacktrace", onCopy = { copyToClipboard(context, "Stacktrace", crash.stackTrace) })
            SelectionContainer {
                Text(
                    text = crash.stackTrace,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onCopy: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        if (onCopy != null) {
            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label Copied", Toast.LENGTH_SHORT).show()
}

private fun shareCrash(context: Context, crash: Crash) {
    val shareText = buildString {
        appendLine("--- WormaCeptor Crash Report ---")
        appendLine("Exception: ${crash.exceptionType}")
        appendLine("Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(crash.timestamp))}")
        if (!crash.message.isNullOrBlank()) {
            appendLine("\nMessage:")
            appendLine(crash.message)
        }
        appendLine("\nStacktrace:")
        appendLine(crash.stackTrace)
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "Crash Report: ${crash.exceptionType}")
    }
    context.startActivity(Intent.createChooser(intent, "Share Crash Report"))
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        SelectionContainer {
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
    }
}


