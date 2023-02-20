/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.support

import okhttp3.Headers
import okhttp3.Response
import java.net.HttpURLConnection

object HttpHeaders {
    private const val HTTP_CONTINUE = 100
    private fun contentLength(headers: Headers): Long {
        return stringToLong(headers["Content-Length"])
    }

    private fun stringToLong(s: String?): Long {
        return if (s == null) -1 else try {
            s.toLong()
        } catch (e: NumberFormatException) {
            -1
        }
    }

    fun hasBody(response: Response): Boolean {
        if (response.request.method == "HEAD") {
            return false
        }
        val responseCode = response.code
        return if ((responseCode < HTTP_CONTINUE || responseCode >= 200)
            && responseCode != HttpURLConnection.HTTP_NO_CONTENT && responseCode != HttpURLConnection.HTTP_NOT_MODIFIED
        ) {
            true
        } else (contentLength(response.headers) != -1L || "chunked".equals(response.header("Transfer-Encoding"), ignoreCase = true))

    }

}