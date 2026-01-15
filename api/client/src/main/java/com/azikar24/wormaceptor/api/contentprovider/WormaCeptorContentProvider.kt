/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.api.contentprovider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptor.domain.entities.TransactionStatus

/**
 * Content provider for IDE communication via ADB.
 * Enables Android Studio plugin to query transactions from the device.
 */
class WormaCeptorContentProvider : ContentProvider() {

    companion object {
        private const val TAG = "WormaCeptorProvider"
        private const val CODE_TRANSACTIONS = 1
        private const val CODE_TRANSACTION_ID = 2
        private const val CODE_STATUS = 3

        private val TRANSACTION_COLUMNS = arrayOf(
            "id", "method", "host", "path", "code", "duration",
            "status", "timestamp", "has_request_body", "has_response_body",
            "request_size", "response_size", "content_type"
        )

        private val STATUS_COLUMNS = arrayOf("capturing", "count")
    }

    private lateinit var uriMatcher: UriMatcher

    override fun onCreate(): Boolean {
        val authority = "${context?.packageName}.wormaceptor.provider"
        uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(authority, "transactions", CODE_TRANSACTIONS)
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
        sortOrder: String?
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
            cursor.addRow(arrayOf(
                getProperty(item, "id")?.toString() ?: return,
                getProperty(item, "method")?.toString() ?: "GET",
                getProperty(item, "host")?.toString() ?: "",
                getProperty(item, "path")?.toString() ?: "/",
                (getProperty(item, "code") as? Number)?.toInt(),
                (getProperty(item, "tookMs") ?: getProperty(item, "durationMs") as? Number).toString().toLongOrNull(),
                getProperty(item, "status")?.toString() ?: TransactionStatus.COMPLETED.name,
                (getProperty(item, "timestamp") as? Number)?.toLong() ?: System.currentTimeMillis(),
                getProperty(item, "hasRequestBody") as? Boolean ?: false,
                getProperty(item, "hasResponseBody") as? Boolean ?: false,
                (getProperty(item, "requestSize") as? Number)?.toLong() ?: 0L,
                (getProperty(item, "responseSize") as? Number)?.toLong() ?: 0L,
                getProperty(item, "contentType")?.toString()
            ))
        } catch (e: Exception) {
            Log.d(TAG, "Failed to add transaction to cursor", e)
        }
    }

    private fun getProperty(obj: Any, name: String): Any? {
        return try {
            obj.javaClass.getDeclaredField(name).apply { isAccessible = true }.get(obj)
        } catch (e: NoSuchFieldException) {
            try {
                obj.javaClass.getMethod("get${name.replaceFirstChar { it.uppercase() }}").invoke(obj)
            } catch (e: Exception) {
                try { obj.javaClass.getMethod(name).invoke(obj) } catch (e: Exception) { null }
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

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
