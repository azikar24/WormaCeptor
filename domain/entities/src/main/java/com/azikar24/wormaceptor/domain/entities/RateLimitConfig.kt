package com.azikar24.wormaceptor.domain.entities

/**
 * Configuration for network rate limiting.
 *
 * Allows simulation of various network conditions including bandwidth throttling,
 * latency injection, and packet loss. Useful for testing app behavior under
 * different network conditions.
 *
 * @property enabled Whether rate limiting is active
 * @property downloadSpeedKbps Download speed limit in kilobits per second
 * @property uploadSpeedKbps Upload speed limit in kilobits per second
 * @property latencyMs Additional latency to add to each request in milliseconds
 * @property packetLossPercent Percentage of requests to drop (0-100)
 * @property preset The preset network condition, if any
 */
data class RateLimitConfig(
    val enabled: Boolean,
    val downloadSpeedKbps: Long,
    val uploadSpeedKbps: Long,
    val latencyMs: Long,
    val packetLossPercent: Float,
    val preset: NetworkPreset?,
) {
    /**
     * Predefined network condition presets simulating various connection types.
     *
     * @property displayName Human-readable label for the preset.
     * @property downloadKbps Download speed in kilobits per second.
     * @property uploadKbps Upload speed in kilobits per second.
     * @property latencyMs Typical latency in milliseconds.
     * @property packetLoss Typical packet loss percentage.
     */
    enum class NetworkPreset(
        val displayName: String,
        val downloadKbps: Long,
        val uploadKbps: Long,
        val latencyMs: Long,
        val packetLoss: Float,
    ) {
        /** Fast Wi-Fi connection with minimal latency. */
        WIFI("Wi-Fi", 50000, 20000, 10, 0f),

        /** Good 3G mobile connection. */
        GOOD_3G("Good 3G", 2000, 500, 100, 0f),

        /** Average 3G connection with moderate latency. */
        REGULAR_3G("Regular 3G", 750, 250, 200, 1f),

        /** Slow 3G with high latency and some packet loss. */
        SLOW_3G("Slow 3G", 400, 100, 400, 2f),

        /** Good 2G connection. */
        GOOD_2G("Good 2G", 150, 50, 600, 3f),

        /** Slow 2G connection with significant latency. */
        SLOW_2G("Slow 2G", 50, 20, 1000, 5f),

        /** EDGE network with very limited bandwidth. */
        EDGE("EDGE", 35, 10, 1500, 5f),

        /** Simulates a completely offline device (100% packet loss). */
        OFFLINE("Offline", 0, 0, 0, 100f),
    }

    /** Factory methods and preset builders for [RateLimitConfig]. */
    companion object {
        /**
         * Creates a default configuration with rate limiting disabled.
         */
        fun default() = RateLimitConfig(
            enabled = false,
            downloadSpeedKbps = 50000,
            uploadSpeedKbps = 20000,
            latencyMs = 0,
            packetLossPercent = 0f,
            preset = null,
        )

        /**
         * Creates a configuration from a network preset.
         *
         * @param preset The network preset to apply
         * @return A RateLimitConfig configured with the preset values
         */
        fun fromPreset(preset: NetworkPreset) = RateLimitConfig(
            enabled = true,
            downloadSpeedKbps = preset.downloadKbps,
            uploadSpeedKbps = preset.uploadKbps,
            latencyMs = preset.latencyMs,
            packetLossPercent = preset.packetLoss,
            preset = preset,
        )
    }
}

/**
 * Statistics about throttled network operations.
 *
 * @property requestsThrottled Number of requests that were rate limited
 * @property totalDelayMs Total delay added across all requests in milliseconds
 * @property packetsDropped Number of requests dropped due to simulated packet loss
 * @property bytesThrottled Total bytes that were throttled
 */
data class ThrottleStats(
    val requestsThrottled: Int,
    val totalDelayMs: Long,
    val packetsDropped: Int,
    val bytesThrottled: Long,
) {
    /** Factory methods for [ThrottleStats]. */
    companion object {
        /**
         * Creates an empty stats instance with zero values.
         */
        fun empty() = ThrottleStats(
            requestsThrottled = 0,
            totalDelayMs = 0L,
            packetsDropped = 0,
            bytesThrottled = 0L,
        )
    }
}
