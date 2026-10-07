package com.azikar24.wormaceptor.mcp.server.serialization

import com.azikar24.wormaceptor.domain.entities.ColumnInfo
import com.azikar24.wormaceptor.domain.entities.CpuInfo
import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.domain.entities.DatabaseInfo
import com.azikar24.wormaceptor.domain.entities.DependencyInfo
import com.azikar24.wormaceptor.domain.entities.DependencySummary
import com.azikar24.wormaceptor.domain.entities.DeviceInfo
import com.azikar24.wormaceptor.domain.entities.FileEntry
import com.azikar24.wormaceptor.domain.entities.FileInfo
import com.azikar24.wormaceptor.domain.entities.FpsInfo
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.domain.entities.LeakSummary
import com.azikar24.wormaceptor.domain.entities.LibrarySummary
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary
import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.domain.entities.LogEntry
import com.azikar24.wormaceptor.domain.entities.MemoryInfo
import com.azikar24.wormaceptor.domain.entities.MockLocation
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.NotificationChannelInfo
import com.azikar24.wormaceptor.domain.entities.PreferenceFile
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PushTokenInfo
import com.azikar24.wormaceptor.domain.entities.QueryResult
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.Response
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry
import com.azikar24.wormaceptor.domain.entities.SecureStorageSummary
import com.azikar24.wormaceptor.domain.entities.SimulatedNotification
import com.azikar24.wormaceptor.domain.entities.TableInfo
import com.azikar24.wormaceptor.domain.entities.ThreadViolation
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.domain.entities.ViolationStats
import com.azikar24.wormaceptor.domain.entities.WebSocketConnection
import com.azikar24.wormaceptor.domain.entities.WebSocketMessage
import com.azikar24.wormaceptor.mcp.protocol.AppDetailsDto
import com.azikar24.wormaceptor.mcp.protocol.ColumnInfoDto
import com.azikar24.wormaceptor.mcp.protocol.CpuInfoDto
import com.azikar24.wormaceptor.mcp.protocol.CrashDto
import com.azikar24.wormaceptor.mcp.protocol.CrashSummaryDto
import com.azikar24.wormaceptor.mcp.protocol.DatabaseInfoDto
import com.azikar24.wormaceptor.mcp.protocol.DependencyInfoDto
import com.azikar24.wormaceptor.mcp.protocol.DependencySummaryDto
import com.azikar24.wormaceptor.mcp.protocol.DeviceDetailsDto
import com.azikar24.wormaceptor.mcp.protocol.DeviceInfoDto
import com.azikar24.wormaceptor.mcp.protocol.FileEntryDto
import com.azikar24.wormaceptor.mcp.protocol.FileInfoDto
import com.azikar24.wormaceptor.mcp.protocol.FpsInfoDto
import com.azikar24.wormaceptor.mcp.protocol.LeakInfoDto
import com.azikar24.wormaceptor.mcp.protocol.LeakSummaryDto
import com.azikar24.wormaceptor.mcp.protocol.LibrarySummaryDto
import com.azikar24.wormaceptor.mcp.protocol.LoadedLibraryDto
import com.azikar24.wormaceptor.mcp.protocol.LocationPresetDto
import com.azikar24.wormaceptor.mcp.protocol.LogEntryDto
import com.azikar24.wormaceptor.mcp.protocol.MemoryDetailsDto
import com.azikar24.wormaceptor.mcp.protocol.MemoryInfoDto
import com.azikar24.wormaceptor.mcp.protocol.MockLocationDto
import com.azikar24.wormaceptor.mcp.protocol.NetworkDetailsDto
import com.azikar24.wormaceptor.mcp.protocol.NotificationChannelInfoDto
import com.azikar24.wormaceptor.mcp.protocol.OsDetailsDto
import com.azikar24.wormaceptor.mcp.protocol.PreferenceDto
import com.azikar24.wormaceptor.mcp.protocol.PreferenceFileDto
import com.azikar24.wormaceptor.mcp.protocol.PushTokenInfoDto
import com.azikar24.wormaceptor.mcp.protocol.QueryResultDto
import com.azikar24.wormaceptor.mcp.protocol.RateLimitConfigDto
import com.azikar24.wormaceptor.mcp.protocol.RequestDto
import com.azikar24.wormaceptor.mcp.protocol.ResponseDto
import com.azikar24.wormaceptor.mcp.protocol.ScreenDetailsDto
import com.azikar24.wormaceptor.mcp.protocol.SecureStorageEntryDto
import com.azikar24.wormaceptor.mcp.protocol.SecureStorageSummaryDto
import com.azikar24.wormaceptor.mcp.protocol.SimulatedNotificationDto
import com.azikar24.wormaceptor.mcp.protocol.StorageDetailsDto
import com.azikar24.wormaceptor.mcp.protocol.TableInfoDto
import com.azikar24.wormaceptor.mcp.protocol.ThreadViolationDto
import com.azikar24.wormaceptor.mcp.protocol.TransactionDetailDto
import com.azikar24.wormaceptor.mcp.protocol.TransactionSummaryDto
import com.azikar24.wormaceptor.mcp.protocol.ViolationStatsDto
import com.azikar24.wormaceptor.mcp.protocol.WebSocketConnectionDto
import com.azikar24.wormaceptor.mcp.protocol.WebSocketMessageDto
import com.azikar24.wormaceptor.mcp.server.middleware.RedactionHelper

