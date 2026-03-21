package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.entities.NotificationAction
import com.azikar24.wormaceptor.domain.entities.NotificationPriority
import com.azikar24.wormaceptor.domain.entities.NotificationTemplate
import com.azikar24.wormaceptor.domain.entities.SimulatedNotification
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class PushTemplateEntityTest {

    private fun fullTemplate() = NotificationTemplate(
        id = "template_1",
        name = "Test Template",
        notification = SimulatedNotification(
            id = "notif_1",
            title = "Test Title",
            body = "Test Body",
            channelId = "test_channel",
            smallIconRes = 42,
            largeIconUri = "https://example.com/icon.png",
            priority = NotificationPriority.HIGH,
            actions = listOf(
                NotificationAction("Accept", "action_accept"),
                NotificationAction("Dismiss", "action_dismiss"),
            ),
            extras = mapOf("key1" to "value1", "key2" to "value2"),
            timestamp = 1_700_000_000_000L,
        ),
    )

    private fun fullEntity() = PushTemplateEntity(
        id = "template_1",
        name = "Test Template",
        notificationId = "notif_1",
        title = "Test Title",
        body = "Test Body",
        channelId = "test_channel",
        smallIconRes = 42,
        largeIconUri = "https://example.com/icon.png",
        priority = "HIGH",
        actionsJson = """[{"title":"Accept","actionId":"action_accept"},""" +
            """{"title":"Dismiss","actionId":"action_dismiss"}]""",
        extrasJson = """{"key1":"value1","key2":"value2"}""",
        timestamp = 1_700_000_000_000L,
    )

    @Nested
    inner class `toDomain` {

        @Test
        fun `maps all fields correctly`() {
            val entity = fullEntity()

            val domain = entity.toDomain()

            domain.id shouldBe "template_1"
            domain.name shouldBe "Test Template"
            domain.notification.id shouldBe "notif_1"
            domain.notification.title shouldBe "Test Title"
            domain.notification.body shouldBe "Test Body"
            domain.notification.channelId shouldBe "test_channel"
            domain.notification.smallIconRes shouldBe 42
            domain.notification.largeIconUri shouldBe "https://example.com/icon.png"
            domain.notification.priority shouldBe NotificationPriority.HIGH
            domain.notification.timestamp shouldBe 1_700_000_000_000L
        }

        @Test
        fun `deserializes actions from JSON`() {
            val domain = fullEntity().toDomain()

            domain.notification.actions shouldHaveSize 2
            domain.notification.actions[0].title shouldBe "Accept"
            domain.notification.actions[0].actionId shouldBe "action_accept"
            domain.notification.actions[1].title shouldBe "Dismiss"
            domain.notification.actions[1].actionId shouldBe "action_dismiss"
        }

        @Test
        fun `deserializes extras from JSON`() {
            val domain = fullEntity().toDomain()

            domain.notification.extras shouldBe mapOf("key1" to "value1", "key2" to "value2")
        }

        @Test
        fun `handles empty actions JSON`() {
            val entity = fullEntity().copy(actionsJson = "[]")

            val domain = entity.toDomain()

            domain.notification.actions.shouldBeEmpty()
        }

        @Test
        fun `handles empty extras JSON`() {
            val entity = fullEntity().copy(extrasJson = "{}")

            val domain = entity.toDomain()

            domain.notification.extras shouldBe emptyMap()
        }

        @Test
        fun `handles malformed actions JSON gracefully`() {
            val entity = fullEntity().copy(actionsJson = "invalid json")

            val domain = entity.toDomain()

            domain.notification.actions.shouldBeEmpty()
        }

        @Test
        fun `handles malformed extras JSON gracefully`() {
            val entity = fullEntity().copy(extrasJson = "invalid json")

            val domain = entity.toDomain()

            domain.notification.extras shouldBe emptyMap()
        }

        @Test
        fun `handles null largeIconUri`() {
            val entity = fullEntity().copy(largeIconUri = null)

            val domain = entity.toDomain()

            domain.notification.largeIconUri.shouldBeNull()
        }

        @ParameterizedTest
        @EnumSource(NotificationPriority::class)
        fun `maps all priority levels`(priority: NotificationPriority) {
            val entity = fullEntity().copy(priority = priority.name)

            val domain = entity.toDomain()

            domain.notification.priority shouldBe priority
        }
    }

    @Nested
    inner class `fromDomain` {

        @Test
        fun `maps all fields correctly`() {
            val domain = fullTemplate()

            val entity = PushTemplateEntity.fromDomain(domain)

            entity.id shouldBe "template_1"
            entity.name shouldBe "Test Template"
            entity.notificationId shouldBe "notif_1"
            entity.title shouldBe "Test Title"
            entity.body shouldBe "Test Body"
            entity.channelId shouldBe "test_channel"
            entity.smallIconRes shouldBe 42
            entity.largeIconUri shouldBe "https://example.com/icon.png"
            entity.priority shouldBe "HIGH"
            entity.timestamp shouldBe 1_700_000_000_000L
        }

        @Test
        fun `serializes actions to JSON`() {
            val entity = PushTemplateEntity.fromDomain(fullTemplate())

            // Verify by round-tripping
            val roundTripped = entity.toDomain()
            roundTripped.notification.actions shouldHaveSize 2
        }

        @Test
        fun `serializes extras to JSON`() {
            val entity = PushTemplateEntity.fromDomain(fullTemplate())

            // Verify by round-tripping
            val roundTripped = entity.toDomain()
            roundTripped.notification.extras shouldBe mapOf("key1" to "value1", "key2" to "value2")
        }

        @Test
        fun `handles empty actions`() {
            val domain = fullTemplate().copy(
                notification = fullTemplate().notification.copy(actions = emptyList()),
            )

            val entity = PushTemplateEntity.fromDomain(domain)

            entity.toDomain().notification.actions.shouldBeEmpty()
        }

        @Test
        fun `handles empty extras`() {
            val domain = fullTemplate().copy(
                notification = fullTemplate().notification.copy(extras = emptyMap()),
            )

            val entity = PushTemplateEntity.fromDomain(domain)

            entity.toDomain().notification.extras shouldBe emptyMap()
        }

        @Test
        fun `handles null largeIconUri`() {
            val domain = fullTemplate().copy(
                notification = fullTemplate().notification.copy(largeIconUri = null),
            )

            val entity = PushTemplateEntity.fromDomain(domain)

            entity.largeIconUri.shouldBeNull()
        }
    }

    @Nested
    inner class `round-trip` {

        @Test
        fun `fromDomain then toDomain preserves all fields`() {
            val original = fullTemplate()

            val entity = PushTemplateEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped.id shouldBe original.id
            roundTripped.name shouldBe original.name
            roundTripped.notification.id shouldBe original.notification.id
            roundTripped.notification.title shouldBe original.notification.title
            roundTripped.notification.body shouldBe original.notification.body
            roundTripped.notification.channelId shouldBe original.notification.channelId
            roundTripped.notification.smallIconRes shouldBe original.notification.smallIconRes
            roundTripped.notification.largeIconUri shouldBe original.notification.largeIconUri
            roundTripped.notification.priority shouldBe original.notification.priority
            roundTripped.notification.actions shouldBe original.notification.actions
            roundTripped.notification.extras shouldBe original.notification.extras
            roundTripped.notification.timestamp shouldBe original.notification.timestamp
        }

        @Test
        fun `round-trips template with no actions and no extras`() {
            val original = NotificationTemplate(
                id = "minimal",
                name = "Minimal",
                notification = SimulatedNotification(
                    id = "min_notif",
                    title = "Title",
                    body = "Body",
                    channelId = "channel",
                    actions = emptyList(),
                    extras = emptyMap(),
                    timestamp = 0L,
                ),
            )

            val entity = PushTemplateEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped.notification.actions.shouldBeEmpty()
            roundTripped.notification.extras shouldBe emptyMap()
        }

        @Test
        fun `round-trips all priority levels`() {
            NotificationPriority.entries.forEach { priority ->
                val original = fullTemplate().copy(
                    notification = fullTemplate().notification.copy(priority = priority),
                )

                val entity = PushTemplateEntity.fromDomain(original)
                val roundTripped = entity.toDomain()

                roundTripped.notification.priority shouldBe priority
            }
        }
    }
}
