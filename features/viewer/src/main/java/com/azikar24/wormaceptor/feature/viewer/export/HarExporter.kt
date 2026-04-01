package com.azikar24.wormaceptor.feature.viewer.export

import android.net.Uri
import android.util.Base64
import com.azikar24.wormaceptor.domain.entities.HarContent
import com.azikar24.wormaceptor.domain.entities.HarCookie
import com.azikar24.wormaceptor.domain.entities.HarCreator
import com.azikar24.wormaceptor.domain.entities.HarEntry
import com.azikar24.wormaceptor.domain.entities.HarHeader
import com.azikar24.wormaceptor.domain.entities.HarLog
import com.azikar24.wormaceptor.domain.entities.HarPostData
import com.azikar24.wormaceptor.domain.entities.HarQueryParam
import com.azikar24.wormaceptor.domain.entities.HarRequest
import com.azikar24.wormaceptor.domain.entities.HarResponse
import com.azikar24.wormaceptor.domain.entities.HarTimings
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Converts [NetworkTransaction] lists into HAR 1.2 compliant JSON.
 *
 * Uses `org.json` (available on Android) for serialization to avoid
 * pulling in additional dependencies.
 */
object HarExporter {

    private const val DEFAULT_HTTP_VERSION = "HTTP/1.1"
    private const val CREATOR_NAME = "WormaCeptor"

    /**
     * Builds a [HarLog] from captured transactions.
     *
     * @param transactions the transactions to convert
     * @param version the WormaCeptor version string embedded in the creator field
     * @param bodyProvider optional callback that resolves a blob reference to its content
     */
    fun toHarLog(
        transactions: List<NetworkTransaction>,
        version: String,
        bodyProvider: (blobRef: String) -> String? = { null },
    ): HarLog {
        val entries = transactions.map { tx -> toHarEntry(tx, bodyProvider) }
        return HarLog(
            creator = HarCreator(name = CREATOR_NAME, version = version),
            entries = entries,
        )
    }

    /** Serializes a [HarLog] to a pretty-printed JSON string. */
    fun toJsonString(harLog: HarLog): String {
        val root = JSONObject()
        root.put("log", serializeLog(harLog))
        return root.toString(2)
    }

    // -----------------------------------------------------------------------
    // Conversion helpers
    // -----------------------------------------------------------------------

    private fun toHarEntry(
        tx: NetworkTransaction,
        bodyProvider: (String) -> String?,
    ): HarEntry {
        val requestBody = tx.request.bodyRef?.let(bodyProvider)
        val responseBody = tx.response?.bodyRef?.let(bodyProvider)

        val httpVersion = tx.response?.protocol?.toHarHttpVersion() ?: DEFAULT_HTTP_VERSION

        return HarEntry(
            startedDateTime = formatIso8601(tx.timestamp),
            time = tx.durationMs ?: 0,
            request = buildHarRequest(tx, requestBody, httpVersion),
            response = buildHarResponse(tx, responseBody, httpVersion),
            timings = buildTimings(tx.durationMs),
            tlsVersion = tx.response?.tlsVersion,
        )
    }

    private fun buildHarRequest(
        tx: NetworkTransaction,
        body: String?,
        httpVersion: String,
    ): HarRequest {
        val headers = flattenHeaders(tx.request.headers)
        val cookies = extractCookies(tx.request.headers)
        val queryParams = parseQueryString(tx.request.url)
        val contentType = tx.request.headers.contentType()

        val postData = if (body != null && contentType != null) {
            HarPostData(mimeType = contentType, text = body)
        } else {
            null
        }

        return HarRequest(
            method = tx.request.method,
            url = tx.request.url,
            httpVersion = httpVersion,
            cookies = cookies,
            headers = headers,
            queryString = queryParams,
            postData = postData,
            headersSize = calculateHeadersSize(headers),
            bodySize = tx.request.bodySize,
        )
    }

