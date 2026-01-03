package com.azikar24.wormaceptor.internal.ui.mainactivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.azikar24.wormaceptor.internal.support.NotificationHelper
import com.azikar24.wormaceptor.internal.ui.features.home.HomeScreen
import com.azikar24.wormaceptor.ui.theme.WormaCeptorMainTheme

class WormaCeptorMainActivity : ComponentActivity() {
    private lateinit var mNotificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mNotificationHelper = NotificationHelper(baseContext)

        setContent {
            WormaCeptorMainTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        IN_FOREGROUND = true
        mNotificationHelper.dismiss()
    }

    override fun onPause() {
        super.onPause()
        IN_FOREGROUND = false
    }

    companion object {
        var IN_FOREGROUND = false
    }
}