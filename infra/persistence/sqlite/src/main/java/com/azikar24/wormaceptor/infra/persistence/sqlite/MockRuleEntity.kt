package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.azikar24.wormaceptor.domain.entities.mock.MockBehavior
import com.azikar24.wormaceptor.domain.entities.mock.MockDelay
import com.azikar24.wormaceptor.domain.entities.mock.MockResponse
import com.azikar24.wormaceptor.domain.entities.mock.MockRule
import com.azikar24.wormaceptor.domain.entities.mock.RequestMatcher
import com.azikar24.wormaceptor.domain.entities.mock.UrlMatchType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "mock_rules")
data class MockRuleEntity(
    @PrimaryKey val id: String,
    val name: String,
    val enabled: Boolean,
    val urlPattern: String,
    val matchType: String,
    val method: String?,
    val matcherHeadersJson: String,
    val statusCode: Int,
    val statusMessage: String,
    val contentType: String,
    val body: String?,
    val responseHeadersJson: String,
    val delayJson: String,
    val behaviorJson: String,
    val priority: Int,
    val createdAt: Long,
) {
    fun toDomain(): MockRule {
        return MockRule(
            id = id,
            name = name,
            enabled = enabled,
            matcher = RequestMatcher(
                urlPattern = urlPattern,
                matchType = UrlMatchType.valueOf(matchType),
                method = method,
                headers = decodeMapOrEmpty(matcherHeadersJson),
            ),
            response = MockResponse(
                statusCode = statusCode,
                statusMessage = statusMessage,
                contentType = contentType,
                body = body,
                headers = decodeMapOrEmpty(responseHeadersJson),
            ),
            delay = deserializeDelay(delayJson),
            behavior = deserializeBehavior(behaviorJson),
            priority = priority,
            createdAt = createdAt,
        )
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDomain(rule: MockRule): MockRuleEntity {
            return MockRuleEntity(
                id = rule.id,
                name = rule.name,
                enabled = rule.enabled,
                urlPattern = rule.matcher.urlPattern,
                matchType = rule.matcher.matchType.name,
                method = rule.matcher.method,
                matcherHeadersJson = json.encodeToString(rule.matcher.headers),
                statusCode = rule.response.statusCode,
                statusMessage = rule.response.statusMessage,
                contentType = rule.response.contentType,
                body = rule.response.body,
                responseHeadersJson = json.encodeToString(rule.response.headers),
                delayJson = serializeDelay(rule.delay),
                behaviorJson = serializeBehavior(rule.behavior),
                priority = rule.priority,
                createdAt = rule.createdAt,
            )
        }

        private fun serializeDelay(delay: MockDelay): String = json.encodeToString(
            when (delay) {
                is MockDelay.None -> SerializedDelay("none")
                is MockDelay.Fixed -> SerializedDelay("fixed", ms = delay.ms)
                is MockDelay.Range -> SerializedDelay("range", minMs = delay.minMs, maxMs = delay.maxMs)
            },
        )

        private fun deserializeDelay(value: String): MockDelay = try {
            val d = json.decodeFromString<SerializedDelay>(value)
            when (d.type) {
                "fixed" -> MockDelay.Fixed(ms = d.ms ?: 0L)
                "range" -> MockDelay.Range(minMs = d.minMs ?: 0L, maxMs = d.maxMs ?: 1000L)
                else -> MockDelay.None
            }
        } catch (_: Exception) {
            MockDelay.None
        }

        private fun serializeBehavior(behavior: MockBehavior): String = json.encodeToString(
            when (behavior) {
                is MockBehavior.Always -> SerializedBehavior("always")
                is MockBehavior.NthRequest -> SerializedBehavior("nth", n = behavior.n)
                is MockBehavior.FirstN -> SerializedBehavior("firstN", count = behavior.count)
                is MockBehavior.Sequential -> SerializedBehavior("sequential")
                is MockBehavior.Passthrough -> SerializedBehavior("passthrough")
            },
        )

        private fun deserializeBehavior(value: String): MockBehavior = try {
            val b = json.decodeFromString<SerializedBehavior>(value)
            when (b.type) {
                "nth" -> MockBehavior.NthRequest(n = b.n ?: 1)
                "firstN" -> MockBehavior.FirstN(count = b.count ?: 1)
                "sequential" -> MockBehavior.Sequential(responses = emptyList())
                "passthrough" -> MockBehavior.Passthrough
                else -> MockBehavior.Always
            }
        } catch (_: Exception) {
            MockBehavior.Always
        }

        private fun decodeMapOrEmpty(value: String): Map<String, String> = try {
            json.decodeFromString<Map<String, String>>(value)
        } catch (_: Exception) {
            emptyMap()
        }
    }
}

@Serializable
private data class SerializedDelay(
    val type: String,
    val ms: Long? = null,
    val minMs: Long? = null,
    val maxMs: Long? = null,
)

@Serializable
private data class SerializedBehavior(
    val type: String,
    val n: Int? = null,
    val count: Int? = null,
)
