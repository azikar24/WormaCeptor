package com.azikar24.wormaceptor.domain.entities.mock

/** Artificial delay applied before returning a mocked response. */
sealed class MockDelay {
    /** No delay. */
    data object None : MockDelay()

    /** Fixed delay of [ms] milliseconds. */
    data class Fixed(val ms: Long) : MockDelay()

    /** Random delay between [minMs] and [maxMs] milliseconds. */
    data class Range(val minMs: Long, val maxMs: Long) : MockDelay()
}