private val SENSITIVE_HEADERS = setOf(
    "Authorization",
    "Cookie",
    "Set-Cookie",
    "Proxy-Authorization",
    "X-Api-Key",
    "X-Auth-Token",
)

// region Network

internal fun TransactionSummary.toDto() = TransactionSummaryDto(
    id = id.toString(),
    method = method,
    url = url.ifEmpty { host + path },
    host = host,
    path = path,
    code = code,
    tookMs = tookMs,
    hasRequestBody = hasRequestBody,
    hasResponseBody = hasResponseBody,
    status = status.name,
    timestamp = timestamp,
)

internal fun NetworkTransaction.toDetailDto() = TransactionDetailDto(
    id = id.toString(),
    timestamp = timestamp,
    durationMs = durationMs,
    status = status.name,
    request = request.toDto(),
    response = response?.toDto(),
    extensions = extensions,
)

internal fun Request.toDto() = RequestDto(
    url = url,
    method = method,
    headers = RedactionHelper.redactHeaders(headers, SENSITIVE_HEADERS),
    bodySize = bodySize,
)

internal fun Response.toDto() = ResponseDto(
    code = code,
    message = message,
    headers = RedactionHelper.redactHeaders(headers, SENSITIVE_HEADERS),
    error = error,
    protocol = protocol,
    tlsVersion = tlsVersion,
    bodySize = bodySize,
)

internal fun RateLimitConfig.toDto() = RateLimitConfigDto(
    enabled = enabled,
    downloadSpeedKbps = downloadSpeedKbps,
    uploadSpeedKbps = uploadSpeedKbps,
    latencyMs = latencyMs,
    packetLossPercent = packetLossPercent,
    preset = preset?.name,
)

// endregion

// region Crash

internal fun Crash.toDto() = CrashDto(
    id = id,
    timestamp = timestamp,
    exceptionType = exceptionType,
    message = message,
    stackTrace = stackTrace,
)

internal fun Crash.toSummaryDto() = CrashSummaryDto(
    id = id,
    timestamp = timestamp,
    exceptionType = exceptionType,
    message = message,
)

// endregion

// region Log

internal fun LogEntry.toDto() = LogEntryDto(
    id = id,
    timestamp = timestamp,
    level = level.name,
    tag = tag,
    pid = pid,
    tid = tid,
    message = message,
)

// endregion

// region Performance

internal fun CpuInfo.toDto() = CpuInfoDto(
    timestamp = timestamp,
    overallUsagePercent = overallUsagePercent,
    perCoreUsage = perCoreUsage,
    coreCount = coreCount,
    cpuFrequencyMHz = cpuFrequencyMHz,
    cpuTemperature = cpuTemperature,
    uptime = uptime,
    measurementSource = measurementSource.name,
)

internal fun MemoryInfo.toDto() = MemoryInfoDto(
    timestamp = timestamp,
    usedMemory = usedMemory,
    freeMemory = freeMemory,
    totalMemory = totalMemory,
    maxMemory = maxMemory,
    heapUsagePercent = heapUsagePercent,
    nativeHeapSize = nativeHeapSize,
    nativeHeapAllocated = nativeHeapAllocated,
    nativeHeapFree = nativeHeapFree,
    gcCount = gcCount,
)

internal fun FpsInfo.toDto() = FpsInfoDto(
    currentFps = currentFps,
    averageFps = averageFps,
    minFps = minFps,
    maxFps = maxFps,
    droppedFrames = droppedFrames,
    jankFrames = jankFrames,
    timestamp = timestamp,
)

// endregion

// region Diagnostic

internal fun LeakInfo.toDto() = LeakInfoDto(
    timestamp = timestamp,
    objectClass = objectClass,
    leakDescription = leakDescription,
    retainedSize = retainedSize,
    referencePath = referencePath,
    severity = severity.name,
)

