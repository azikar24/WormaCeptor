package com.azikar24.wormaceptor.feature.pushtoken.vm

import com.azikar24.wormaceptor.domain.entities.PushTokenInfo
import com.azikar24.wormaceptor.domain.entities.TokenHistory
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** UI state for the Push Token management screen. */
data class PushTokenViewState(
    val currentToken: PushTokenInfo? = null,
    val tokenHistory: ImmutableList<TokenHistory> = persistentListOf(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
