/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptorapp

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.azikar24.wormaceptor.feature.securestorage.SecureStorageViewer
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorMainTheme
import java.security.KeyStore
import javax.crypto.KeyGenerator

/**
 * Test activity for the Secure Storage Viewer feature.
 * Sets up test data in EncryptedSharedPreferences and KeyStore.
 */
class SecureStorageTestActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set up test secure storage data
        setupTestSecureStorage()

        setContent {
            WormaCeptorMainTheme {
                Scaffold { padding ->
                    SecureStorageViewer(
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

    private fun setupTestSecureStorage() {
        // Set up EncryptedSharedPreferences
        try {
            val masterKey = MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encryptedPrefs = EncryptedSharedPreferences.create(
                this,
                "test_encrypted_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )

            encryptedPrefs.edit().apply {
                putString("user_token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test")
                putString("refresh_token", "refresh_xyz_123456")
                putString("api_key", "sk-test-1234567890abcdef")
                putString("user_email", "user@example.com")
                putBoolean("is_premium", true)
                putInt("login_count", 42)
                putLong("last_login", System.currentTimeMillis())
                apply()
            }
        } catch (e: Exception) {
            // EncryptedSharedPreferences may not be available on all devices
            e.printStackTrace()
        }

        // Set up KeyStore aliases
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            // Create test key aliases
            val keyAliases = listOf(
                "test_encryption_key",
                "test_signing_key",
                "test_auth_key",
            )

            keyAliases.forEach { alias ->
                if (!keyStore.containsAlias(alias)) {
                    val keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES,
                        "AndroidKeyStore",
                    )

                    val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                        alias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()

                    keyGenerator.init(keyGenParameterSpec)
                    keyGenerator.generateKey()
                }
            }
        } catch (e: Exception) {
            // KeyStore operations may fail on some devices
            e.printStackTrace()
        }
    }
}
