/*
 * Copyright AziKar24 20/2/2023.
 */

package com.azikar24.wormaceptor_imdb

import com.azikar24.wormaceptor.internal.data.HttpTransaction
import java.util.*


internal class TransactionPredicateProvider {
    fun getDefaultSearchPredicate(searchKeyWord: String): DefaultSearchPredicate {
        return DefaultSearchPredicate(searchKeyWord)
    }

    fun getRequestSearchPredicate(searchKeyWord: String): RequestSearchPredicate {
        return RequestSearchPredicate(searchKeyWord)
    }

    fun getResponseSearchPredicate(searchKeyWord: String): ResponseSearchPredicate {
        return ResponseSearchPredicate(searchKeyWord)
    }

    fun getRequestResponseSearchPredicate(searchKeyWord: String): RequestResponseSearchPredicate {
        return RequestResponseSearchPredicate(searchKeyWord)
    }

    open class DefaultSearchPredicate(var searchKeyWord: String) : Predicate<HttpTransaction> {
        override fun apply(t: HttpTransaction): Boolean {
            return if (t.protocol?.lowercase(Locale.ROOT)?.startsWith(searchKeyWord.lowercase(Locale.getDefault())) == true) {
                true
            } else if (t.method?.lowercase(Locale.ROOT)?.startsWith(searchKeyWord.lowercase(Locale.getDefault())) == true) {
                true
            } else if (t.url?.lowercase(Locale.ROOT)?.contains(searchKeyWord.lowercase(Locale.getDefault())) == true) {
                true
            } else t.responseCode.toString().startsWith(searchKeyWord)
        }
    }

    class RequestSearchPredicate(searchKeyWord: String) : DefaultSearchPredicate(searchKeyWord) {
        override fun apply(t: HttpTransaction): Boolean {
            return (
                    super.apply(t)
                            || t.requestBody?.lowercase(Locale.ROOT)?.contains(searchKeyWord.lowercase(Locale.getDefault())) == true
                    )
        }
    }

    class ResponseSearchPredicate(searchKeyWord: String) : DefaultSearchPredicate(searchKeyWord) {
        override fun apply(t: HttpTransaction): Boolean {
            return (super.apply(t)
                    || t.responseBody?.lowercase(Locale.ROOT)?.contains(searchKeyWord.lowercase(Locale.getDefault())) == true
                    || t.responseMessage?.lowercase(Locale.ROOT)?.contains(searchKeyWord.lowercase(Locale.getDefault())) == true)
        }
    }

    class RequestResponseSearchPredicate(searchKeyWord: String) : DefaultSearchPredicate(searchKeyWord) {
        override fun apply(t: HttpTransaction): Boolean {

            return super.apply(t) ||
                    t.responseBody?.lowercase(Locale.ROOT)?.contains(searchKeyWord.lowercase(Locale.getDefault())) == true
                    || t.responseMessage?.lowercase(Locale.ROOT)?.contains(searchKeyWord.lowercase(Locale.getDefault())) == true
                    || t.requestBody?.lowercase(Locale.ROOT)?.contains(searchKeyWord.lowercase(Locale.getDefault())) == true

        }
    }
}