internal fun LeakSummary.toDto() = LeakSummaryDto(
    totalLeaks = totalLeaks,
    criticalCount = criticalCount,
    highCount = highCount,
    mediumCount = mediumCount,
    lowCount = lowCount,
    totalRetainedBytes = totalRetainedBytes,
)

internal fun ThreadViolation.toDto() = ThreadViolationDto(
    id = id,
    timestamp = timestamp,
    violationType = violationType.name,
    description = description,
    stackTrace = stackTrace,
    durationMs = durationMs,
    threadName = threadName,
)

internal fun ViolationStats.toDto() = ViolationStatsDto(
    totalViolations = totalViolations,
    diskReadCount = diskReadCount,
    diskWriteCount = diskWriteCount,
    networkCount = networkCount,
    slowCallCount = slowCallCount,
    customSlowCodeCount = customSlowCodeCount,
)

// endregion

// region Storage

internal fun PreferenceItem.toDto() = PreferenceDto(
    key = key,
    value = value.displayValue,
    type = value.typeName,
)

internal fun PreferenceFile.toDto(entries: List<PreferenceDto> = emptyList()) = PreferenceFileDto(
    name = name,
    itemCount = itemCount,
    entries = entries,
)

internal fun DatabaseInfo.toDto() = DatabaseInfoDto(
    name = name,
    path = path,
    sizeBytes = sizeBytes,
    tableCount = tableCount,
)

internal fun TableInfo.toDto() = TableInfoDto(
    name = name,
    rowCount = rowCount,
    columnCount = columnCount,
)

internal fun ColumnInfo.toDto() = ColumnInfoDto(
    name = name,
    type = type,
    isPrimaryKey = isPrimaryKey,
    isNullable = isNullable,
)

internal fun QueryResult.toDto() = QueryResultDto(
    columns = columns,
    rows = rows.map { row -> row.map { it?.toString() } },
    rowCount = rowCount,
    error = error,
)

internal fun FileEntry.toDto() = FileEntryDto(
    name = name,
    path = path,
    isDirectory = isDirectory,
    sizeBytes = sizeBytes,
    lastModified = lastModified,
    permissions = permissions,
    isReadable = isReadable,
    isWritable = isWritable,
)

internal fun FileInfo.toDto() = FileInfoDto(
    name = name,
    path = path,
    sizeBytes = sizeBytes,
    lastModified = lastModified,
    mimeType = mimeType,
    isReadable = isReadable,
    isWritable = isWritable,
    extension = extension,
    parentPath = parentPath,
)

// endregion

// region Security

internal fun SecureStorageEntry.toDto() = SecureStorageEntryDto(
    key = key,
    storageType = storageType.name,
    isEncrypted = isEncrypted,
    lastModified = lastModified,
)

internal fun SecureStorageSummary.toDto() = SecureStorageSummaryDto(
    encryptedPrefsCount = encryptedPrefsCount,
    keystoreAliasCount = keystoreAliasCount,
    dataStoreFileCount = dataStoreFileCount,
    totalCount = totalCount,
)

// endregion

// region WebSocket

internal fun WebSocketConnection.toDto() = WebSocketConnectionDto(
    id = id,
    url = url,
    state = state.name,
    openedAt = openedAt,
    closedAt = closedAt,
    closeCode = closeCode,
    closeReason = closeReason,
    duration = duration,
    isActive = isActive,
)

internal fun WebSocketMessage.toDto() = WebSocketMessageDto(
    id = id,
    connectionId = connectionId,
    type = type.name,
    direction = direction.name,
    payload = payload,
    timestamp = timestamp,
    size = size,
)

// endregion

// region DeviceInfo

internal fun DeviceInfo.toDto() = DeviceInfoDto(
    device = device.toDto(),
    os = os.toDto(),
    screen = screen.toDto(),
    memory = memory.toDto(),
    storage = storage.toDto(),
    app = app.toDto(),
    network = network.toDto(),
    timestamp = timestamp,
)

private fun com.azikar24.wormaceptor.domain.entities.DeviceDetails.toDto() = DeviceDetailsDto(
    manufacturer = manufacturer,
    model = model,
    brand = brand,
    device = device,
    hardware = hardware,
    board = board,
    product = product,
    isEmulator = isEmulator,
)

private fun com.azikar24.wormaceptor.domain.entities.OsDetails.toDto() = OsDetailsDto(
    androidVersion = androidVersion,
    sdkLevel = sdkLevel,
    buildId = buildId,
    securityPatch = securityPatch,
    bootloader = bootloader,
    fingerprint = fingerprint,
    incremental = incremental,
)

