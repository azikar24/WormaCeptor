/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.imdb

import com.azikar24.wormaceptor.internal.data.WormaCeptorStorage

object WormaCeptorIMDB {
    fun getInstance(): WormaCeptorStorage {
        return WormaCeptorStorage.STORAGE
    }
}