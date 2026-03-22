package com.azikar24.wormaceptor.infra.persistence.sqlite

import app.cash.turbine.test
import com.azikar24.wormaceptor.domain.entities.NotificationAction
import com.azikar24.wormaceptor.domain.entities.NotificationPriority
import com.azikar24.wormaceptor.domain.entities.NotificationTemplate
import com.azikar24.wormaceptor.domain.entities.SimulatedNotification
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryPushSimulatorRepositoryTest {

    private val repository = InMemoryPushSimulatorRepository()

    private fun createTemplate(
        id: String = "test_template",
        name: String = "Test Template",
        notificationId: String = "test_notif",
        title: String = "Test Title",
        body: String = "Test Body",
        channelId: String = "test_channel",
        priority: NotificationPriority = NotificationPriority.DEFAULT,
        actions: List<NotificationAction> = emptyList(),
    ) = NotificationTemplate(
        id = id,
        name = name,
        notification = SimulatedNotification(
            id = notificationId,
            title = title,
            body = body,
            channelId = channelId,
            priority = priority,
            actions = actions,
        ),
    )

    @Nested
    inner class `getTemplates` {

        @Test
        fun `returns preset templates initially`() = runTest {
            repository.getTemplates().test {
                val templates = awaitItem()
                templates shouldHaveSize 3
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `preset templates have expected names`() = runTest {
            repository.getTemplates().test {
                val names = awaitItem().map { it.name }
                names shouldBe listOf("Simple Alert", "Message Style", "Action Buttons")
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `Simple Alert preset has correct properties`() = runTest {
            repository.getTemplates().test {
                val alert = awaitItem().first { it.id == "preset_simple_alert" }
                alert.notification.title shouldBe "Alert"
                alert.notification.body shouldBe "This is a test notification"
                alert.notification.priority shouldBe NotificationPriority.DEFAULT
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `Message Style preset has HIGH priority`() = runTest {
            repository.getTemplates().test {
                val message = awaitItem().first { it.id == "preset_message" }
                message.notification.priority shouldBe NotificationPriority.HIGH
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `Action Buttons preset has two actions`() = runTest {
            repository.getTemplates().test {
                val actions = awaitItem().first { it.id == "preset_actions" }
                actions.notification.actions shouldHaveSize 2
                actions.notification.actions[0].title shouldBe "Open"
                actions.notification.actions[1].title shouldBe "Share"
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class `saveTemplate` {

        @Test
        fun `adds a custom template`() = runTest {
            val custom = createTemplate(id = "custom_1", name = "My Custom")

            repository.saveTemplate(custom)

            repository.getTemplates().test {
                val templates = awaitItem()
                templates shouldHaveSize 4
                templates.any { it.id == "custom_1" } shouldBe true
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `replaces existing template with same ID`() = runTest {
            val original = createTemplate(id = "my_id", name = "Original")
            val updated = createTemplate(id = "my_id", name = "Updated")

            repository.saveTemplate(original)
            repository.saveTemplate(updated)

            repository.getTemplates().test {
                val templates = awaitItem()
                val matching = templates.filter { it.id == "my_id" }
                matching shouldHaveSize 1
                matching.first().name shouldBe "Updated"
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `preserves all notification fields`() = runTest {
            val template = NotificationTemplate(
                id = "full_test",
                name = "Full Test",
                notification = SimulatedNotification(
                    id = "notif_full",
                    title = "Full Title",
                    body = "Full Body",
                    channelId = "full_channel",
                    smallIconRes = 42,
                    largeIconUri = "https://example.com/icon.png",
                    priority = NotificationPriority.MAX,
                    actions = listOf(
                        NotificationAction("Accept", "action_accept"),
                        NotificationAction("Reject", "action_reject"),
                    ),
                    extras = mapOf("key1" to "value1", "key2" to "value2"),
                    timestamp = 12_345L,
                ),
            )

            repository.saveTemplate(template)

            repository.getTemplates().test {
                val saved = awaitItem().first { it.id == "full_test" }
                saved.name shouldBe "Full Test"
                saved.notification.id shouldBe "notif_full"
                saved.notification.title shouldBe "Full Title"
                saved.notification.body shouldBe "Full Body"
                saved.notification.channelId shouldBe "full_channel"
                saved.notification.smallIconRes shouldBe 42
                saved.notification.largeIconUri shouldBe "https://example.com/icon.png"
                saved.notification.priority shouldBe NotificationPriority.MAX
                saved.notification.actions shouldHaveSize 2
                saved.notification.extras shouldBe mapOf("key1" to "value1", "key2" to "value2")
                saved.notification.timestamp shouldBe 12_345L
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class `deleteTemplate` {

        @Test
        fun `removes the specified template`() = runTest {
            val custom = createTemplate(id = "delete_me")
            repository.saveTemplate(custom)

            repository.deleteTemplate("delete_me")

            repository.getTemplates().test {
                val templates = awaitItem()
                templates.none { it.id == "delete_me" } shouldBe true
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `can delete preset templates`() = runTest {
            repository.deleteTemplate("preset_simple_alert")

            repository.getTemplates().test {
                val templates = awaitItem()
                templates.none { it.id == "preset_simple_alert" } shouldBe true
                templates shouldHaveSize 2
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `does not throw for non-existent template ID`() = runTest {
            repository.deleteTemplate("non_existent")

            repository.getTemplates().test {
                awaitItem() shouldHaveSize 3
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `does not affect other templates`() = runTest {
            repository.saveTemplate(createTemplate(id = "keep"))
            repository.saveTemplate(createTemplate(id = "remove"))

            repository.deleteTemplate("remove")

            repository.getTemplates().test {
                val templates = awaitItem()
                templates.any { it.id == "keep" } shouldBe true
                templates.none { it.id == "remove" } shouldBe true
                cancelAndConsumeRemainingEvents()
            }
        }
    }
}
