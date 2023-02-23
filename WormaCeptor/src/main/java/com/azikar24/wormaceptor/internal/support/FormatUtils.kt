/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.support

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.internal.NetworkTransactionUIHelper
import com.azikar24.wormaceptor.internal.data.HttpHeader
import org.json.JSONArray
import org.json.JSONObject
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.util.*
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.stream.StreamResult

object FormatUtils {

    fun formatTextHighlight(context: Context, text: String?, searchKey: String?): CharSequence? {
        return if (TextUtil.isNullOrWhiteSpace(text) || TextUtil.isNullOrWhiteSpace(searchKey)) {
            text
        } else {
            val startIndexes: List<Int>? = text?.let { indexOf(it, searchKey) }
            val spannableString = SpannableString(text)
            applyHighlightSpan(context, spannableString, startIndexes, searchKey?.length)
            spannableString
        }
    }


    fun indexOf(charSequence: CharSequence?, criteria: String?): List<Int> {
        var mCriteria = criteria
        val text = charSequence.toString().lowercase(Locale.getDefault())
        mCriteria = mCriteria?.lowercase(Locale.getDefault())
        val startPositions: MutableList<Int> = ArrayList()
        var index = mCriteria?.let { text.indexOf(it) } ?: -1
        while (index >= 0) {
            startPositions.add(index)
            index = mCriteria?.let { text.indexOf(it, index + 1) } ?: -1
        }
        return startPositions
    }

