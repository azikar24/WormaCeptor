/*
 * Copyright AziKar24 20/2/2023.
 */

package com.azikar24.wormaceptor_no_op

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build

class WormaCeptor {
    var storage: WormaCeptorStorage? = null

    fun getLaunchIntent(context: Context?): Intent? {
        return null
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    fun addAppShortcut(context: Context?): String? {
        return null
    }
}