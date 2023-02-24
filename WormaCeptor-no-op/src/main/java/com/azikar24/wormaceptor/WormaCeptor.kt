/*
 * Copyright AziKar24 20/2/2023.
 */

package com.azikar24.wormaceptor

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import com.azikar24.wormaceptor.internal.data.WormaCeptorStorage

object WormaCeptor {
    var storage: WormaCeptorStorage? = null

    fun getLaunchIntent(context: Context?): Intent? {
        return null
    }

    fun logUnexpectedCrashes() = Unit
    @TargetApi(Build.VERSION_CODES.N_MR1)
    fun addAppShortcut(context: Context?): String? = null

    fun startActivityOnShake(context: Context?) {}
}