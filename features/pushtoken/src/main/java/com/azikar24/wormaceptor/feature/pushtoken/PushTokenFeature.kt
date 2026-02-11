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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.PushTokenEngine
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.util.formatDateShort
import com.azikar24.wormaceptor.domain.entities.PushTokenInfo
import com.azikar24.wormaceptor.domain.entities.TokenHistory
import kotlinx.coroutines.flow.StateFlow

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
fun PushTokenManager(context: Context, modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
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
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        Icon(Icons.Default.Notifications, null, tint = WormaCeptorColors.StatusAmber)
                        Text(stringResource(R.string.pushtoken_title), fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    onNavigateBack?.let {
                        IconButton(
                            onClick = it,
                        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.pushtoken_back)) }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchToken() }, enabled = !isLoading) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                Modifier.size(WormaCeptorDesignSystem.IconSize.lg),
                                strokeWidth = WormaCeptorDesignSystem.BorderWidth.thick,
                            )
                        } else {
                            Icon(Icons.Default.Refresh, stringResource(R.string.pushtoken_fetch_token))
                        }
                    }
                },
            )
        },
        snackbarHost = {
            AnimatedVisibility(showCopiedSnackbar, enter = fadeIn(), exit = fadeOut()) {
                Snackbar { Text(stringResource(R.string.pushtoken_token_copied)) }
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            // Error
            error?.let {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = WormaCeptorDesignSystem.Shapes.cardLarge,
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(WormaCeptorDesignSystem.Spacing.md),
                            Arrangement.SpaceBetween,
                            Alignment.CenterVertically,
                        ) {
                            Text(it, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                viewModel.clearError()
                            }) { Icon(Icons.Default.Close, stringResource(R.string.pushtoken_dismiss)) }
                        }
                    }
                }
            }

            // Current token
            item {
                Card(
                    shape = WormaCeptorDesignSystem.Shapes.cardLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(WormaCeptorDesignSystem.Spacing.lg),
                        Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
                    ) {
                        Text(
                            stringResource(R.string.pushtoken_current_token),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        val token = currentToken
                        if (token != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = WormaCeptorDesignSystem.Shapes.card,
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                            ) {
                                Text(
                                    token.token,
                                    Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Row(
                                Modifier.fillMaxWidth(),
                                Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                            ) {
                                Surface(
                                    shape = WormaCeptorDesignSystem.Shapes.chip,
                                    color = WormaCeptorColors.StatusGreen.copy(
                                        alpha = WormaCeptorDesignSystem.Alpha.light,
                                    ),
                                ) {
                                    Text(
                                        token.provider.name,
                                        Modifier.padding(
                                            horizontal = WormaCeptorDesignSystem.Spacing.sm,
                                            vertical = WormaCeptorDesignSystem.Spacing.xs,
                                        ),
                                        color = WormaCeptorColors.StatusGreen,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                                Text(
                                    stringResource(R.string.pushtoken_refreshed, formatDateShort(token.lastRefreshed)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Row(
                                Modifier.fillMaxWidth(),
                                Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                            ) {
                                val clipboardLabel = stringResource(R.string.pushtoken_clipboard_label)
                                Button(onClick = {
                                    clipboardManager.setPrimaryClip(
                                        ClipData.newPlainText(clipboardLabel, token.token),
                                    )
                                    showCopiedSnackbar = true
                                }, Modifier.weight(1f)) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        null,
                                        Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                                    )
                                    Spacer(Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                                    Text(stringResource(R.string.pushtoken_copy))
                                }
                                OutlinedButton(
                                    onClick = {
                                        viewModel.deleteToken()
                                    },
                                    enabled = !isLoading,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = WormaCeptorColors.StatusRed,
                                    ),
                                ) {
                                    Icon(Icons.Default.Delete, null, Modifier.size(WormaCeptorDesignSystem.IconSize.sm))
                                }
                            }
                        } else {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(WormaCeptorDesignSystem.Spacing.xxxl + WormaCeptorDesignSystem.Spacing.xxl),
                                Alignment.Center,
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator()
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.NotificationsOff,
                                            null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.xl),
                                        )
                                        Text(
                                            stringResource(R.string.pushtoken_no_token),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // History
            item {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.pushtoken_token_history),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (tokenHistory.isNotEmpty()) {
                        TextButton(onClick = {
                            viewModel.clearHistory()
                        }) { Text(stringResource(R.string.pushtoken_clear)) }
                    }
                }
            }

            if (tokenHistory.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(WormaCeptorDesignSystem.Spacing.xxxl + WormaCeptorDesignSystem.Spacing.xxxl),
                        Alignment.Center,
                    ) {
                        Text(
                            stringResource(R.string.pushtoken_no_history),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = WormaCeptorDesignSystem.Alpha.heavy,
                            ),
                        )
                    }
                }
            } else {
                items(tokenHistory.take(20), key = { "${it.timestamp}_${it.event}" }) { entry ->
                    Card(
                        shape = WormaCeptorDesignSystem.Shapes.card,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(WormaCeptorDesignSystem.Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val (icon, color) = when (entry.event) {
                                TokenHistory.TokenEvent.CREATED ->
                                    Icons.Default.Add to WormaCeptorColors.StatusGreen
                                TokenHistory.TokenEvent.REFRESHED ->
                                    Icons.Default.Autorenew to WormaCeptorColors.StatusBlue
                                TokenHistory.TokenEvent.INVALIDATED ->
                                    Icons.Default.Warning to WormaCeptorColors.StatusAmber
                                TokenHistory.TokenEvent.DELETED ->
                                    Icons.Default.Delete to WormaCeptorColors.StatusRed
                            }
                            Box(
                                Modifier
                                    .size(WormaCeptorDesignSystem.IconSize.xl)
                                    .clip(WormaCeptorDesignSystem.Shapes.card)
                                    .background(color.copy(alpha = WormaCeptorDesignSystem.Alpha.light)),
                                Alignment.Center,
                            ) {
                                Icon(
                                    icon,
                                    null,
                                    tint = color,
                                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                                )
                            }
                            Spacer(Modifier.width(WormaCeptorDesignSystem.Spacing.md))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    entry.event.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    entry.token.take(20) + "...",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                formatDateShort(entry.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = WormaCeptorDesignSystem.Alpha.heavy,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}
