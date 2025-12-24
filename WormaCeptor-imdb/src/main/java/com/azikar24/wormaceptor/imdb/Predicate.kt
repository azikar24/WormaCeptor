/*
 * Copyright AziKar24 21/2/2023.
 */

@file:Suppress("EmptyMethod", "EmptyMethod")

package com.azikar24.wormaceptor.imdb

import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import com.azikar24.wormaceptor.internal.data.CrashTransaction

@Suppress("EmptyMethod")
interface Predicate<T> {
    fun apply(t: T): Boolean

    companion object {
        val ALLOW_ALL = object : Predicate<NetworkTransaction> {
            override fun apply(t: NetworkTransaction) = true
        }
        val ALLOW_ALL_CRASHES = object : Predicate<CrashTransaction> {
            override fun apply(t: CrashTransaction) = true
        }
    }
}