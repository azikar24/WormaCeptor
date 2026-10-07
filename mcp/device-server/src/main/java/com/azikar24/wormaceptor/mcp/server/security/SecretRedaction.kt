package com.azikar24.wormaceptor.mcp.server.security

import com.azikar24.wormaceptor.mcp.protocol.PreferenceDto

internal const val REDACTED = "[REDACTED]"

private val SecretKey = Regex(
    "token|secret|password|passwd|passphrase|apikey|api_key|auth|credential|private_key|session",
    RegexOption.IGNORE_CASE,
)

/** `"key": "value"` in JSON or any text; the value may contain escaped quotes. */
private val QuotedPair = Regex(""""((?:[^"\\]|\\.)*)"(\s*:\s*)"((?:[^"\\]|\\.)*)"""")

/** SharedPreferences XML: `<string name="key">value</string>`. */
private val XmlStringElement = Regex("""(<string name=")([^"]*)(">)([^<]*)(</string>)""")

/** SharedPreferences XML: `<long name="key" value="..." />` and the other typed elements. */
private val XmlValueAttribute = Regex("""(name=")([^"]*)("\s+value=")([^"]*)(")""")

internal fun isSecretKey(key: String): Boolean = SecretKey.containsMatchIn(key)

/**
 * Best-effort masking of secret-looking values in file content: quoted JSON-style pairs and
 * SharedPreferences XML entries whose key matches [isSecretKey]. Structure and other values stay.
 */
internal fun redactSecrets(text: String): String = text
    .replace(QuotedPair) { m ->
        val (key, separator) = m.destructured
        if (isSecretKey(key)) "\"$key\"$separator\"$REDACTED\"" else m.value
    }
    .redactXml(XmlStringElement)
    .redactXml(XmlValueAttribute)

/** Both XML patterns capture (prefix)(key)(middle)(value)(suffix). */
private fun String.redactXml(pattern: Regex): String = replace(pattern) { m ->
    val (prefix, key, middle, _, suffix) = m.destructured
    if (isSecretKey(key)) "$prefix$key$middle$REDACTED$suffix" else m.value
}

internal fun PreferenceDto.redacted(): PreferenceDto =
    copy(value = if (isSecretKey(key)) REDACTED else redactSecrets(value))
