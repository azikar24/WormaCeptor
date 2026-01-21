/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.pushtoken

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.PushTokenEngine
import com.azikar24.wormaceptor.domain.entities.PushTokenInfo
import com.azikar24.wormaceptor.domain.entities.TokenHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

object PushTokenFeature {
    fun createEngine(context: Context) = PushTokenEngine(context.applicationContext)
    fun createViewModelFactory(engine: PushTokenEngine) = PushTokenViewModelFactory(engine)
}

class PushTokenViewModel(private val engine: PushTokenEngine) : ViewModel() {
    val currentToken: StateFlow<PushTokenInfo?> = engine.currentToken
    val tokenHistory: StateFlow<List<TokenHistory>> = engine.tokenHistory
    val isLoading: StateFlow<Boolean> = engine.isLoading
    val error: StateFlow<String?> = engine.error

    fun fetchToken() = engine.fetchCurrentToken()
    fun refreshToken() = engine.requestNewToken()
    fun deleteToken() = engine.deleteToken()
    fun clearHistory() = engine.clearHistory()
    fun clearError() = engine.clearError()
}

class PushTokenViewModelFactory(private val engine: PushTokenEngine) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PushTokenViewModel(engine) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PushTokenManager(
    context: Context,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine = remember { PushTokenFeature.createEngine(context) }
    val factory = remember { PushTokenFeature.createViewModelFactory(engine) }
    val viewModel: PushTokenViewModel = viewModel(factory = factory)

    val currentToken by viewModel.currentToken.collectAsState()
    val tokenHistory by viewModel.tokenHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val clipboardManager = LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    var showCopiedSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.fetchToken() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Notifications, null, tint = Color(0xFFFF9800))
                    Text("Push Token", fontWeight = FontWeight.SemiBold)
                }},
                navigationIcon = { onNavigateBack?.let { IconButton(onClick = it) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } } },
                actions = {
                    IconButton(onClick = { viewModel.fetchToken() }, enabled = !isLoading) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                        else Icon(Icons.Default.Refresh, "Fetch token")
                    }
                },
            )
        },
        snackbarHost = {
            AnimatedVisibility(showCopiedSnackbar, enter = fadeIn(), exit = fadeOut()) {
                Snackbar { Text("Token copied to clipboard") }
            }
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Error
            error?.let { item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text(it, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearError() }) { Icon(Icons.Default.Close, "Dismiss") }
                    }
                }
            }}

            // Current token
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(12.dp)) {
                        Text("Current Token", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (currentToken != null) {
                            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = Color(0xFFF5F5F5)) {
                                Text(currentToken!!.token, Modifier.padding(12.dp), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            }
                            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF4CAF50).copy(alpha = 0.15f)) {
                                    Text(currentToken!!.provider.name, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
                                }
                                Text("Refreshed: ${formatTime(currentToken!!.lastRefreshed)}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF757575))
                            }
                            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    clipboardManager.setPrimaryClip(ClipData.newPlainText("Push Token", currentToken!!.token))
                                    showCopiedSnackbar = true
                                }, Modifier.weight(1f)) { Icon(Icons.Default.ContentCopy, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Copy") }
                                OutlinedButton(onClick = { viewModel.refreshToken() }, Modifier.weight(1f), enabled = !isLoading) { Icon(Icons.Default.Autorenew, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Refresh") }
                                OutlinedButton(onClick = { viewModel.deleteToken() }, enabled = !isLoading, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF44336))) { Icon(Icons.Default.Delete, null, Modifier.size(18.dp)) }
                            }
                        } else {
                            Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) {
                                if (isLoading) CircularProgressIndicator()
                                else Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.NotificationsOff, null, tint = Color(0xFF9E9E9E), modifier = Modifier.size(32.dp))
                                    Text("No token available", color = Color(0xFF757575))
                                }
                            }
                        }
                    }
                }
            }

            // History
            item {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Token History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (tokenHistory.isNotEmpty()) TextButton(onClick = { viewModel.clearHistory() }) { Text("Clear") }
                }
            }

            if (tokenHistory.isEmpty()) {
                item { Box(Modifier.fillMaxWidth().height(100.dp), Alignment.Center) { Text("No history", color = Color(0xFF9E9E9E)) } }
            } else {
                items(tokenHistory.take(20), key = { "${it.timestamp}_${it.event}" }) { entry ->
                    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            val (icon, color) = when (entry.event) {
                                TokenHistory.TokenEvent.CREATED -> Icons.Default.Add to Color(0xFF4CAF50)
                                TokenHistory.TokenEvent.REFRESHED -> Icons.Default.Autorenew to Color(0xFF2196F3)
                                TokenHistory.TokenEvent.INVALIDATED -> Icons.Default.Warning to Color(0xFFFF9800)
                                TokenHistory.TokenEvent.DELETED -> Icons.Default.Delete to Color(0xFFF44336)
                            }
                            Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.15f)), Alignment.Center) {
                                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(entry.event.name.lowercase().replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Medium)
                                Text(entry.token.take(20) + "...", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, color = Color(0xFF757575))
                            }
                            Text(formatTime(entry.timestamp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E))
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long) = if (timestamp > 0) SimpleDateFormat("MMM d, HH:mm", Locale.US).format(Date(timestamp)) else "--"
