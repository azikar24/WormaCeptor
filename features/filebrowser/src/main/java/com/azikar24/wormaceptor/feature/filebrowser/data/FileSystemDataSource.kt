/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.filebrowser.data

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.webkit.MimeTypeMap
import com.azikar24.wormaceptor.domain.entities.FileContent
import com.azikar24.wormaceptor.domain.entities.FileEntry
import com.azikar24.wormaceptor.domain.entities.FileInfo
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.xml.sax.InputSource

/**
 * Data source for file system operations using Java File API.
 */
class FileSystemDataSource(private val context: Context) {

    companion object {
        private const val MAX_TEXT_FILE_SIZE = 1_048_576L // 1MB
        private const val MAX_BINARY_PREVIEW_SIZE = 5_242_880L // 5MB

        private val TEXT_EXTENSIONS = setOf(
            "txt", "log", "html", "css", "js", "kt", "java",
            "md", "gradle", "properties", "yml", "yaml", "sql", "csv",
        )

        private const val JSON_EXTENSION = "json"
        private const val XML_EXTENSION = "xml"

        private val IMAGE_EXTENSIONS = setOf(
            "png",
            "jpg",
            "jpeg",
            "gif",
            "webp",
            "bmp",
        )

        private const val PDF_EXTENSION = "pdf"
    }

    /**
     * Gets the root directories accessible to the app.
     */
    fun getRootDirectories(): List<FileEntry> {
        val roots = mutableListOf<FileEntry>()

        // Internal files directory
        context.filesDir?.let { dir ->
            roots.add(createFileEntry(dir, "Internal Files"))
        }

        // Cache directory
        context.cacheDir?.let { dir ->
            roots.add(createFileEntry(dir, "Cache"))
        }

        // Code cache directory
        context.codeCacheDir?.let { dir ->
            roots.add(createFileEntry(dir, "Code Cache"))
        }

        // External files directory
        context.getExternalFilesDir(null)?.let { dir ->
            roots.add(createFileEntry(dir, "External Files"))
        }

        // App data directory
        context.applicationInfo?.dataDir?.let { dataDir ->
            val dir = File(dataDir)
            if (dir.exists()) {
                roots.add(createFileEntry(dir, "App Data"))
            }
        }

        // Databases directory
        context.getDatabasePath("placeholder")?.parentFile?.let { dir ->
            if (dir.exists()) {
                roots.add(createFileEntry(dir, "Databases"))
            }
        }

        // SharedPreferences directory
        File(context.applicationInfo?.dataDir ?: "", "shared_prefs").let { dir ->
            if (dir.exists()) {
                roots.add(createFileEntry(dir, "SharedPreferences"))
            }
        }

        return roots.sortedBy { it.name }
    }

