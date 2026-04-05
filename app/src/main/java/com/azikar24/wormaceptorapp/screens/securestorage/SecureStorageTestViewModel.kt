package com.azikar24.wormaceptorapp.screens.securestorage

import android.app.Application
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import java.security.KeyStore
import javax.crypto.KeyGenerator

class SecureStorageTestViewModel(
    private val application: Application,
) : BaseViewModel<SecureStorageTestViewState, SecureStorageTestViewEffect, SecureStorageTestViewEvent>(
    SecureStorageTestViewState(),
) {

    init {
        refreshAll()
    }

    override fun handleEvent(event: SecureStorageTestViewEvent) {
        when (event) {
            SecureStorageTestViewEvent.SetupTestData -> handleSetupTestData()
            SecureStorageTestViewEvent.ShowAddDialog -> updateState { copy(showAddDialog = true) }
            SecureStorageTestViewEvent.DismissAddDialog -> updateState { copy(showAddDialog = false) }
            is SecureStorageTestViewEvent.AddEntry -> handleAddEntry(event.key, event.value)
            is SecureStorageTestViewEvent.FabExpandedChanged -> updateState { copy(isFabExpanded = event.expanded) }
        }
    }

    private fun refreshAll() {
        refreshEncryptedPrefs()
        refreshKeyStore()
    }

    private fun refreshEncryptedPrefs() {
        try {
            val prefs = createEncryptedPrefs()
            val entries = prefs.all.map { (key, value) ->
                EncryptedPrefEntry(
                    key = key,
                    value = value?.toString() ?: "null",
                    type = resolveType(value),
                )
            }.sortedBy { it.key }
            updateState { copy(encryptedPrefs = entries) }
        } catch (e: Exception) {
            updateState { copy(encryptedPrefs = emptyList()) }
            emitEffect(SecureStorageTestViewEffect.ShowError("Failed to read encrypted prefs: ${e.message}"))
        }
    }

    private fun refreshKeyStore() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            val entries = keyStore.aliases().toList().map { alias ->
                val entry = keyStore.getEntry(alias, null)
                val algorithm = when (entry) {
                    is KeyStore.SecretKeyEntry -> entry.secretKey?.algorithm ?: UNKNOWN
                    is KeyStore.PrivateKeyEntry -> entry.privateKey?.algorithm ?: UNKNOWN
                    else -> UNKNOWN
                }
                KeyStoreEntry(
                    alias = alias,
                    algorithm = algorithm,
                    keySize = null,
                    creationDate = keyStore.getCreationDate(alias)?.toString(),
                )
            }.sortedBy { it.alias }
            updateState { copy(keyStoreEntries = entries) }
        } catch (e: Exception) {
            updateState { copy(keyStoreEntries = emptyList()) }
            emitEffect(SecureStorageTestViewEffect.ShowError("Failed to read keystore: ${e.message}"))
        }
    }

    private fun handleSetupTestData() {
        setupEncryptedPrefsTestData()
        setupKeyStoreTestData()
        refreshAll()
    }

    private fun setupEncryptedPrefsTestData() {
        try {
            val prefs = createEncryptedPrefs()
            prefs.edit().apply {
                putString("user_token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test")
                putString("refresh_token", "refresh_xyz_123456")
                putString("api_key", "sk-test-1234567890abcdef")
                putString("user_email", "user@example.com")
                putBoolean("is_premium", true)
                putInt("login_count", LOGIN_COUNT_TEST_VALUE)
                putLong("last_login", System.currentTimeMillis())
                apply()
            }
        } catch (e: Exception) {
            emitEffect(SecureStorageTestViewEffect.ShowError("Failed to create encrypted prefs: ${e.message}"))
        }
    }

    private fun setupKeyStoreTestData() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            TEST_KEY_ALIASES.forEach { alias ->
                if (!keyStore.containsAlias(alias)) {
                    val keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES,
                        ANDROID_KEYSTORE,
                    )
                    val spec = KeyGenParameterSpec.Builder(
                        alias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(AES_KEY_SIZE)
                        .build()

                    keyGenerator.init(spec)
                    keyGenerator.generateKey()
                }
            }
        } catch (e: Exception) {
            emitEffect(SecureStorageTestViewEffect.ShowError("Failed to create keystore keys: ${e.message}"))
        }
    }

    private fun handleAddEntry(
        key: String,
        value: String,
    ) {
        try {
            val prefs = createEncryptedPrefs()
            prefs.edit().putString(key, value).apply()
            refreshEncryptedPrefs()
            updateState { copy(showAddDialog = false) }
        } catch (e: Exception) {
            emitEffect(SecureStorageTestViewEffect.ShowError("Failed to add entry: ${e.message}"))
        }
    }

    private fun createEncryptedPrefs() = EncryptedSharedPreferences.create(
        application,
        ENCRYPTED_PREFS_FILE,
        MasterKey.Builder(application)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    companion object {
        private const val ENCRYPTED_PREFS_FILE = "test_encrypted_prefs"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val UNKNOWN = "Unknown"
        private const val LOGIN_COUNT_TEST_VALUE = 42
        private const val AES_KEY_SIZE = 256

        private val TEST_KEY_ALIASES = listOf(
            "test_encryption_key",
            "test_signing_key",
            "test_auth_key",
        )

        private fun resolveType(value: Any?): String = when (value) {
            is String -> "String"
            is Int -> "Int"
            is Long -> "Long"
            is Float -> "Float"
            is Boolean -> "Boolean"
            is Set<*> -> "StringSet"
            else -> UNKNOWN
        }
    }
}
