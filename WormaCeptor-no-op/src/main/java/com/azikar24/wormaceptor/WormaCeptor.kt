/*
 * Copyright AziKar24 20/2/2023.
 */

package com.azikar24.wormaceptor

import android.content.Context
import android.content.Intent
import com.azikar24.wormaceptor.internal.data.WormaCeptorStorage

object WormaCeptor {
    var storage: WormaCeptorStorage? = null

    fun getLaunchIntent(context: Context?): Intent? = null

    fun init(
        context: Context,
        storage: WormaCeptorStorage,
        appShortcut: Boolean = false,
        logCrashes: Boolean = false,
    ) = Unit

    private fun logUnexpectedCrashes() = Unit

    private fun addAppShortcut(context: Context?): String? = null

    fun startActivityOnShake(context: Context?) = Unit
}
