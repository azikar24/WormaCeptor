/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.data

interface WormaCeptorStorage {
    companion object {
        val STORAGE: WormaCeptorStorage = object : WormaCeptorStorage {}
    }
}