    private fun buildHarResponse(
        tx: NetworkTransaction,
        body: String?,
        httpVersion: String,
    ): HarResponse {
        val response = tx.response
        val headers = response?.let { flattenHeaders(it.headers) } ?: emptyList()
        val cookies = response?.let { extractSetCookies(it.headers) } ?: emptyList()
        val contentType = response?.headers?.contentType() ?: "application/octet-stream"

        val isBinary = isBinaryContentType(contentType)
        val content = HarContent(
            size = response?.bodySize ?: 0,
            mimeType = contentType,
            text = if (isBinary && body != null) {
                Base64.encodeToString(body.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            } else {
                body
            },
            encoding = if (isBinary && body != null) "base64" else null,
        )

        return HarResponse(
            status = response?.code ?: 0,
            statusText = response?.message ?: "",
            httpVersion = httpVersion,
            cookies = cookies,
            headers = headers,
            content = content,
            redirectURL = response?.headers?.redirectUrl() ?: "",
            headersSize = calculateHeadersSize(headers),
            bodySize = response?.bodySize ?: 0,
        )
    }

    private fun buildTimings(durationMs: Long?): HarTimings {
        // We only have total duration; individual phases are unavailable.
        return HarTimings(
            send = 0,
            wait = durationMs ?: -1,
            receive = 0,
        )
    }

    // -----------------------------------------------------------------------
    // Header / cookie / query helpers
    // -----------------------------------------------------------------------

    internal fun flattenHeaders(headers: Map<String, List<String>>): List<HarHeader> =
        headers.flatMap { (name, values) ->
            values.map { value -> HarHeader(name = name, value = value) }
        }

    internal fun parseQueryString(url: String): List<HarQueryParam> {
        return try {
            val uri = Uri.parse(url)
            uri.queryParameterNames.flatMap { name ->
                uri.getQueryParameters(name).map { value ->
                    HarQueryParam(name = name, value = value)
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    internal fun extractCookies(headers: Map<String, List<String>>): List<HarCookie> {
        val cookieValues = headers.entries
            .filter { it.key.equals("Cookie", ignoreCase = true) }
            .flatMap { it.value }
        return cookieValues.flatMap { parseCookieHeader(it) }
    }

    internal fun extractSetCookies(headers: Map<String, List<String>>): List<HarCookie> {
        val setCookieValues = headers.entries
            .filter { it.key.equals("Set-Cookie", ignoreCase = true) }
            .flatMap { it.value }
        return setCookieValues.mapNotNull { parseSetCookieHeader(it) }
    }

    private fun parseCookieHeader(header: String): List<HarCookie> = header.split(";").mapNotNull { part ->
        val trimmed = part.trim()
        val eqIndex = trimmed.indexOf('=')
        if (eqIndex > 0) {
            HarCookie(
                name = trimmed.substring(0, eqIndex).trim(),
                value = trimmed.substring(eqIndex + 1).trim(),
            )
        } else {
            null
        }
    }

    private fun parseSetCookieHeader(header: String): HarCookie? {
        val parts = header.split(";")
        val mainPart = parts.firstOrNull()?.trim() ?: return null
        val eqIndex = mainPart.indexOf('=')
        if (eqIndex <= 0) return null

        val name = mainPart.substring(0, eqIndex).trim()
        val value = mainPart.substring(eqIndex + 1).trim()

        var path: String? = null
        var domain: String? = null
        var expires: String? = null
        var httpOnly: Boolean? = null
        var secure: Boolean? = null

        parts.drop(1).forEach { attr ->
            val trimmed = attr.trim()
            val attrEq = trimmed.indexOf('=')
            if (attrEq > 0) {
                val attrName = trimmed.substring(0, attrEq).trim().lowercase()
                val attrValue = trimmed.substring(attrEq + 1).trim()
                when (attrName) {
                    "path" -> path = attrValue
                    "domain" -> domain = attrValue
                    "expires" -> expires = attrValue
                }
            } else {
                when (trimmed.lowercase()) {
                    "httponly" -> httpOnly = true
                    "secure" -> secure = true
                }
            }
        }

        return HarCookie(
            name = name,
            value = value,
            path = path,
            domain = domain,
            expires = expires,
            httpOnly = httpOnly,
            secure = secure,
        )
    }

    // -----------------------------------------------------------------------
    // Serialization to JSONObject
    // -----------------------------------------------------------------------

    private fun serializeLog(log: HarLog): JSONObject = JSONObject().apply {
        put("version", log.version)
        put(
            "creator",
            JSONObject().apply {
                put("name", log.creator.name)
                put("version", log.creator.version)
            },
        )
        put(
            "entries",
            JSONArray().apply {
                log.entries.forEach { put(serializeEntry(it)) }
            },
        )
    }

    private fun serializeEntry(entry: HarEntry): JSONObject = JSONObject().apply {
        put("startedDateTime", entry.startedDateTime)
        put("time", entry.time)
        put("request", serializeRequest(entry.request))
        put("response", serializeResponse(entry.response))
        put("timings", serializeTimings(entry.timings))
        entry.serverIPAddress?.let { put("serverIPAddress", it) }
        entry.connection?.let { put("connection", it) }
        // Custom extensions
        entry.webSocketFrames?.let { frames ->
            put(
                "_webSocketFrames",
                JSONArray().apply {
                    frames.forEach { frame ->
                        put(
                            JSONObject().apply {
                                put("type", frame.type)
                                put("direction", frame.direction)
                                put("data", frame.data)
                                put("timestamp", frame.timestamp)
                                put("size", frame.size)
                            },
                        )
                    }
                },
            )
        }
        entry.tlsVersion?.let { put("_tlsVersion", it) }
        entry.cipherSuite?.let { put("_cipherSuite", it) }
    }

    private fun serializeRequest(req: HarRequest): JSONObject = JSONObject().apply {
        put("method", req.method)
        put("url", req.url)
        put("httpVersion", req.httpVersion)
        put("cookies", serializeCookies(req.cookies))
        put("headers", serializeHeaders(req.headers))
        put("queryString", serializeQueryParams(req.queryString))
        req.postData?.let { put("postData", serializePostData(it)) }
        put("headersSize", req.headersSize)
        put("bodySize", req.bodySize)
    }

    private fun serializeResponse(res: HarResponse): JSONObject = JSONObject().apply {
        put("status", res.status)
        put("statusText", res.statusText)
        put("httpVersion", res.httpVersion)
        put("cookies", serializeCookies(res.cookies))
        put("headers", serializeHeaders(res.headers))
        put(
            "content",
            JSONObject().apply {
                put("size", res.content.size)
                put("mimeType", res.content.mimeType)
                res.content.text?.let { put("text", it) }
                res.content.encoding?.let { put("encoding", it) }
            },
        )
        put("redirectURL", res.redirectURL)
        put("headersSize", res.headersSize)
        put("bodySize", res.bodySize)
    }

    private fun serializeTimings(t: HarTimings): JSONObject = JSONObject().apply {
        put("blocked", t.blocked)
        put("dns", t.dns)
        put("connect", t.connect)
        put("ssl", t.ssl)
        put("send", t.send)
        put("wait", t.wait)
        put("receive", t.receive)
    }

    private fun serializeHeaders(headers: List<HarHeader>): JSONArray = JSONArray().apply {
        headers.forEach { h ->
            put(
                JSONObject().apply {
                    put("name", h.name)
                    put("value", h.value)
                },
            )
        }
    }

    private fun serializeQueryParams(params: List<HarQueryParam>): JSONArray = JSONArray().apply {
        params.forEach { p ->
            put(
                JSONObject().apply {
                    put("name", p.name)
                    put("value", p.value)
                },
            )
        }
    }

    private fun serializeCookies(cookies: List<HarCookie>): JSONArray = JSONArray().apply {
        cookies.forEach { c ->
            put(
                JSONObject().apply {
                    put("name", c.name)
                    put("value", c.value)
                    c.path?.let { put("path", it) }
                    c.domain?.let { put("domain", it) }
                    c.expires?.let { put("expires", it) }
                    c.httpOnly?.let { put("httpOnly", it) }
                    c.secure?.let { put("secure", it) }
                },
            )
        }
    }

    private fun serializePostData(pd: HarPostData): JSONObject = JSONObject().apply {
        put("mimeType", pd.mimeType)
        pd.text?.let { put("text", it) }
        pd.params?.let { params ->
            put(
                "params",
                JSONArray().apply {
                    params.forEach { p ->
                        put(
                            JSONObject().apply {
                                put("name", p.name)
                                p.value?.let { put("value", it) }
                                p.fileName?.let { put("fileName", it) }
                                p.contentType?.let { put("contentType", it) }
                            },
                        )
                    }
                },
            )
        }
    }

    // -----------------------------------------------------------------------
    // Utility functions
    // -----------------------------------------------------------------------

    internal fun formatIso8601(epochMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date(epochMillis))
    }

    private fun calculateHeadersSize(headers: List<HarHeader>): Long = if (headers.isEmpty()) {
        -1
    } else {
        headers.sumOf { "${it.name}: ${it.value}\r\n".toByteArray(Charsets.UTF_8).size.toLong() }
    }

    private fun isBinaryContentType(contentType: String): Boolean {
        val lower = contentType.lowercase()
        return lower.startsWith("image/") ||
            lower.startsWith("audio/") ||
            lower.startsWith("video/") ||
            lower == "application/octet-stream" ||
            lower.startsWith("application/pdf") ||
            lower.startsWith("application/zip")
    }

    private fun Map<String, List<String>>.contentType(): String? =
        entries.firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }
            ?.value?.firstOrNull()

    private fun Map<String, List<String>>.redirectUrl(): String? =
        entries.firstOrNull { it.key.equals("Location", ignoreCase = true) }
            ?.value?.firstOrNull()

    private fun String.toHarHttpVersion(): String = when {
        equals("h2", ignoreCase = true) -> "HTTP/2"
        equals("h3", ignoreCase = true) -> "HTTP/3"
        startsWith("http/", ignoreCase = true) -> uppercase()
        else -> "HTTP/1.1"
    }
}
