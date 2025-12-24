/*
 * Copyright AziKar24 2/3/2023.
 */

package com.azikar24.wormaceptor.internal.support

import android.content.Context
import android.text.SpannableStringBuilder
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import com.azikar24.wormaceptor.internal.data.HttpHeader
import com.azikar24.wormaceptor.ui.theme.mSearchHighlightBackgroundColor
import com.azikar24.wormaceptor.ui.theme.mSearchHighlightTextColor
import com.azikar24.wormaceptor.ui.theme.mSearchHighlightUnderline
import org.json.JSONArray
import org.json.JSONObject
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.util.*
import java.util.concurrent.TimeUnit
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.stream.StreamResult
import kotlin.math.ln
import kotlin.math.pow

object FormatUtils {

    fun indexOf(charSequence: CharSequence?, criteria: String?): List<Int> {
        if(criteria.isNullOrEmpty()) return listOf()
        var mCriteria = criteria
        val text = charSequence.toString().lowercase(Locale.getDefault())
        mCriteria = mCriteria.lowercase(Locale.getDefault())
        val startPositions: MutableList<Int> = ArrayList()
        var index = mCriteria.let { text.indexOf(it) }
        while (index >= 0) {
            startPositions.add(index)
            index = mCriteria.let { text.indexOf(it, index + 1) }
        }
        return startPositions
    }

