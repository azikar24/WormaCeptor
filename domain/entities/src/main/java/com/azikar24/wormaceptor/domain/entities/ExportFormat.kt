package com.azikar24.wormaceptor.domain.entities

/** Supported export formats for network transactions. */
enum class ExportFormat {
    /** WormaCeptor's proprietary JSON format. */
    JSON,

    /** HAR 1.2 (HTTP Archive) industry-standard format. */
    HAR,

    /** cURL command (single transaction only). */
    CURL,
}
