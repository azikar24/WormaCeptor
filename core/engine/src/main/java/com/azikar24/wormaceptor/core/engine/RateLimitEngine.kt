/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.entities.RateLimitConfig
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig.NetworkPreset
import com.azikar24.wormaceptor.domain.entities.ThrottleStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

/**
 * Engine that provides network rate limiting capabilities through an OkHttp Interceptor.
 *
 * Features:
 * - Bandwidth throttling (download and upload speed limits)
 * - Latency injection (artificial delay)
 * - Packet loss simulation (random request dropping)
 * - Network condition presets (WiFi, 3G, 2G, EDGE, etc.)
 * - Real-time statistics tracking
 *
 * Usage:
 * 1. Create an instance of RateLimitEngine
 * 2. Get the interceptor via getInterceptor()
 * 3. Add the interceptor to your OkHttpClient
 * 4. Configure rate limiting via setConfig() or applyPreset()
 */
class RateLimitEngine {

    // Current configuration
    private val _config = MutableStateFlow(RateLimitConfig.default())
    val config: StateFlow<RateLimitConfig> = _config.asStateFlow()

    // Throttle statistics
    private val _stats = MutableStateFlow(ThrottleStats.empty())
    val stats: StateFlow<ThrottleStats> = _stats.asStateFlow()

    // Atomic counters for thread-safe stat tracking
    private val requestsThrottled = AtomicInteger(0)
    private val totalDelayMs = AtomicLong(0)
    private val packetsDropped = AtomicInteger(0)
    private val bytesThrottled = AtomicLong(0)

    // The interceptor instance
    private val interceptor = RateLimitInterceptor()

    /**
     * Enables rate limiting with the current configuration.
     */
    fun enable() {
        _config.value = _config.value.copy(enabled = true)
    }

    /**
     * Disables rate limiting.
     */
    fun disable() {
        _config.value = _config.value.copy(enabled = false)
    }

    /**
     * Sets a new rate limiting configuration.
     *
     * @param config The new configuration to apply
     */
    fun setConfig(config: RateLimitConfig) {
        _config.value = config
    }

    /**
     * Applies a network preset configuration.
     *
     * @param preset The network preset to apply
     */
    fun applyPreset(preset: NetworkPreset) {
        _config.value = RateLimitConfig.fromPreset(preset)
    }

    /**
     * Sets custom rate limiting parameters.
     *
     * @param downloadSpeedKbps Download speed limit in kilobits per second
     * @param uploadSpeedKbps Upload speed limit in kilobits per second
     * @param latencyMs Additional latency in milliseconds
     * @param packetLossPercent Packet loss percentage (0-100)
     */
    fun setCustomConfig(
        downloadSpeedKbps: Long,
        uploadSpeedKbps: Long,
        latencyMs: Long,
        packetLossPercent: Float,
    ) {
        _config.value = RateLimitConfig(
            enabled = true,
            downloadSpeedKbps = downloadSpeedKbps,
            uploadSpeedKbps = uploadSpeedKbps,
            latencyMs = latencyMs,
            packetLossPercent = packetLossPercent.coerceIn(0f, 100f),
            preset = null,
        )
    }

    /**
     * Clears all throttle statistics.
     */
    fun clearStats() {
        requestsThrottled.set(0)
        totalDelayMs.set(0)
        packetsDropped.set(0)
        bytesThrottled.set(0)
        updateStats()
    }

    /**
     * Returns the OkHttp Interceptor for rate limiting.
     * Add this to your OkHttpClient.Builder via addInterceptor().
     *
     * @return The rate limiting interceptor
     */
    fun getInterceptor(): Interceptor = interceptor

    /**
     * Updates the stats StateFlow with current values.
     */
    private fun updateStats() {
        _stats.value = ThrottleStats(
            requestsThrottled = requestsThrottled.get(),
            totalDelayMs = totalDelayMs.get(),
            packetsDropped = packetsDropped.get(),
            bytesThrottled = bytesThrottled.get(),
        )
    }

    /**
     * Internal interceptor implementation that applies rate limiting.
     */
    private inner class RateLimitInterceptor : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val currentConfig = _config.value

            // If not enabled, pass through
            if (!currentConfig.enabled) {
                return chain.proceed(chain.request())
            }

            // Apply artificial latency
            if (currentConfig.latencyMs > 0) {
                runBlocking {
                    delay(currentConfig.latencyMs)
                }
                totalDelayMs.addAndGet(currentConfig.latencyMs)
                requestsThrottled.incrementAndGet()
                updateStats()
            }

