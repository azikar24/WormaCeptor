/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptorapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.azikar24.wormaceptor.feature.location.LocationSimulator
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorMainTheme

/**
 * Test activity for the Location Simulator feature.
 */
class LocationTestActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WormaCeptorMainTheme {
                Scaffold { padding ->
                    LocationSimulator(
                        context = this,
                        onNavigateBack = { finish() },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                    )
                }
            }
        }
    }
}