    fun formatHeaders(httpHeaders: List<HttpHeader?>?, withMarkup: Boolean): AnnotatedString {
        if (httpHeaders == null) return buildAnnotatedString { }
        return buildAnnotatedString {
            for (header in httpHeaders) {
                if (header?.name != null) {
                    if (withMarkup) {
                        withStyle(
                            style = SpanStyle(
                                color = mSearchHighlightTextColor,
                                background = mSearchHighlightBackgroundColor,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append("${header.name}: ")
                        }
                    } else {
                        append("${header.name}: ")
                    }

                }
                if (header?.value != null) {
                    append("${header.value}\n")
                }

            }
        }
    }

    fun formatByteCount(bytes: Long, si: Boolean): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1].toString() + if (si) "" else "i"
        return String.format(Locale.US, "%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }

    fun formatJson(json: String?): AnnotatedString? {
        return try {
            val msjon = json?.trim()
            buildAnnotatedString {
                append(
                    if (msjon?.startsWith("[") == true) {
                        val jsonArray = JSONArray(msjon)
                        jsonArray.toString(4)
                    } else {
                        val jsonObject = msjon?.let { JSONObject(it) }
                        jsonObject?.toString(4) ?: "error"
                    }
                )
            }
        } catch (e: Exception) {
//            Logger.e("non json content", e)
            buildAnnotatedString {
                append(json ?: "")
            }
        }
    }

    fun formatXml(xml: String?): AnnotatedString? {
        if (xml == null) return buildAnnotatedString { }
        return try {
            val serializer = SAXTransformerFactory.newInstance().newTransformer()
            serializer.setOutputProperty(OutputKeys.INDENT, "yes")
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            val xmlSource: Source = SAXSource(InputSource(ByteArrayInputStream(xml.toByteArray())))
            val res = StreamResult(ByteArrayOutputStream())
            serializer.transform(xmlSource, res)
            buildAnnotatedString {
                append(String((res.outputStream as ByteArrayOutputStream).toByteArray()))
            }
        } catch (e: Exception) {
            Logger.e("non xml content", e)
            buildAnnotatedString {
                append(xml)
            }
        }
    }


    fun formatFormEncoded(formEncoded: String?): AnnotatedString? {
        if (formEncoded == null) {
            return buildAnnotatedString { }
        }
        return try {
            buildAnnotatedString {
                var mFormEncoded = formEncoded
                mFormEncoded = URLDecoder.decode(mFormEncoded, "UTF-8")
                val pairs = mFormEncoded.split("&").toTypedArray()
                for (pair in pairs) {
                    if (pair.contains("=")) {
                        val idx = pair.indexOf("=")
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(pair.substring(0, idx))
                            append("= ")
                        }
                        append(pair.substring(idx + 1))
                        append("\n")
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e("non form url content content", e)
            buildAnnotatedString {
                append("formEncoded")
            }
        }
    }


    fun getShareText(context: Context, networkTransaction: NetworkTransaction): String {
        val text = SpannableStringBuilder()
        text.append(context.getString(R.string.url)).append(": ").append(v(networkTransaction.url)).append("\n")
        text.append(context.getString(R.string.method)).append(": ").append(v(networkTransaction.method)).append("\n")
        text.append(context.getString(R.string.protocol)).append(": ").append(v(networkTransaction.protocol)).append("\n")
        text.append(context.getString(R.string.status)).append(": ").append(v(networkTransaction.getStatus().toString())).append("\n")
        text.append(context.getString(R.string.response)).append(": ").append(v(networkTransaction.getResponseSummaryText())).append("\n")
        text.append(context.getString(R.string.ssl)).append(": ").append(v(context.getString(if (networkTransaction.isSsl()) R.string.yes else R.string.no))).append("\n")
        text.append("\n")
        text.append(context.getString(R.string.request_time)).append(": ").append(v(networkTransaction.requestDate.toString())).append("\n")
        text.append(context.getString(R.string.response_time)).append(": ").append(v(networkTransaction.responseDate.toString())).append("\n")
        text.append(context.getString(R.string.duration)).append(": ").append(v("${networkTransaction.tookMs.toString()} ms")).append("\n")
        text.append("\n")
        text.append(context.getString(R.string.request_size)).append(": ").append(v(networkTransaction.getRequestSizeString())).append("\n")
        text.append(context.getString(R.string.response_size)).append(": ").append(v(networkTransaction.getResponseSizeString())).append("\n")
        text.append(context.getString(R.string.total_size)).append(": ").append(v(networkTransaction.getTotalSizeString())).append("\n")
        text.append("\n")
        text.append("---------- ").append(context.getString(R.string.request)).append(" ----------\n\n")
        var headers = formatHeaders(networkTransaction.requestHeaders, false)
        if (!TextUtil.isNullOrWhiteSpace(headers)) {
            text.append(headers).append("\n")
        }
        text.append(if (networkTransaction.requestBodyIsPlainText) v(networkTransaction.getFormattedRequestBody()) else context.getString(R.string.body_omitted))
        text.append("\n\n")
        text.append("---------- ").append(context.getString(R.string.response)).append(" ----------\n\n")
        headers = formatHeaders(networkTransaction.responseHeaders, false)
        if (!TextUtil.isNullOrWhiteSpace(headers)) {
            text.append(headers).append("\n")
        }
        text.append(if (networkTransaction.responseBodyIsPlainText) v(networkTransaction.getFormattedResponseBody()) else context.getString(R.string.body_omitted))
        return text.toString()
    }


    fun getShareCurlCommand(networkTransaction: NetworkTransaction): String {
        var compressed = false
        val curlCmd = StringBuilder("curl")
        curlCmd.append(" -X ").append(networkTransaction.method)
        val headers: List<HttpHeader>? = networkTransaction.requestHeaders
        var i = 0
        headers?.size?.apply {
            while (i < this) {
                val name: String? = headers[i].name
                val value: String? = headers[i].value
                if ("Accept-Encoding".equals(name, ignoreCase = true) && "gzip".equals(value, ignoreCase = true)) {
                    compressed = true
                }
                curlCmd.append(" -H ").append("\"").append(name).append(": ").append(value).append("\"")
                i++
            }
        }

        val requestBody = networkTransaction.requestBody
        if (!requestBody.isNullOrEmpty()) {
            curlCmd.append(" --data $'").append(requestBody.replace("\n", "\\n")).append("'")
        }
        curlCmd.append(if (compressed) " --compressed " else " ").append(networkTransaction.url)
        return curlCmd.toString()
    }

    private fun v(charSequence: CharSequence?): CharSequence {
        return charSequence ?: ""
    }

     fun getAnnotatedString(text: String?): AnnotatedString {
         return buildAnnotatedString {
             append(text ?: "")
         }
     }

     fun getHighlightedText(text: String?, searchKey: String?): AnnotatedString {
        val startNs = System.nanoTime()
        return buildAnnotatedString {
            if (!text.isNullOrEmpty() && !searchKey.isNullOrEmpty()) {
                val lowerText = text.lowercase()
                val lowerKey = searchKey.lowercase()
                var previousIndex = 0
                var index = lowerText.indexOf(lowerKey)
                while (index >= 0) {
                    // Append the text before the match
                    append(text.substring(previousIndex, index))
                    // Append the match with the highlight style
                    withStyle(
                        style = SpanStyle(
                            color = mSearchHighlightTextColor,
                            background = mSearchHighlightBackgroundColor,
                            textDecoration = if (mSearchHighlightUnderline) TextDecoration.Underline else TextDecoration.None
                        )
                    ) {
                        append(text.substring(index, index + searchKey.length))
                    }
                    previousIndex = index + searchKey.length
                    index = lowerText.indexOf(lowerKey, previousIndex)
                }
                // Append the remaining text after the last match
                append(text.substring(previousIndex))
                println(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs))
            } else {
                append(text ?: "")
            }
        }
    }


}