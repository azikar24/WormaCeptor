package com.azikar24.wormaceptorapp.sampleservice

import android.content.Context

object SamplePreferencesService {

    private const val USER_PREFS = "demo_user_prefs"
    private const val FEATURE_FLAGS = "demo_feature_flags"
    private const val SESSION_PREFS = "demo_session"
    private const val EXPIRY_MS = 24L * 60L * 60L * 1000L
    private const val AVATAR_COLOR = 0xFF6750A4.toInt()
    private const val TEXT_SCALE = 1.15f
    private const val ROLLOUT_BUCKET = 42
    private const val SESSION_COUNT = 7

    fun seed(context: Context) {
        val ctx = context.applicationContext

        ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).edit().apply {
            putString("display_name", "Aziz K.")
            putString("email", "azikar24@gmail.com")
            putInt("avatar_color", AVATAR_COLOR)
            putBoolean("dark_mode", true)
            putFloat("text_scale", TEXT_SCALE)
            putLong("last_seen", System.currentTimeMillis())
            putStringSet("subscribed_topics", setOf("releases", "tips", "deep-dives"))
            apply()
        }

        ctx.getSharedPreferences(FEATURE_FLAGS, Context.MODE_PRIVATE).edit().apply {
            putBoolean("har_export", true)
            putBoolean("recomposition_overlay", false)
            putBoolean("experimental_crypto_ui", true)
            putInt("rollout_bucket", ROLLOUT_BUCKET)
            apply()
        }

        ctx.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE).edit().apply {
            putString("auth_token", "demo-jwt-eyJhbGciOiJIUzI1NiJ9...")
            putLong("token_expiry", System.currentTimeMillis() + EXPIRY_MS)
            putInt("session_count", SESSION_COUNT)
            apply()
        }
    }
}