    fun applyHighlightSpan(context: Context, spannableString: Spannable, indexes: List<Int>?, length: Int?) {
        if (indexes == null) return
        val mColorUtil = ColorUtil.getInstance(context)

        for (position in indexes) {
            spannableString.setSpan(
                HighlightSpan(
                    mColorUtil.mSearchHighlightBackgroundColor,
                    mColorUtil.mSearchHighlightTextColor,
                    mColorUtil.mSearchHighlightUnderline
                ),
                position, position + (length ?: 0), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun formatHeaders(httpHeaders: List<HttpHeader?>?, withMarkup: Boolean): CharSequence {
        val truss = Truss()
        if (httpHeaders != null) {
            for (header in httpHeaders) {
                if (withMarkup) {
                    truss.pushSpan(StyleSpan(Typeface.BOLD))
                }
                truss.append(header?.name).append(": ")
                if (withMarkup) {
                    truss.popSpan()
                }
                truss.append(header?.value).append("\n")
            }
        }
        return truss.build()
    }

    fun formatByteCount(bytes: Long, si: Boolean): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1].toString() + if (si) "" else "i"
        return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }

    fun formatJson(json: String): CharSequence? {
        return try {
            val msjon = json.trim()
            if (msjon.startsWith("[")) {
                val jsonArray = JSONArray(msjon)
                jsonArray.toString(4)
            } else {
                val jsonObject = JSONObject(msjon)
                jsonObject.toString(4)
            }
        } catch (e: Exception) {
            Logger.e("non json content", e)
            json
        }
    }

    fun formatXml(xml: String): CharSequence? {
        return try {
            val serializer = SAXTransformerFactory.newInstance().newTransformer()
            serializer.setOutputProperty(OutputKeys.INDENT, "yes")
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            val xmlSource: Source = SAXSource(InputSource(ByteArrayInputStream(xml.toByteArray())))
            val res = StreamResult(ByteArrayOutputStream())
            serializer.transform(xmlSource, res)
            String((res.outputStream as ByteArrayOutputStream).toByteArray())
        } catch (e: Exception) {
            Logger.e("non xml content", e)
            xml
        }
    }

    fun formatFormEncoded(formEncoded: String?): CharSequence? {
        return try {
            var mFormEncoded = formEncoded

            val truss = Truss()
            if (mFormEncoded != null) {
                mFormEncoded = URLDecoder.decode(mFormEncoded, "UTF-8")
                val pairs = mFormEncoded.split("&").toTypedArray()
                for (pair in pairs) {
                    if (pair.contains("=")) {
                        val idx = pair.indexOf("=")
                        truss.pushSpan(StyleSpan(Typeface.BOLD))
                        truss.append(pair.substring(0, idx)).append("= ")
                        truss.popSpan()
                        truss.append(pair.substring(idx + 1)).append("\n")
                    }
                }
            }
            truss.build()
        } catch (e: Exception) {
            Logger.e("non form url content content", e)
            formEncoded
        }
    }

    fun getShareText(context: Context, transactionUIHelper: NetworkTransactionUIHelper): CharSequence {
        val text = SpannableStringBuilder()
        text.append(context.getString(R.string.url)).append(": ").append(v(transactionUIHelper.networkTransaction.url)).append("\n")
        text.append(context.getString(R.string.method)).append(": ").append(v(transactionUIHelper.networkTransaction.method)).append("\n")
        text.append(context.getString(R.string.protocol)).append(": ").append(v(transactionUIHelper.networkTransaction.protocol)).append("\n")
        text.append(context.getString(R.string.status)).append(": ").append(v(transactionUIHelper.getStatus().toString())).append("\n")
        text.append(context.getString(R.string.response)).append(": ").append(v(transactionUIHelper.getResponseSummaryText())).append("\n")
        text.append(context.getString(R.string.ssl)).append(": ").append(v(context.getString(if (transactionUIHelper.isSsl()) R.string.yes else R.string.no))).append("\n")
        text.append("\n")
        text.append(context.getString(R.string.request_time)).append(": ").append(v(transactionUIHelper.networkTransaction.requestDate.toString())).append("\n")
        text.append(context.getString(R.string.response_time)).append(": ").append(v(transactionUIHelper.networkTransaction.responseDate.toString())).append("\n")
        text.append(context.getString(R.string.duration)).append(": ").append(v("${transactionUIHelper.networkTransaction.tookMs.toString()} ms")).append("\n")
        text.append("\n")
        text.append(context.getString(R.string.request_size)).append(": ").append(v(transactionUIHelper.getRequestSizeString())).append("\n")
        text.append(context.getString(R.string.response_size)).append(": ").append(v(transactionUIHelper.getResponseSizeString())).append("\n")
        text.append(context.getString(R.string.total_size)).append(": ").append(v(transactionUIHelper.getTotalSizeString())).append("\n")
        text.append("\n")
        text.append("---------- ").append(context.getString(R.string.request)).append(" ----------\n\n")
        var headers = formatHeaders(transactionUIHelper.networkTransaction.requestHeaders, false)
        if (!TextUtil.isNullOrWhiteSpace(headers)) {
            text.append(headers).append("\n")
        }
        text.append(if (transactionUIHelper.networkTransaction.requestBodyIsPlainText) v(transactionUIHelper.getFormattedRequestBody()) else context.getString(R.string.body_omitted))
        text.append("\n\n")
        text.append("---------- ").append(context.getString(R.string.response)).append(" ----------\n\n")
        headers = formatHeaders(transactionUIHelper.networkTransaction.responseHeaders, false)
        if (!TextUtil.isNullOrWhiteSpace(headers)) {
            text.append(headers).append("\n")
        }
        text.append(if (transactionUIHelper.networkTransaction.responseBodyIsPlainText) v(transactionUIHelper.getFormattedResponseBody()) else context.getString(R.string.body_omitted))
        return text
    }


    fun getShareCurlCommand(transactionUIHelper: NetworkTransactionUIHelper): String {
        var compressed = false
        val curlCmd = StringBuilder("curl")
        curlCmd.append(" -X ").append(transactionUIHelper.networkTransaction.method)
        val headers: List<HttpHeader>? = transactionUIHelper.networkTransaction.requestHeaders
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

        val requestBody = transactionUIHelper.networkTransaction.requestBody
        if (!requestBody.isNullOrEmpty()) {
            curlCmd.append(" --data $'").append(requestBody.replace("\n", "\\n")).append("'")
        }
        curlCmd.append(if (compressed) " --compressed " else " ").append(transactionUIHelper.networkTransaction.url)
        return curlCmd.toString()
    }

    private fun v(charSequence: CharSequence?): CharSequence {
        return charSequence ?: ""
    }

}