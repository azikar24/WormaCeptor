package com.azikar24.wormaceptor.mcp.server.serialization.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class CpuInfoDto(
    val timestamp: Long,
    val overallUsagePercent: Float,
    val perCoreUsage: List<Float>,
    val coreCount: Int,
    val cpuFrequencyMHz: Long,
    val cpuTemperature: Float?,
    val uptime: Long,
    val measurementSource: String,
)

@Serializable
internal data class MemoryInfoDto(
    val timestamp: Long,
    val usedMemory: Long,
    val freeMemory: Long,
    val totalMemory: Long,
    val maxMemory: Long,
    val heapUsagePercent: Float,
    val nativeHeapSize: Long,
    val nativeHeapAllocated: Long,
    val nativeHeapFree: Long,
    val gcCount: Long,
)

@Serializable
internal data class FpsInfoDto(
    val currentFps: Float,
    val averageFps: Float,
    val minFps: Float,
    val maxFps: Float,
    val droppedFrames: Int,
    val jankFrames: Int,
    val timestamp: Long,
)

@Serializable
internal data class PerformanceSnapshotDto(
    val cpu: CpuInfoDto,
    val memory: MemoryInfoDto,
    val fps: FpsInfoDto,
)
