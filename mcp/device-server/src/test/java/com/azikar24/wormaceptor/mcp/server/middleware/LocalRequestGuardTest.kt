package com.azikar24.wormaceptor.mcp.server.middleware

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LocalRequestGuardTest {

    @Test
    fun `accepts the bridge's localhost requests`() {
        assertTrue(isTrustedLocalRequest(host = "localhost:8999", origin = null))
        assertTrue(isTrustedLocalRequest(host = "127.0.0.1:8999", origin = null))
        assertTrue(isTrustedLocalRequest(host = "[::1]:8999", origin = null))
        assertTrue(isTrustedLocalRequest(host = "LOCALHOST", origin = null))
    }

    @Test
    fun `rejects any request carrying an Origin header`() {
        assertFalse(isTrustedLocalRequest(host = "localhost:8999", origin = "https://evil.example"))
        assertFalse(isTrustedLocalRequest(host = "localhost:8999", origin = "null"))
    }

    @Test
    fun `rejects DNS-rebinding hosts and missing Host`() {
        assertFalse(isTrustedLocalRequest(host = "evil.example:8999", origin = null))
        assertFalse(isTrustedLocalRequest(host = "localhost.evil.example", origin = null))
        assertFalse(isTrustedLocalRequest(host = null, origin = null))
    }
}
