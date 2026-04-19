package com.azikar24.wormaceptor.infra.persistence.sqlite

import android.content.Context
import java.util.UUID

/** Manages a device-bound encryption key for the SQLCipher database. */
object KeystoreKeyManager {

    private const val PREFS_NAME = "wormaceptor_db_prefs"
    private const val PREFS_KEY_PASSPHRASE = "db_passphrase"

    /** Returns the database passphrase, generating one on first use. */
    fun getOrCreatePassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(PREFS_KEY_PASSPHRASE, null)
        if (existing != null) {
            return existing.toByteArray(Charsets.UTF_8)
        }

        val passphrase = UUID.randomUUID().toString() + UUID.randomUUID().toString()
        prefs.edit().putString(PREFS_KEY_PASSPHRASE, passphrase).apply()
        return passphrase.toByteArray(Charsets.UTF_8)
    }
}
