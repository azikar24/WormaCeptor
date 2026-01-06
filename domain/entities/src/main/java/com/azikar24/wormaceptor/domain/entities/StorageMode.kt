package com.azikar24.wormaceptor.domain.entities

/**
 * Defines how WormaCeptor should store captured data.
 */
enum class StorageMode {
    /**
     * persistent storage in SQLite and FileSystem.
     */
    PERSISTENT,

    /**
     * RAM-only storage. Data is lost when the process is killed.
     */
    MEMORY,

    /**
     * No data is captured or stored. Interceptor becomes a pass-through.
     */
    NO_OP
}
