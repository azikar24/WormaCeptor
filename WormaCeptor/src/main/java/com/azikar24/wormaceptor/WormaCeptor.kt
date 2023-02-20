/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import com.azikar24.wormaceptor.internal.data.WormaCeptorStorage
import com.azikar24.wormaceptor.internal.ui.WormaCeptorMainActivity

object WormaCeptor {

    var storage: WormaCeptorStorage? = null

    fun getLaunchIntent(context: Context): Intent {
        return Intent(context, WormaCeptorMainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun addAppShortcut(context: Context): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val id = "${context.packageName}.wormaceptor_ui"
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            val shortcutInfo = ShortcutInfo.Builder(context, id)
                .setShortLabel(context.getString(R.string.app_name_2))
                .setLongLabel(context.getString(R.string.app_name_2))
                .setIntent(getLaunchIntent(context).setAction(Intent.ACTION_VIEW))
                .build()
            shortcutManager.addDynamicShortcuts(listOf(shortcutInfo).toMutableList())
            id
        } else {
            null
        }
    }
}