            // Simulate packet loss
            if (currentConfig.packetLossPercent > 0) {
                val random = Random.nextFloat() * 100f
                if (random < currentConfig.packetLossPercent) {
                    packetsDropped.incrementAndGet()
                    updateStats()
                    throw IOException("Simulated packet loss (${currentConfig.packetLossPercent}% configured)")
                }
            }

            // Throttle upload (request body)
            val request = chain.request()
            val requestBody = request.body
            if (requestBody != null && currentConfig.uploadSpeedKbps > 0) {
                val contentLength = requestBody.contentLength()
                if (contentLength > 0) {
                    val uploadDelayMs = calculateThrottleDelay(contentLength, currentConfig.uploadSpeedKbps)
                    if (uploadDelayMs > 0) {
                        runBlocking {
                            delay(uploadDelayMs)
                        }
                        totalDelayMs.addAndGet(uploadDelayMs)
                        bytesThrottled.addAndGet(contentLength)
                        updateStats()
                    }
                }
            }

            // Proceed with the request
            val response = chain.proceed(request)

            // Throttle download (response body)
            val responseBody = response.body
            if (responseBody != null && currentConfig.downloadSpeedKbps > 0) {
                val throttledBody = ThrottledResponseBody(
                    responseBody = responseBody,
                    downloadSpeedKbps = currentConfig.downloadSpeedKbps,
                )
                return response.newBuilder()
                    .body(throttledBody)
                    .build()
            }

            return response
        }
    }

    /**
     * Calculates the delay needed to simulate a given bandwidth.
     *
     * @param bytes Number of bytes to transfer
     * @param speedKbps Speed limit in kilobits per second
     * @return Delay in milliseconds
     */
    private fun calculateThrottleDelay(bytes: Long, speedKbps: Long): Long {
        if (speedKbps <= 0) return 0

        // Convert Kbps to bytes per second: Kbps * 1000 / 8
        val bytesPerSecond = speedKbps * 1000 / 8

        // Calculate how long it should take to transfer the bytes
        val transferTimeMs = (bytes * 1000) / bytesPerSecond

        return transferTimeMs
    }

    /**
     * A ResponseBody wrapper that throttles read operations.
     */
    private inner class ThrottledResponseBody(
        private val responseBody: ResponseBody,
        private val downloadSpeedKbps: Long,
    ) : ResponseBody() {

        private val throttledSource: BufferedSource by lazy {
            ThrottledSource(responseBody.source(), downloadSpeedKbps).buffer()
        }

        override fun contentType(): MediaType? = responseBody.contentType()

        override fun contentLength(): Long = responseBody.contentLength()

        override fun source(): BufferedSource = throttledSource
    }

    /**
     * A Source wrapper that throttles read operations to simulate bandwidth limits.
     */
    private inner class ThrottledSource(
        source: Source,
        private val downloadSpeedKbps: Long,
    ) : ForwardingSource(source) {

        private var bytesRead = 0L
        private var startTime = System.currentTimeMillis()

        override fun read(sink: Buffer, byteCount: Long): Long {
            val bytesReadNow = super.read(sink, byteCount)

            if (bytesReadNow > 0) {
                bytesRead += bytesReadNow
                bytesThrottled.addAndGet(bytesReadNow)

                // Calculate expected time based on throughput
                val bytesPerSecond = downloadSpeedKbps * 1000 / 8
                if (bytesPerSecond > 0) {
                    val expectedTimeMs = (bytesRead * 1000) / bytesPerSecond
                    val elapsedTime = System.currentTimeMillis() - startTime
                    val sleepTime = expectedTimeMs - elapsedTime

                    if (sleepTime > 0) {
                        runBlocking {
                            delay(sleepTime)
                        }
                        totalDelayMs.addAndGet(sleepTime)
                        requestsThrottled.incrementAndGet()
                        updateStats()
                    }
                }
            }

            return bytesReadNow
        }
    }

    companion object {
        /** Minimum speed in Kbps */
        const val MIN_SPEED_KBPS = 1L

        /** Maximum speed in Kbps (100 Mbps) */
        const val MAX_SPEED_KBPS = 100_000L

        /** Minimum latency in ms */
        const val MIN_LATENCY_MS = 0L

        /** Maximum latency in ms */
        const val MAX_LATENCY_MS = 5000L

        /** Minimum packet loss percent */
        const val MIN_PACKET_LOSS = 0f

        /** Maximum packet loss percent */
        const val MAX_PACKET_LOSS = 100f
    }
}
