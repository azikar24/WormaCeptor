/*
 * Copyright AziKar24 20/2/2023.
 */

package com.azikar24.wormaceptor.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.azikar24.wormaceptor.internal.data.HttpHeader
import java.util.*

@Entity(tableName = "NetworkTransaction")
class PersistentNetworkTransaction {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "request_date")
    var requestDate: Date? = null

    @ColumnInfo(name = "response_date")
    var responseDate: Date? = null

    @ColumnInfo(name = "took_ms")
    var tookMs: Long? = null

    @ColumnInfo(name = "protocol")
    var protocol: String? = null

    @ColumnInfo(name = "method")
    var method: String? = null

    @ColumnInfo(name = "url")
    var url: String? = null

    @ColumnInfo(name = "host")
    var host: String? = null

    @ColumnInfo(name = "path")
    var path: String? = null

    @ColumnInfo(name = "scheme")
    var scheme: String? = null

    @ColumnInfo(name = "request_content_length")
    var requestContentLength: Long? = null

    @ColumnInfo(name = "request_content_type")
    var requestContentType: String? = null

    @ColumnInfo(name = "request_headers")
    var requestHeaders: List<HttpHeader>? = null

    @ColumnInfo(name = "request_body", typeAffinity = ColumnInfo.TEXT)
    var requestBody: String? = null

    @ColumnInfo(name = "request_body_is_plain_text")
    var requestBodyIsPlainText = true

    @ColumnInfo(name = "response_code")
    var responseCode: Int? = null

    @ColumnInfo(name = "response_message")
    var responseMessage: String? = null

    @ColumnInfo(name = "error")
    var error: String? = null

    @ColumnInfo(name = "response_content_length")
    var responseContentLength: Long? = null

    @ColumnInfo(name = "response_content_type")
    var responseContentType: String? = null

    @ColumnInfo(name = "response_headers")
    var responseHeaders: List<HttpHeader>? = null

    @ColumnInfo(name = "response_body", typeAffinity = ColumnInfo.TEXT)
    var responseBody: String? = null

    @ColumnInfo(name = "response_body_is_plain_text")
    var responseBodyIsPlainText = true
}
