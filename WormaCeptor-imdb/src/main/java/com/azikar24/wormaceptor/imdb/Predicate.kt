/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.imdb

import com.azikar24.wormaceptor.internal.data.HttpTransaction

interface Predicate<T> {
    fun apply(t: T): Boolean

    companion object {
        val ALLOW_ALL = object : Predicate<HttpTransaction> {
            override fun apply(t: HttpTransaction): Boolean {
                return true
            }
        }
    }
}