/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.support.event

interface Callback<T> {
    fun onEmit(event: T)
}