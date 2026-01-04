/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.imdb

import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import com.azikar24.wormaceptor.internal.data.CrashTransaction
import java.util.*


internal class TransactionPredicateProvider {
    fun getDefaultSearchPredicate(
        searchKeyWord: String?,
        method: String?,
        statusRange: String?
    ): DefaultSearchPredicate {
        return DefaultSearchPredicate(searchKeyWord, method, statusRange)
    }

    fun getRequestSearchPredicate(
        searchKeyWord: String?,
        method: String?,
        statusRange: String?
    ): RequestSearchPredicate {
        return RequestSearchPredicate(searchKeyWord, method, statusRange)
    }

    fun getResponseSearchPredicate(
        searchKeyWord: String?,
        method: String?,
        statusRange: String?
    ): ResponseSearchPredicate {
        return ResponseSearchPredicate(searchKeyWord, method, statusRange)
    }

    fun getRequestResponseSearchPredicate(
        searchKeyWord: String?,
        method: String?,
        statusRange: String?
    ): RequestResponseSearchPredicate {
        return RequestResponseSearchPredicate(searchKeyWord, method, statusRange)
    }

    open class DefaultSearchPredicate(
        var searchKeyWord: String?,
        var methodFilter: String?,
        var statusRange: String?
    ) : Predicate<NetworkTransaction> {
        override fun apply(t: NetworkTransaction): Boolean {
            // Method Filter
            if (methodFilter != null && t.method != methodFilter) return false

            // Status Range Filter
            if (statusRange != null) {
                val code = t.responseCode ?: 0
                val matches = when (statusRange) {
                    "2xx" -> code in 200..299
                    "3xx" -> code in 300..399
                    "4xx" -> code in 400..499
                    "5xx" -> code in 500..599
                    else -> true
                }
                if (!matches) return false
            }

            // Keyword Search
            if (searchKeyWord.isNullOrEmpty()) return true

            val key = searchKeyWord!!.lowercase(Locale.getDefault())
            return (t.protocol?.lowercase(Locale.ROOT)?.startsWith(key) == true) ||
                    (t.method?.lowercase(Locale.ROOT)?.startsWith(key) == true) ||
                    (t.url?.lowercase(Locale.ROOT)?.contains(key) == true) ||
                    (t.responseCode.toString().startsWith(key))
        }
    }

    class RequestSearchPredicate(searchKeyWord: String?, method: String?, statusRange: String?) :
        DefaultSearchPredicate(searchKeyWord, method, statusRange) {
        override fun apply(t: NetworkTransaction): Boolean {
            if (!super.apply(t)) {
                if (methodFilter != null && t.method != methodFilter) return false

                val key = searchKeyWord?.lowercase(Locale.getDefault()) ?: return true
                return t.requestBody?.lowercase(Locale.ROOT)?.contains(key) == true
            }
            return true
        }
    }

    class ResponseSearchPredicate(searchKeyWord: String?, method: String?, statusRange: String?) :
        DefaultSearchPredicate(searchKeyWord, method, statusRange) {
        override fun apply(t: NetworkTransaction): Boolean {
            if (!super.apply(t)) {
                val key = searchKeyWord?.lowercase(Locale.getDefault()) ?: return true
                return t.responseBody?.lowercase(Locale.ROOT)?.contains(key) == true ||
                        t.responseMessage?.lowercase(Locale.ROOT)?.contains(key) == true
            }
            return true
        }
    }

    class RequestResponseSearchPredicate(
        searchKeyWord: String?,
        method: String?,
        statusRange: String?
    ) :
        DefaultSearchPredicate(searchKeyWord, method, statusRange) {
        override fun apply(t: NetworkTransaction): Boolean {
            if (!super.apply(t)) {
                val key = searchKeyWord?.lowercase(Locale.getDefault()) ?: return true
                return t.responseBody?.lowercase(Locale.ROOT)?.contains(key) == true ||
                        t.responseMessage?.lowercase(Locale.ROOT)?.contains(key) == true ||
                        t.requestBody?.lowercase(Locale.ROOT)?.contains(key) == true
            }
            return true
        }
    }

    fun getCrashSearchPredicate(searchKeyWord: String): CrashSearchPredicate {
        return CrashSearchPredicate(searchKeyWord)
    }

    class CrashSearchPredicate(var searchKeyWord: String) : Predicate<CrashTransaction> {
        override fun apply(t: CrashTransaction): Boolean {
            val lowerSearchKeyWord = searchKeyWord.lowercase(Locale.getDefault())
            val inThrowable = t.throwable?.lowercase(Locale.ROOT)?.contains(lowerSearchKeyWord) == true
            val inClassNameAndLineNumber = t.getClassNameAndLineNumber()?.lowercase(Locale.ROOT)?.contains(lowerSearchKeyWord) == true

            return inThrowable || inClassNameAndLineNumber
        }
    }
}
