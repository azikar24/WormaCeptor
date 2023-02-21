/*
 * Copyright AziKar24 20/2/2023.
 */

package com.azikar24.wormaceptor.persistence

import android.content.Context
import com.azikar24.wormaceptor.internal.data.WormaCeptorStorage

object WormaCeptorPersistence {
    fun getInstance(context: Context?): WormaCeptorStorage {
        return WormaCeptorStorage.STORAGE
    }

}