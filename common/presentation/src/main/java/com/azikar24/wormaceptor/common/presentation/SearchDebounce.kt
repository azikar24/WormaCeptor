package com.azikar24.wormaceptor.common.presentation

/** Shared debounce durations (ms) for search/filter flows across features. */
object SearchDebounce {
    /** Standard search debounce used for in-memory and light DB-backed filtering. */
    const val DEFAULT = 150L

    /** Longer debounce for expensive lookups (e.g. SQLite queries across many rows). */
    const val HEAVY = 300L
}
