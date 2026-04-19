package com.azikar24.wormaceptor.feature.viewer.vm

/** One-time side effects emitted by [TransactionPagerViewModel] and consumed by the UI. */
internal sealed class TransactionPagerEffect {
    data object HapticFeedback : TransactionPagerEffect()
}