    /**
     * Lists all files and directories in the specified path.
     */
    fun listFiles(path: String): List<FileEntry> {
        val directory = File(path)

        if (!directory.exists() || !directory.isDirectory) {
            return emptyList()
        }

        val files = directory.listFiles() ?: return emptyList()

        return files
            .map { createFileEntry(it) }
            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    /**
     * Reads the content of a file based on its type.
     */
    fun readFile(path: String): FileContent {
        val file = File(path)

        if (!file.exists()) {
            return FileContent.Error("File does not exist")
        }

        if (!file.canRead()) {
            return FileContent.Error("Permission denied")
        }

        if (file.isDirectory) {
            return FileContent.Error("Cannot read directory content")
        }

        val size = file.length()
        val extension = file.extension.lowercase()

        return when {
            // Too large for any preview
            size > MAX_BINARY_PREVIEW_SIZE -> FileContent.TooLarge(size, MAX_BINARY_PREVIEW_SIZE)

            // PDF files
            extension == PDF_EXTENSION -> readPdfFile(file)

            // Image files
            extension in IMAGE_EXTENSIONS -> readImageFile(file)

            // JSON files
            extension == JSON_EXTENSION && size <= MAX_TEXT_FILE_SIZE -> readJsonFile(file)
            extension == JSON_EXTENSION -> FileContent.TooLarge(size, MAX_TEXT_FILE_SIZE)

            // XML files
            extension == XML_EXTENSION && size <= MAX_TEXT_FILE_SIZE -> readXmlFile(file)
            extension == XML_EXTENSION -> FileContent.TooLarge(size, MAX_TEXT_FILE_SIZE)

            // Text files
            extension in TEXT_EXTENSIONS && size <= MAX_TEXT_FILE_SIZE -> readTextFile(file)

            // Text files too large
            extension in TEXT_EXTENSIONS -> FileContent.TooLarge(size, MAX_TEXT_FILE_SIZE)

            // Binary files
            else -> readBinaryFile(file)
        }
    }

    /**
     * Gets detailed information about a file.
     */
    fun getFileInfo(path: String): FileInfo {
        val file = File(path)

        return FileInfo(
            name = file.name,
            path = file.absolutePath,
            sizeBytes = if (file.isFile) file.length() else 0L,
            lastModified = file.lastModified(),
            mimeType = getMimeType(file.extension),
            isReadable = file.canRead(),
            isWritable = file.canWrite(),
            extension = file.extension.takeIf { it.isNotEmpty() },
            parentPath = file.parent,
        )
    }

    /**
     * Deletes a file or empty directory.
     */
    fun deleteFile(path: String): Boolean {
        val file = File(path)

        if (!file.exists()) {
            return false
        }

        // Don't allow deleting root directories
        val rootPaths = getRootDirectories().map { it.path }
        if (file.absolutePath in rootPaths) {
            return false
        }

        return try {
            file.delete()
        } catch (e: Exception) {
            false
        }
    }

    private fun createFileEntry(file: File, customName: String? = null): FileEntry {
        val permissions = buildString {
            append(if (file.canRead()) "r" else "-")
            append(if (file.canWrite()) "w" else "-")
            append(if (file.canExecute()) "x" else "-")
        }

        return FileEntry(
            name = customName ?: file.name,
            path = file.absolutePath,
            isDirectory = file.isDirectory,
            sizeBytes = if (file.isFile) file.length() else 0L,
            lastModified = file.lastModified(),
            permissions = permissions,
            isReadable = file.canRead(),
            isWritable = file.canWrite(),
        )
    }

    private fun readTextFile(file: File): FileContent {
        return try {
            val content = file.readText(Charsets.UTF_8)
            FileContent.Text(content, "UTF-8", content.lines().size)
        } catch (e: Exception) {
            FileContent.Error("Failed to read text file: ${e.message}", e)
        }
    }

    private fun readJsonFile(file: File): FileContent {
        return try {
            val rawContent = file.readText(Charsets.UTF_8)
            val (formattedContent, isValid) = formatJson(rawContent)
            FileContent.Json(
                rawContent = rawContent,
                formattedContent = formattedContent,
                isValid = isValid,
            )
        } catch (e: Exception) {
            FileContent.Error("Failed to read JSON file: ${e.message}", e)
        }
    }

    private fun formatJson(json: String): Pair<String, Boolean> {
        return try {
            val trimmed = json.trim()
            val formatted = when {
                trimmed.startsWith("{") -> JSONObject(trimmed).toString(2)
                trimmed.startsWith("[") -> JSONArray(trimmed).toString(2)
                else -> {
                    // Try to parse anyway
                    val token = JSONTokener(trimmed).nextValue()
                    when (token) {
                        is JSONObject -> token.toString(2)
                        is JSONArray -> token.toString(2)
                        else -> json
                    }
                }
            }
            Pair(formatted, true)
        } catch (e: Exception) {
            // Return original content if parsing fails
            Pair(json, false)
        }
    }

    private fun readXmlFile(file: File): FileContent {
        return try {
            val rawContent = file.readText(Charsets.UTF_8)
            val (formattedContent, isValid) = formatXml(rawContent)
            FileContent.Xml(
                rawContent = rawContent,
                formattedContent = formattedContent,
                isValid = isValid,
            )
        } catch (e: Exception) {
            FileContent.Error("Failed to read XML file: ${e.message}", e)
        }
    }

    private fun formatXml(xml: String): Pair<String, Boolean> {
        return try {
            val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val document = documentBuilder.parse(InputSource(StringReader(xml)))

            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")

            val writer = StringWriter()
            transformer.transform(DOMSource(document), StreamResult(writer))
            Pair(writer.toString(), true)
        } catch (e: Exception) {
            // Return original content if parsing fails
            Pair(xml, false)
        }
    }

    private fun readBinaryFile(file: File): FileContent {
        return try {
            val bytes = file.readBytes()

            FileContent.Binary(
                bytes = bytes,
                displaySize = bytes.size,
            )
        } catch (e: Exception) {
            FileContent.Error("Failed to read binary file: ${e.message}", e)
        }
    }

    private fun readImageFile(file: File): FileContent {
        return try {
            val bytes = file.readBytes()
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

            FileContent.Image(
                bytes = bytes,
                width = options.outWidth,
                height = options.outHeight,
                mimeType = getMimeType(file.extension) ?: "image/*",
            )
        } catch (e: Exception) {
            // If image decode fails, try as binary
            readBinaryFile(file)
        }
    }

    private fun readPdfFile(file: File): FileContent {
        return try {
            val parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(parcelFileDescriptor)
            val pageCount = pdfRenderer.pageCount
            pdfRenderer.close()
            parcelFileDescriptor.close()

            FileContent.Pdf(
                filePath = file.absolutePath,
                pageCount = pageCount,
                sizeBytes = file.length(),
            )
        } catch (e: Exception) {
            // If PDF rendering fails, fall back to binary
            readBinaryFile(file)
        }
    }

    private fun getMimeType(extension: String): String? {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
    }
}
