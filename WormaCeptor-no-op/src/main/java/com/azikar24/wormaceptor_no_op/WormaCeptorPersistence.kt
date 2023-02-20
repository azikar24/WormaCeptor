/*
 * Copyright AziKar24 20/2/2023.
 */

package com.azikar24.wormaceptor_no_op

import android.content.Context

class WormaCeptorPersistence {
    fun getInstance(context: Context?): WormaCeptorStorage {
        return WormaCeptorStorage.STORAGE
    }

}