private fun com.azikar24.wormaceptor.domain.entities.ScreenDetails.toDto() = ScreenDetailsDto(
    widthPixels = widthPixels,
    heightPixels = heightPixels,
    densityDpi = densityDpi,
    density = density,
    scaledDensity = scaledDensity,
    sizeCategory = sizeCategory,
    orientation = orientation,
    refreshRate = refreshRate,
)

private fun com.azikar24.wormaceptor.domain.entities.MemoryDetails.toDto() = MemoryDetailsDto(
    totalRam = totalRam,
    availableRam = availableRam,
    lowMemoryThreshold = lowMemoryThreshold,
    isLowMemory = isLowMemory,
    usedRam = usedRam,
    usagePercentage = usagePercentage,
)

private fun com.azikar24.wormaceptor.domain.entities.StorageDetails.toDto() = StorageDetailsDto(
    internalTotal = internalTotal,
    internalAvailable = internalAvailable,
    internalUsed = internalUsed,
    externalTotal = externalTotal,
    externalAvailable = externalAvailable,
    externalUsed = externalUsed,
    hasExternalStorage = hasExternalStorage,
)

private fun com.azikar24.wormaceptor.domain.entities.AppDetails.toDto() = AppDetailsDto(
    packageName = packageName,
    versionName = versionName,
    versionCode = versionCode,
    targetSdk = targetSdk,
    minSdk = minSdk,
    firstInstallTime = firstInstallTime,
    lastUpdateTime = lastUpdateTime,
    isDebuggable = isDebuggable,
)

private fun com.azikar24.wormaceptor.domain.entities.NetworkDetails.toDto() = NetworkDetailsDto(
    connectionType = connectionType,
    isConnected = isConnected,
    isWifiConnected = isWifiConnected,
    isCellularConnected = isCellularConnected,
    isMetered = isMetered,
    wifiSsid = wifiSsid,
    cellularNetworkType = cellularNetworkType,
)

// endregion

// region Library

internal fun LoadedLibrary.toDto() = LoadedLibraryDto(
    name = name,
    path = path,
    type = type.name,
    size = size,
    loadAddress = loadAddress,
    version = version,
    isSystemLibrary = isSystemLibrary,
)

internal fun LibrarySummary.toDto() = LibrarySummaryDto(
    totalLibraries = totalLibraries,
    nativeSoCount = nativeSoCount,
    dexCount = dexCount,
    jarCount = jarCount,
    totalSizeBytes = totalSizeBytes,
    systemLibraryCount = systemLibraryCount,
    appLibraryCount = appLibraryCount,
)

internal fun DependencyInfo.toDto() = DependencyInfoDto(
    name = name,
    groupId = groupId,
    artifactId = artifactId,
    version = version,
    category = category.name,
    detectionMethod = detectionMethod.name,
    packageName = packageName,
    isDetected = isDetected,
    description = description,
    website = website,
    isInternalDependency = isInternalDependency,
    mavenCoordinate = mavenCoordinate,
)

internal fun DependencySummary.toDto() = DependencySummaryDto(
    totalDetected = totalDetected,
    withVersion = withVersion,
    withoutVersion = withoutVersion,
    byCategory = byCategory.map { (key, value) -> key.name to value }.toMap(),
)

// endregion

// region Push

internal fun PushTokenInfo.toDto() = PushTokenInfoDto(
    token = token,
    provider = provider.name,
    createdAt = createdAt,
    lastRefreshed = lastRefreshed,
    isValid = isValid,
    associatedUserId = associatedUserId,
    metadata = metadata,
)

internal fun SimulatedNotification.toDto() = SimulatedNotificationDto(
    id = id,
    title = title,
    body = body,
    channelId = channelId,
    priority = priority.name,
    extras = extras,
    timestamp = timestamp,
)

internal fun NotificationChannelInfo.toDto() = NotificationChannelInfoDto(
    id = id,
    name = name,
    description = description,
    importance = importance,
)

// endregion

// region Location

internal fun MockLocation.toDto() = MockLocationDto(
    latitude = latitude,
    longitude = longitude,
    altitude = altitude,
    accuracy = accuracy,
    speed = speed,
    bearing = bearing,
    timestamp = timestamp,
    name = name,
)

internal fun LocationPreset.toDto() = LocationPresetDto(
    id = id,
    name = name,
    location = location.toDto(),
    isBuiltIn = isBuiltIn,
)

// endregion
