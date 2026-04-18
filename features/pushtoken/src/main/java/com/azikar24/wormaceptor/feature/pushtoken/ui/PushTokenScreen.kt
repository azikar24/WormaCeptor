package com.azikar24.wormaceptor.feature.pushtoken.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.appbar.WormaCeptorTopBar
import com.azikar24.wormaceptor.core.ui.components.button.ButtonVariant
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.PushTokenInfo
import com.azikar24.wormaceptor.domain.entities.TokenHistory
import com.azikar24.wormaceptor.feature.pushtoken.R
import com.azikar24.wormaceptor.feature.pushtoken.vm.PushTokenViewEvent
import com.azikar24.wormaceptor.feature.pushtoken.vm.PushTokenViewModel
import com.azikar24.wormaceptor.feature.pushtoken.vm.PushTokenViewState
import kotlinx.collections.immutable.persistentListOf

private val EmptyHistoryHeight = 96.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PushTokenScreen(
    state: PushTokenViewState,
    onEvent: (PushTokenViewEvent) -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    showCopiedSnackbar: Boolean = false,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        snackbarHost = {
            AnimatedVisibility(showCopiedSnackbar, enter = fadeIn(), exit = fadeOut()) {
                Snackbar { Text(stringResource(R.string.pushtoken_token_copied)) }
            }
        },
        topBar = { PushTokenTopAppBar(state = state, onEvent = onEvent, onBack = onBack) },
    ) { padding ->
        PushTokenContent(state = state, onEvent = onEvent, padding = padding)
    }
}

@Composable
private fun PushTokenTopAppBar(
    state: PushTokenViewState,
    onEvent: (PushTokenViewEvent) -> Unit,
    onBack: (() -> Unit)?,
) {
    WormaCeptorTopBar(
        title = stringResource(R.string.pushtoken_title),
        onBack = onBack,
        backContentDescription = stringResource(R.string.pushtoken_back),
        actions = {
            IconButton(
                onClick = { onEvent(PushTokenViewEvent.FetchToken) },
                enabled = !state.isLoading,
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        Modifier.size(WormaCeptorTokens.IconSize.lg),
                        strokeWidth = WormaCeptorTokens.BorderWidth.thick,
                    )
                } else {
                    Icon(Icons.Default.Refresh, stringResource(R.string.pushtoken_fetch_token))
                }
            }
        },
    )
}

@Composable
private fun PushTokenContent(
    state: PushTokenViewState,
    onEvent: (PushTokenViewEvent) -> Unit,
    padding: PaddingValues,
) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(
            start = WormaCeptorTokens.Spacing.lg,
            top = WormaCeptorTokens.Spacing.lg,
            end = WormaCeptorTokens.Spacing.lg,
            bottom = WormaCeptorTokens.Spacing.lg +
                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
    ) {
        state.error?.let { errorMessage ->
            item {
                PushTokenErrorCard(
                    message = errorMessage,
                    onDismiss = { onEvent(PushTokenViewEvent.DismissError) },
                )
            }
        }

        item {
            PushTokenCurrentTokenCard(token = state.currentToken, isLoading = state.isLoading, onEvent = onEvent)
        }

        historySection(state = state, onEvent = onEvent)
    }
}

private fun LazyListScope.historySection(
    state: PushTokenViewState,
    onEvent: (PushTokenViewEvent) -> Unit,
) {
    item {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = WormaCeptorTokens.TouchTarget.comfortable),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.pushtoken_token_history),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (state.tokenHistory.isNotEmpty()) {
                WormaCeptorButton(
                    text = stringResource(R.string.pushtoken_clear),
                    onClick = { onEvent(PushTokenViewEvent.ClearHistory) },
                    variant = ButtonVariant.Text,
                )
            }
        }
    }

    if (state.tokenHistory.isEmpty()) {
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(EmptyHistoryHeight),
                Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.pushtoken_no_history),
                    color = WormaCeptorTokens.semantic().textSecondary.copy(
                        alpha = WormaCeptorTokens.Alpha.HEAVY,
                    ),
                )
            }
        }
    } else {
        items(
            state.tokenHistory.take(PushTokenViewModel.MAX_DISPLAY_HISTORY),
            key = { "${it.timestamp}_${it.event}_${it.token.hashCode()}" },
        ) { entry ->
            PushTokenHistoryItem(entry = entry)
        }
    }
}

@Composable
private fun PushTokenErrorCard(
    message: String,
    onDismiss: () -> Unit,
) {
    WormaCeptorCard(
        style = CardStyle.Outlined,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.md),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically,
        ) {
            Text(
                message,
                color = WormaCeptorTokens.semantic().error,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, stringResource(R.string.pushtoken_dismiss))
            }
        }
    }
}

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview(showBackground = true)
@Composable
private fun PushTokenScreenWithTokenPreview() {
    WormaCeptorTheme {
        PushTokenScreen(
            state = PushTokenViewState(
                currentToken = PushTokenInfo(
                    token = "dGhpcyBpcyBhIHNhbXBsZSBGQ00gcHVzaCB0b2tlbg==:APA91bHPRgkT",
                    provider = PushTokenInfo.PushProvider.FCM,
                    createdAt = 1_712_000_000_000L,
                    lastRefreshed = 1_712_300_000_000L,
                    isValid = true,
                    associatedUserId = null,
                    metadata = emptyMap(),
                ),
                tokenHistory = persistentListOf(
                    TokenHistory(
                        token = "dGhpcyBpcyBhIHNhbXBsZSBGQ00gcHVzaCB0b2tlbg==:APA91bHPRgkT",
                        timestamp = 1_712_300_000_000L,
                        event = TokenHistory.TokenEvent.REFRESHED,
                    ),
                    TokenHistory(
                        token = "dGhpcyBpcyBhIHNhbXBsZSBGQ00gcHVzaCB0b2tlbg==:APA91bHPRgkT",
                        timestamp = 1_712_000_000_000L,
                        event = TokenHistory.TokenEvent.CREATED,
                    ),
                ),
            ),
            onEvent = {},
            onBack = {},
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun PushTokenScreenEmptyPreview() {
    WormaCeptorTheme {
        PushTokenScreen(
            state = PushTokenViewState(),
            onEvent = {},
            onBack = {},
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun PushTokenScreenErrorPreview() {
    WormaCeptorTheme {
        PushTokenScreen(
            state = PushTokenViewState(
                error = "No push token available. Firebase may not be configured.",
            ),
            onEvent = {},
            onBack = {},
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun PushTokenScreenLoadingPreview() {
    WormaCeptorTheme {
        PushTokenScreen(
            state = PushTokenViewState(isLoading = true),
            onEvent = {},
            onBack = {},
        )
    }
}
