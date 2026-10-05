package com.azikar24.wormaceptor.core.engine

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.RandomAccessFile
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ApkNativeLibrariesTest {

    private val apk = "/data/app/~~abc==/com.example-xyz==/base.apk"

    @Test
    fun `apkMappingOffsets groups apk mapping offsets by path`() {
        val lines = listOf(
            "75b92d6a4000-75b92d898000 r-xp 017ac000 fe:3d 123 $apk",
            "75b92d898000-75b92d8a4000 r--p 0199c000 fe:3d 123 $apk",
            "7f8e900000-7f8e901000 r-xp 00000000 fd:01 456 /system/lib64/libc.so",
            "7f8e901000-7f8e902000 rw-p 00000000 00:00 0",
            "7f8e902000-7f8e903000 r--s 00000000 fd:01 789 /product/overlay/Overlay.apk",
        )

        apkMappingOffsets(lines) shouldBe mapOf(
            apk to listOf(0x17ac000L, 0x199c000L),
            "/product/overlay/Overlay.apk" to listOf(0L),
        )
    }

    @Test
    fun `mappedEntries keeps only entries whose data start is mapped`() {
        val ranges = mapOf(
            "lib/arm64-v8a/libsqlcipher.so" to 0x17ac000L..<0x19a8df0L,
            "lib/arm64-v8a/libgraphics.so" to 0x17a8000L..<0x17aa770L,
        )

        mappedEntries(ranges, listOf(0x17ac000L, 0x199c000L, 0x19a4000L)) shouldBe
            listOf("lib/arm64-v8a/libsqlcipher.so")
    }

    @Test
    fun `mappedEntries ignores an offset inside an entry that is not its data start`() {
        val ranges = mapOf(
            "lib/arm64-v8a/libunused.so" to 0x17a8770L..<0x17ab000L,
            "lib/arm64-v8a/libloaded.so" to 0x17ac000L..<0x17b0000L,
        )

        // A mapping of libunused's last page, rounded down from a neighbour, lands inside it but not at its start.
        mappedEntries(ranges, listOf(0x17aa000L, 0x17ac000L)) shouldBe listOf("lib/arm64-v8a/libloaded.so")
    }

    @Test
    fun `mappedEntries returns nothing when only non-library regions are mapped`() {
        val ranges = mapOf("lib/arm64-v8a/libfoo.so" to 4096L..<8192L)

        mappedEntries(ranges, listOf(0L, 8192L)).shouldBeEmpty()
    }

    @Test
    fun `readZipEntryDataRanges points at each entry's raw data`(@TempDir dir: File) {
        val zip = File(dir, "base.apk")
        val foo = ByteArray(5000) { (it % 251).toByte() }
        val bar = "bar-library".toByteArray()
        ZipOutputStream(zip.outputStream()).use { out ->
            out.putNextEntry(ZipEntry("classes.dex"))
            out.write(ByteArray(300) { 7 })
            out.closeEntry()
            out.putStored("lib/arm64-v8a/libfoo.so", foo, padded = true)
            out.putStored("lib/arm64-v8a/libbar.so", bar, padded = false)
        }

        val ranges = readZipEntryDataRanges(zip) { it.endsWith(".so") }

        ranges.keys shouldBe setOf("lib/arm64-v8a/libfoo.so", "lib/arm64-v8a/libbar.so")
        zip.readRange(ranges.getValue("lib/arm64-v8a/libfoo.so")) shouldBe foo.toList()
        zip.readRange(ranges.getValue("lib/arm64-v8a/libbar.so")) shouldBe bar.toList()
    }

    @Test
    fun `readZipEntryDataRanges returns nothing for a non-zip file`(@TempDir dir: File) {
        val file = File(dir, "not.apk").apply { writeBytes(ByteArray(100)) }

        readZipEntryDataRanges(file) { true }.shouldBeEmpty()
    }

    @Test
    fun `readZipEntryDataRanges returns nothing when a name length overruns the central directory`(
        @TempDir dir: File,
    ) {
        val zip = libZip(dir)
        RandomAccessFile(zip, "rw").use { raf ->
            raf.writeUInt16(raf.centralDirectoryOffset() + CD_NAME_LENGTH, 0xFFFF)
        }

        readZipEntryDataRanges(zip) { true }.shouldBeEmpty()
    }

    @Test
    fun `readZipEntryDataRanges returns nothing when a local header offset is past the end`(@TempDir dir: File) {
        val zip = libZip(dir)
        RandomAccessFile(zip, "rw").use { raf ->
            raf.writeUInt32(raf.centralDirectoryOffset() + CD_LOCAL_HEADER_OFFSET, raf.length())
        }

        readZipEntryDataRanges(zip) { true }.shouldBeEmpty()
    }

    private fun libZip(dir: File): File = File(dir, "base.apk").also { zip ->
        ZipOutputStream(zip.outputStream()).use { it.putStored("lib/arm64-v8a/libfoo.so", ByteArray(64), false) }
    }

    /** Central directory offset from an EOCD without a comment, i.e. in the last 22 bytes. */
    private fun RandomAccessFile.centralDirectoryOffset(): Long {
        seek(length() - EOCD_SIZE + EOCD_CD_OFFSET)
        return (0..3).sumOf { read().toLong() shl it * 8 }
    }

    private fun RandomAccessFile.writeUInt16(
        position: Long,
        value: Int,
    ) {
        seek(position)
        write(byteArrayOf(value.toByte(), (value shr 8).toByte()))
    }

    private fun RandomAccessFile.writeUInt32(
        position: Long,
        value: Long,
    ) {
        seek(position)
        write(ByteArray(4) { (value shr it * 8).toByte() })
    }

    private fun ZipOutputStream.putStored(
        name: String,
        data: ByteArray,
        padded: Boolean,
    ) {
        val entry = ZipEntry(name).apply {
            method = ZipEntry.STORED
            size = data.size.toLong()
            compressedSize = data.size.toLong()
            crc = CRC32().apply { update(data) }.value
            // Mimics zipalign padding in the local header's extra field (id 0xCAFE, 9 bytes).
            if (padded) extra = byteArrayOf(0xFE.toByte(), 0xCA.toByte(), 9, 0) + ByteArray(9)
        }
        putNextEntry(entry)
        write(data)
        closeEntry()
    }

    private fun File.readRange(range: LongRange): List<Byte> = RandomAccessFile(this, "r").use { raf ->
        val bytes = ByteArray((range.last - range.first + 1).toInt())
        raf.seek(range.first)
        raf.readFully(bytes)
        bytes.toList()
    }

    private companion object {
        const val EOCD_SIZE = 22
        const val EOCD_CD_OFFSET = 16
        const val CD_NAME_LENGTH = 28
        const val CD_LOCAL_HEADER_OFFSET = 42
    }
}
