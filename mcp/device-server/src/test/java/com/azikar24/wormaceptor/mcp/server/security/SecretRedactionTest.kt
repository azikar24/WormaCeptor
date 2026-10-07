package com.azikar24.wormaceptor.mcp.server.security

import com.azikar24.wormaceptor.mcp.protocol.PreferenceDto
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SecretRedactionTest {

    @Test
    fun `secret-looking keys match case-insensitively`() {
        listOf(
            "auth_token", "FirebaseAuthToken", "db_passphrase", "user_password", "passwd", "client_secret",
            "apiKey", "API_KEY", "credentials", "private_key", "sessionId",
        ).forEach { isSecretKey(it) shouldBe true }
        listOf("theme", "fire-count", "last_sync", "userName").forEach { isSecretKey(it) shouldBe false }
    }

    @Test
    fun `preference values are redacted by key`() {
        PreferenceDto("auth_token", "eyJhbGciOi", "String").redacted() shouldBe
            PreferenceDto("auth_token", REDACTED, "String")
        PreferenceDto("theme", "dark", "String").redacted() shouldBe PreferenceDto("theme", "dark", "String")
    }

    @Test
    fun `json stored in a preference value is redacted too`() {
        PreferenceDto("user", """{"name":"a","refresh_token":"r1"}""", "String").redacted().value shouldBe
            """{"name":"a","refresh_token":"$REDACTED"}"""
    }

    @Test
    fun `json file content keeps structure and non-secret values`() {
        val json = """
            {
              "name": "Demo",
              "apiKey": "AIzaSyA-123",
              "nested": { "password" : "hunter\"2", "count": 3 }
            }
        """.trimIndent()
        redactSecrets(json) shouldBe """
            {
              "name": "Demo",
              "apiKey": "$REDACTED",
              "nested": { "password" : "$REDACTED", "count": 3 }
            }
        """.trimIndent()
    }

    @Test
    fun `shared preferences xml is redacted by name attribute`() {
        val xml = """
            <map>
                <string name="auth_token">eyJhbGciOi</string>
                <string name="theme">dark</string>
                <long name="session_started" value="1791320342367" />
                <int name="launches" value="3" />
            </map>
        """.trimIndent()
        redactSecrets(xml) shouldBe """
            <map>
                <string name="auth_token">$REDACTED</string>
                <string name="theme">dark</string>
                <long name="session_started" value="$REDACTED" />
                <int name="launches" value="3" />
            </map>
        """.trimIndent()
    }

    @Test
    fun `plain text with quoted pairs is redacted`() {
        redactSecrets("""config: "db_passphrase": "s3cr3t", "mode": "fast"""") shouldBe
            """config: "db_passphrase": "$REDACTED", "mode": "fast""""
        redactSecrets("nothing to see") shouldBe "nothing to see"
    }
}
