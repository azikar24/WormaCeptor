package com.azikar24.wormaceptor.api.contentprovider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.azikar24.wormaceptor.api.TransactionDetailDto
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileOutputStream

/**
 * Content provider for IDE communication via ADB.
 * Enables Android Studio plugin to query transactions from the device.
 */
class WormaCeptorContentProvider : ContentProvider() {

    private lateinit var uriMatcher: UriMatcher

    override fun onCreate(): Boolean {
        val authority = "${context?.packageName}.wormaceptor.provider"
        uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(authority, "transactions", CODE_TRANSACTIONS)
            addURI(authority, "transaction/*/detail", CODE_TRANSACTION_DETAIL)
            addURI(authority, "transaction/*", CODE_TRANSACTION_ID)
            addURI(authority, "status", CODE_STATUS)
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            CODE_TRANSACTIONS -> queryTransactions()
            CODE_TRANSACTION_ID -> queryTransactionById(uri.lastPathSegment ?: return null)
            CODE_STATUS -> queryStatus()
            else -> null
        }
    }

    private fun queryTransactions(): Cursor {
        val cursor = MatrixCursor(TRANSACTION_COLUMNS)
        val provider = WormaCeptorApi.provider ?: return cursor

        try {
            val method = provider.javaClass.methods.find {
                it.name in listOf("getAllTransactions", "getTransactions", "getTransactionSummaries")
            }
            val result = method?.invoke(provider)
            if (result is List<*>) {
                result.filterNotNull().forEach { addTransactionToCursor(cursor, it) }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to query transactions", e)
        }

        return cursor
    }

    private fun queryTransactionById(id: String): Cursor {
        val cursor = MatrixCursor(TRANSACTION_COLUMNS)
        val provider = WormaCeptorApi.provider ?: return cursor

        try {
            val method = provider.javaClass.methods.find {
                it.name in listOf("getTransaction", "getTransactionById")
            }
            method?.invoke(provider, id)?.let { addTransactionToCursor(cursor, it) }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to query transaction by id: $id", e)
        }

        return cursor
    }

    private fun queryStatus(): Cursor {
        val cursor = MatrixCursor(STATUS_COLUMNS)
        val provider = WormaCeptorApi.provider
        val capturing = provider != null
        var count = 0

        try {
            val method = provider?.javaClass?.methods?.find {
                it.name in listOf("getTransactionCount", "count")
            }
            count = (method?.invoke(provider) as? Number)?.toInt() ?: 0
        } catch (e: Exception) {
            Log.w(TAG, "Failed to query status", e)
        }

        cursor.addRow(arrayOf(capturing, count))
        return cursor
    }

    private fun addTransactionToCursor(cursor: MatrixCursor, item: Any) {
        try {
            // NetworkTransaction has nested request/response objects
            val request = getProperty(item, "request")
            val response = getProperty(item, "response")

            // Extract URL from request and parse host/path
            val url = request?.let { getProperty(it, "url")?.toString() }
            val uri = url?.let { Uri.parse(it) }
            val host = uri?.host ?: ""
            val path = uri?.path ?: "/"

            // Get method from request object
            val method = request?.let { getProperty(it, "method")?.toString() } ?: "GET"

            // Get response code
            val code = response?.let { (getProperty(it, "code") as? Number)?.toInt() }

            // Check for body refs to determine has body flags
            val hasRequestBody = request?.let { getProperty(it, "bodyRef") } != null
            val hasResponseBody = response?.let { getProperty(it, "bodyRef") } != null

            // Get sizes
            val requestSize = request?.let { (getProperty(it, "bodySize") as? Number)?.toLong() } ?: 0L
            val responseSize = response?.let { (getProperty(it, "bodySize") as? Number)?.toLong() } ?: 0L

            // Extract content type from response headers
            val contentType = response?.let { extractContentType(it) }

            cursor.addRow(
                arrayOf(
                    getProperty(item, "id")?.toString() ?: return,
                    method,
                    host,
                    path,
                    code,
                    (getProperty(item, "durationMs") as? Number)?.toLong(),
                    getProperty(item, "status")?.toString() ?: TransactionStatus.COMPLETED.name,
                    (getProperty(item, "timestamp") as? Number)?.toLong() ?: System.currentTimeMillis(),
                    hasRequestBody,
                    hasResponseBody,
                    requestSize,
                    responseSize,
                    contentType,
                ),
            )
        } catch (e: Exception) {
            Log.d(TAG, "Failed to add transaction to cursor", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractContentType(response: Any): String? {
        return try {
            val headers = getProperty(response, "headers") as? Map<String, List<String>> ?: return null
            headers.entries.find { it.key.equals("content-type", ignoreCase = true) }?.value?.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    private fun getProperty(obj: Any, name: String): Any? {
        return try {
            obj.javaClass.getDeclaredField(name).apply { isAccessible = true }.get(obj)
        } catch (e: NoSuchFieldException) {
            try {
                obj.javaClass.getMethod("get${name.replaceFirstChar { it.uppercase() }}").invoke(obj)
            } catch (e: Exception) {
                try {
                    obj.javaClass.getMethod(name).invoke(obj)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    override fun getType(uri: Uri): String? = when (uriMatcher.match(uri)) {
        CODE_TRANSACTIONS -> "vnd.android.cursor.dir/vnd.wormaceptor.transaction"
        CODE_TRANSACTION_ID -> "vnd.android.cursor.item/vnd.wormaceptor.transaction"
        CODE_STATUS -> "vnd.android.cursor.item/vnd.wormaceptor.status"
        else -> null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        if (uriMatcher.match(uri) != CODE_TRANSACTIONS) return 0
        val provider = WormaCeptorApi.provider ?: return 0
        return try {
            val method = provider.javaClass.methods.find {
                it.name in listOf("clearTransactions", "clear", "deleteAll")
            }
            method?.invoke(provider)
            1
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete transactions", e)
            0
        }
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int =
        0

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        if (uriMatcher.match(uri) != CODE_TRANSACTION_DETAIL) return null

        // Extract transaction ID from URI: transaction/{id}/detail
        val pathSegments = uri.pathSegments
        if (pathSegments.size < 2) return null
        val transactionId = pathSegments[1]

        val provider = WormaCeptorApi.provider ?: return null

        return try {
            // Call getTransactionDetail via reflection
            val method = provider.javaClass.methods.find { it.name == "getTransactionDetail" }
            val detail = method?.invoke(provider, transactionId) as? TransactionDetailDto ?: return null

            // Convert to JSON
            val json = transactionDetailToJson(detail)

            // Create pipe to return JSON data
            val pipe = ParcelFileDescriptor.createPipe()
            val writeEnd = pipe[1]

            Thread {
                try {
                    FileOutputStream(writeEnd.fileDescriptor).use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to write transaction detail", e)
                } finally {
                    try {
                        writeEnd.close()
                    } catch (_: Exception) {}
                }
            }.start()

            pipe[0]
        } catch (e: Exception) {
            Log.w(TAG, "Failed to open transaction detail: $transactionId", e)
            null
        }
    }

    private fun transactionDetailToJson(detail: TransactionDetailDto): String {
        val json = JSONObject().apply {
            put("id", detail.id)
            put("method", detail.method)
            put("url", detail.url)
            put("host", detail.host)
            put("path", detail.path)
            put("code", detail.code)
            put("duration", detail.duration)
            put("status", detail.status)
            put("timestamp", detail.timestamp)
            put("request_headers", headersToJson(detail.requestHeaders))
            put("request_body", detail.requestBody)
            put("request_size", detail.requestSize)
            put("response_headers", headersToJson(detail.responseHeaders))
            put("response_body", detail.responseBody)
            put("response_size", detail.responseSize)
            put("response_message", detail.responseMessage)
            put("protocol", detail.protocol)
            put("tls_version", detail.tlsVersion)
            put("error", detail.error)
            put("content_type", detail.contentType)
        }
        return json.toString()
    }

    private fun headersToJson(headers: Map<String, List<String>>): JSONObject {
        val json = JSONObject()
        headers.forEach { (key, values) ->
            json.put(key, JSONArray(values))
        }
        return json
    }

    companion object {
        private const val TAG = "WormaCeptorProvider"
        private const val CODE_TRANSACTIONS = 1
        private const val CODE_TRANSACTION_ID = 2
        private const val CODE_STATUS = 3
        private const val CODE_TRANSACTION_DETAIL = 4

        private val TRANSACTION_COLUMNS = arrayOf(
            "id", "method", "host", "path", "code", "duration",
            "status", "timestamp", "has_request_body", "has_response_body",
            "request_size", "response_size", "content_type",
        )

        private val STATUS_COLUMNS = arrayOf("capturing", "count")
    }
}
