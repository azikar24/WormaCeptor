package com.azikar24.wormaceptor.core.engine

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Zip layout constants (PKWARE APPNOTE): signatures, record sizes and field offsets. */
private object Zip {
    const val EOCD_SIGNATURE = 0x06054b50
    const val EOCD_MIN_SIZE = 22
    const val EOCD_CD_SIZE = 12
    const val EOCD_CD_OFFSET = 16
    const val MAX_COMMENT_SIZE = 0xFFFF

    const val CD_SIGNATURE = 0x02014b50
    const val CD_HEADER_SIZE = 46
    const val CD_COMPRESSED_SIZE = 20
    const val CD_NAME_LENGTH = 28
    const val CD_EXTRA_LENGTH = 30
    const val CD_COMMENT_LENGTH = 32
    const val CD_LOCAL_HEADER_OFFSET = 42

    const val LOCAL_HEADER_SIZE = 30
    const val LOCAL_NAME_LENGTH = 26
    const val LOCAL_EXTRA_LENGTH = 28

    const val ZIP64_MARKER = 0xFFFFFFFFL
    const val UINT16_MASK = 0xFFFF
    const val UINT32_MASK = 0xFFFFFFFFL
}

private const val MapsOffsetColumn = 2
private const val MapsPathColumn = 5
private const val MapsColumnCount = 6

/**
 * Groups the file offsets of `.apk` mappings in /proc/self/maps lines by APK path. A library loaded
 * straight from the APK (extractNativeLibs=false) shows up as the APK itself at the offset of its data.
 */
internal fun apkMappingOffsets(mapsLines: List<String>): Map<String, List<Long>> = mapsLines
    .mapNotNull { line ->
        val parts = line.trim().split(Regex("\\s+"), limit = MapsColumnCount)
        val path = parts.getOrNull(MapsPathColumn)?.takeIf { it.endsWith(".apk") } ?: return@mapNotNull null
        val offset = parts[MapsOffsetColumn].toLongOrNull(radix = 16) ?: return@mapNotNull null
        path to offset
    }
    .groupBy({ it.first }, { it.second })

/** Entries of [entryRanges] whose data range contains at least one of [mappedOffsets], sorted by name. */
internal fun mappedEntries(
    entryRanges: Map<String, LongRange>,
    mappedOffsets: List<Long>,
): List<String> = entryRanges
    .filterValues { range -> mappedOffsets.any { it in range } }
    .keys
    .sorted()

/**
 * Reads the zip central directory of [file] and returns the file-offset range of the raw data of each
 * entry matching [include]. java.util.zip doesn't expose entry offsets. Zip64 archives yield nothing.
 */
internal fun readZipEntryDataRanges(
    file: File,
    include: (String) -> Boolean,
): Map<String, LongRange> = RandomAccessFile(file, "r").use { raf ->
    val eocd = findEndOfCentralDirectory(raf) ?: return emptyMap()
    val cdSize = eocd.uint32(Zip.EOCD_CD_SIZE)
    val cdOffset = eocd.uint32(Zip.EOCD_CD_OFFSET)
    if (cdOffset == Zip.ZIP64_MARKER || cdOffset + cdSize > raf.length()) return emptyMap()

    val cd = readBuffer(raf, cdOffset, cdSize.toInt())
    val result = mutableMapOf<String, LongRange>()
    while (cd.remaining() >= Zip.CD_HEADER_SIZE && cd.getInt(cd.position()) == Zip.CD_SIGNATURE) {
        val start = cd.position()
        val compressedSize = cd.uint32(start + Zip.CD_COMPRESSED_SIZE)
        val nameLength = cd.uint16(start + Zip.CD_NAME_LENGTH)
        val skipLength = cd.uint16(start + Zip.CD_EXTRA_LENGTH) + cd.uint16(start + Zip.CD_COMMENT_LENGTH)
        val localHeaderOffset = cd.uint32(start + Zip.CD_LOCAL_HEADER_OFFSET)
        val nameBytes = ByteArray(nameLength)
        cd.position(start + Zip.CD_HEADER_SIZE)
        cd.get(nameBytes)
        val name = String(nameBytes, Charsets.UTF_8)
        cd.position(start + Zip.CD_HEADER_SIZE + nameLength + skipLength)

        if (include(name) && compressedSize != Zip.ZIP64_MARKER && localHeaderOffset != Zip.ZIP64_MARKER) {
            val local = readBuffer(raf, localHeaderOffset, Zip.LOCAL_HEADER_SIZE)
            val dataStart = localHeaderOffset + Zip.LOCAL_HEADER_SIZE +
                local.uint16(Zip.LOCAL_NAME_LENGTH) + local.uint16(Zip.LOCAL_EXTRA_LENGTH)
            result[name] = dataStart..<dataStart + compressedSize
        }
    }
    result
}

private fun ByteBuffer.uint16(index: Int): Int = getShort(index).toInt() and Zip.UINT16_MASK

private fun ByteBuffer.uint32(index: Int): Long = getInt(index).toLong() and Zip.UINT32_MASK

private fun findEndOfCentralDirectory(raf: RandomAccessFile): ByteBuffer? {
    val length = raf.length()
    if (length < Zip.EOCD_MIN_SIZE) return null
    val searchStart = maxOf(0L, length - Zip.EOCD_MIN_SIZE - Zip.MAX_COMMENT_SIZE)
    val tail = readBuffer(raf, searchStart, (length - searchStart).toInt())
    val position = (tail.limit() - Zip.EOCD_MIN_SIZE downTo 0).firstOrNull { tail.getInt(it) == Zip.EOCD_SIGNATURE }
        ?: return null
    tail.position(position)
    return tail.slice().order(ByteOrder.LITTLE_ENDIAN)
}

private fun readBuffer(
    raf: RandomAccessFile,
    offset: Long,
    size: Int,
): ByteBuffer {
    val bytes = ByteArray(size)
    raf.seek(offset)
    raf.readFully(bytes)
    return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
}
