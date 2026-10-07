package com.azikar24.wormaceptor.mcp.protocol

import kotlinx.serialization.Serializable

@Serializable
internal data class LoadedLibraryDto(
    val name: String,
    val path: String,
    val type: String,
    val size: Long?,
    val loadAddress: String?,
    val version: String?,
    val isSystemLibrary: Boolean,
)

@Serializable
internal data class LibrarySummaryDto(
    val totalLibraries: Int,
    val nativeSoCount: Int,
    val dexCount: Int,
    val jarCount: Int,
    val totalSizeBytes: Long,
    val systemLibraryCount: Int,
    val appLibraryCount: Int,
)

@Serializable
internal data class DependencyInfoDto(
    val name: String,
    val groupId: String?,
    val artifactId: String?,
    val version: String?,
    val category: String,
    val detectionMethod: String,
    val packageName: String,
    val isDetected: Boolean,
    val description: String,
    val website: String?,
    val isInternalDependency: Boolean,
    val mavenCoordinate: String?,
)

@Serializable
internal data class DependencySummaryDto(
    val totalDetected: Int,
    val withVersion: Int,
    val withoutVersion: Int,
    val byCategory: Map<String, Int>,
)
