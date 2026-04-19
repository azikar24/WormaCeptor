package com.azikar24.wormaceptor.feature.viewer.vm

import java.util.UUID

/** User actions dispatched from the transaction pager UI. */
internal sealed class TransactionPagerEvent {
    data class Initialize(val transactionIds: List<UUID>, val initialIndex: Int) : TransactionPagerEvent()
    data object NavigatePrev : TransactionPagerEvent()
    data object NavigateNext : TransactionPagerEvent()
}
