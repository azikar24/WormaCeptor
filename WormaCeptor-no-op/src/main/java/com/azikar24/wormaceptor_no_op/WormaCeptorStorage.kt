/*
 * Copyright AziKar24 20/2/2023.
 */

package com.azikar24.wormaceptor_no_op

interface WormaCeptorStorage {
    companion object {
        val STORAGE: WormaCeptorStorage = object : WormaCeptorStorage {}
    }
}
