package com.azikar24.wormaceptor.feature.pushsimulator.vm

import app.cash.turbine.test
import com.azikar24.wormaceptor.core.engine.NotificationPermissionException
import com.azikar24.wormaceptor.core.engine.PushSimulatorEngine
import com.azikar24.wormaceptor.domain.contracts.PushSimulatorRepository
import com.azikar24.wormaceptor.domain.entities.NotificationAction
import com.azikar24.wormaceptor.domain.entities.NotificationChannelInfo
import com.azikar24.wormaceptor.domain.entities.NotificationPriority
import com.azikar24.wormaceptor.domain.entities.NotificationTemplate
import com.azikar24.wormaceptor.domain.entities.SimulatedNotification
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PushSimulatorViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val templatesFlow = MutableStateFlow<List<NotificationTemplate>>(emptyList())

    private val sampleChannels = listOf(
        NotificationChannelInfo(
            id = PushSimulatorEngine.DEFAULT_CHANNEL_ID,
            name = PushSimulatorEngine.DEFAULT_CHANNEL_NAME,
            description = PushSimulatorEngine.DEFAULT_CHANNEL_DESCRIPTION,
            importance = 3,
        ),
    )

    private val repository = mockk<PushSimulatorRepository>(relaxed = true) {
        every { getTemplates() } returns templatesFlow
    }

    private val engine = mockk<PushSimulatorEngine>(relaxed = true) {
        every { getNotificationChannels() } returns sampleChannels
        every { sendNotification(any()) } returns 1
    }

    private lateinit var viewModel: PushSimulatorViewModel

    private val sampleTemplate = NotificationTemplate(
        id = "test_template",
        name = "Test Template",
        notification = SimulatedNotification(
            id = "notif_1",
            title = "Template Title",
            body = "Template Body",
            channelId = PushSimulatorEngine.DEFAULT_CHANNEL_ID,
            priority = NotificationPriority.HIGH,
            actions = listOf(NotificationAction("Reply", "action_reply")),
        ),
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PushSimulatorViewModel(repository, engine)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class `initial state` {

        @Test
        fun `title is empty`() = runTest {
            advanceUntilIdle()
            viewModel.uiState.value.title shouldBe ""
        }

        @Test
        fun `body is empty`() = runTest {
            advanceUntilIdle()
            viewModel.uiState.value.body shouldBe ""
        }

        @Test
        fun `priority is DEFAULT`() = runTest {
            advanceUntilIdle()
            viewModel.uiState.value.priority shouldBe NotificationPriority.DEFAULT
        }

        @Test
        fun `actions list is empty`() = runTest {
            advanceUntilIdle()
            viewModel.uiState.value.actions.shouldBeEmpty()
        }

        @Test
        fun `loads channels on init`() = runTest {
            advanceUntilIdle()

            verify { engine.createDefaultChannel() }
            viewModel.uiState.value.channels shouldHaveSize 1
        }

        @Test
        fun `selects first channel when none selected`() = runTest {
            advanceUntilIdle()

            viewModel.uiState.value.selectedChannelId shouldBe PushSimulatorEngine.DEFAULT_CHANNEL_ID
        }

        @Test
        fun `observes templates from repository`() = runTest {
            advanceUntilIdle()
            viewModel.uiState.value.templates.shouldBeEmpty()

            templatesFlow.value = listOf(sampleTemplate)
            advanceUntilIdle()

            viewModel.uiState.value.templates shouldHaveSize 1
        }
    }

    @Nested
    inner class `UpdateTitle event` {

        @Test
        fun `updates title in state`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(PushSimulatorViewEvent.UpdateTitle("New Title"))

            viewModel.uiState.value.title shouldBe "New Title"
        }
    }

    @Nested
    inner class `UpdateBody event` {

        @Test
        fun `updates body in state`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(PushSimulatorViewEvent.UpdateBody("New Body"))

            viewModel.uiState.value.body shouldBe "New Body"
        }
    }

    @Nested
    inner class `UpdateChannelId event` {

        @Test
        fun `updates selected channel id`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(PushSimulatorViewEvent.UpdateChannelId("custom_channel"))

            viewModel.uiState.value.selectedChannelId shouldBe "custom_channel"
        }
    }

    @Nested
    inner class `UpdatePriority event` {

        @Test
        fun `updates priority in state`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(PushSimulatorViewEvent.UpdatePriority(NotificationPriority.HIGH))

            viewModel.uiState.value.priority shouldBe NotificationPriority.HIGH
        }
    }

    @Nested
    inner class `UpdateNewActionTitle event` {

        @Test
        fun `updates new action title`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(PushSimulatorViewEvent.UpdateNewActionTitle("Reply"))

            viewModel.uiState.value.newActionTitle shouldBe "Reply"
        }
    }

    @Nested
    inner class `AddAction event` {

        @Test
        fun `adds action to list`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(PushSimulatorViewEvent.AddAction("Reply"))

            viewModel.uiState.value.actions shouldHaveSize 1
            viewModel.uiState.value.actions.first().title shouldBe "Reply"
        }

        @Test
        fun `clears new action title after adding`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(PushSimulatorViewEvent.UpdateNewActionTitle("Reply"))
            viewModel.sendEvent(PushSimulatorViewEvent.AddAction("Reply"))

            viewModel.uiState.value.newActionTitle shouldBe ""
        }

        @Test
        fun `limits actions to 3`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(PushSimulatorViewEvent.AddAction("Action 1"))
            viewModel.sendEvent(PushSimulatorViewEvent.AddAction("Action 2"))
            viewModel.sendEvent(PushSimulatorViewEvent.AddAction("Action 3"))
            viewModel.sendEvent(PushSimulatorViewEvent.AddAction("Action 4"))

            viewModel.uiState.value.actions shouldHaveSize 3
        }
    }

    @Nested
    inner class `RemoveAction event` {

        @Test
        fun `removes action by id`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(PushSimulatorViewEvent.AddAction("Reply"))
            val actionId = viewModel.uiState.value.actions.first().actionId

            viewModel.sendEvent(PushSimulatorViewEvent.RemoveAction(actionId))

            viewModel.uiState.value.actions.shouldBeEmpty()
        }

        @Test
        fun `does not remove unmatched action id`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(PushSimulatorViewEvent.AddAction("Reply"))

            viewModel.sendEvent(PushSimulatorViewEvent.RemoveAction("nonexistent_id"))

            viewModel.uiState.value.actions shouldHaveSize 1
        }
    }

    @Nested
    inner class `SendNotification event` {

        @Test
        fun `emits error when title is blank`() = runTest {
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.sendEvent(PushSimulatorViewEvent.SendNotification)
                val effect = awaitItem()
                effect.shouldBeInstanceOf<PushSimulatorViewEffect.Error>()
                (effect as PushSimulatorViewEffect.Error).message shouldBe "Title is required"
            }
        }

        @Test
        fun `sends notification via engine when title is present`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(PushSimulatorViewEvent.UpdateTitle("Test Title"))
            viewModel.sendEvent(PushSimulatorViewEvent.UpdateBody("Test Body"))

            viewModel.effects.test {
                viewModel.sendEvent(PushSimulatorViewEvent.SendNotification)
                advanceUntilIdle()
                awaitItem() shouldBe PushSimulatorViewEffect.NotificationSent
            }

            verify {
                engine.sendNotification(
                    match { it.title == "Test Title" && it.body == "Test Body" },
                )
            }
        }

        @Test
        fun `emits PermissionRequired when engine throws NotificationPermissionException`() = runTest {
            every { engine.sendNotification(any()) } throws NotificationPermissionException("No permission")
            advanceUntilIdle()
            viewModel.sendEvent(PushSimulatorViewEvent.UpdateTitle("Test"))

            viewModel.effects.test {
                viewModel.sendEvent(PushSimulatorViewEvent.SendNotification)
                advanceUntilIdle()
                awaitItem() shouldBe PushSimulatorViewEffect.PermissionRequired
            }
        }

        @Test
        fun `emits Error for generic exceptions`() = runTest {
            every { engine.sendNotification(any()) } throws RuntimeException("Network error")
            advanceUntilIdle()
            viewModel.sendEvent(PushSimulatorViewEvent.UpdateTitle("Test"))

            viewModel.effects.test {
                viewModel.sendEvent(PushSimulatorViewEvent.SendNotification)
                advanceUntilIdle()
                val effect = awaitItem()
                effect.shouldBeInstanceOf<PushSimulatorViewEffect.Error>()
            }
        }

        @Test
        fun `uses default channel id when selectedChannelId is blank`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(PushSimulatorViewEvent.UpdateChannelId(""))
            viewModel.sendEvent(PushSimulatorViewEvent.UpdateTitle("Test"))

            viewModel.effects.test {
                viewModel.sendEvent(PushSimulatorViewEvent.SendNotification)
                advanceUntilIdle()
                awaitItem() shouldBe PushSimulatorViewEffect.NotificationSent
            }

            verify {
                engine.sendNotification(
                    match { it.channelId == PushSimulatorEngine.DEFAULT_CHANNEL_ID },
                )
            }
        }
    }

    @Nested
    inner class `SaveAsTemplate event` {

        @Test
        fun `emits error when template name is blank`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(PushSimulatorViewEvent.UpdateTitle("Title"))

            viewModel.effects.test {
                viewModel.sendEvent(PushSimulatorViewEvent.SaveAsTemplate(""))
                awaitItem().shouldBeInstanceOf<PushSimulatorViewEffect.Error>()
            }
        }

        @Test
        fun `emits error when title is blank`() = runTest {
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.sendEvent(PushSimulatorViewEvent.SaveAsTemplate("My Template"))
                awaitItem().shouldBeInstanceOf<PushSimulatorViewEffect.Error>()
            }
        }

        @Test
        fun `saves template via repository`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(PushSimulatorViewEvent.UpdateTitle("Title"))
            viewModel.sendEvent(PushSimulatorViewEvent.UpdateBody("Body"))

            viewModel.effects.test {
                viewModel.sendEvent(PushSimulatorViewEvent.SaveAsTemplate("My Template"))
                advanceUntilIdle()
                awaitItem() shouldBe PushSimulatorViewEffect.TemplateSaved
            }

            coVerify {
                repository.saveTemplate(
                    match { it.name == "My Template" && it.notification.title == "Title" },
                )
            }
        }
    }

    @Nested
    inner class `LoadTemplate event` {

        @Test
        fun `populates form fields from template`() = runTest {
            advanceUntilIdle()

            viewModel.sendEvent(PushSimulatorViewEvent.LoadTemplate(sampleTemplate))

            val state = viewModel.uiState.value
            state.title shouldBe "Template Title"
            state.body shouldBe "Template Body"
            state.priority shouldBe NotificationPriority.HIGH
            state.actions shouldHaveSize 1
            state.actions.first().title shouldBe "Reply"
        }

        @Test
        fun `emits TemplateLoaded effect with name`() = runTest {
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.sendEvent(PushSimulatorViewEvent.LoadTemplate(sampleTemplate))
                val effect = awaitItem()
                effect shouldBe PushSimulatorViewEffect.TemplateLoaded("Test Template")
            }
        }
    }

    @Nested
    inner class `DeleteTemplate event` {

        @Test
        fun `deletes template via repository`() = runTest {
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.sendEvent(PushSimulatorViewEvent.DeleteTemplate("test_template"))
                advanceUntilIdle()
                awaitItem() shouldBe PushSimulatorViewEffect.TemplateDeleted
            }

            coVerify { repository.deleteTemplate("test_template") }
        }
    }

    @Nested
    inner class `SendFromTemplate event` {

        @Test
        fun `sends notification from template`() = runTest {
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.sendEvent(PushSimulatorViewEvent.SendFromTemplate(sampleTemplate))
                advanceUntilIdle()
                awaitItem() shouldBe PushSimulatorViewEffect.NotificationSent
            }

            verify { engine.sendNotification(match { it.title == "Template Title" }) }
        }

        @Test
        fun `emits PermissionRequired on permission error`() = runTest {
            every { engine.sendNotification(any()) } throws NotificationPermissionException("No permission")
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.sendEvent(PushSimulatorViewEvent.SendFromTemplate(sampleTemplate))
                advanceUntilIdle()
                awaitItem() shouldBe PushSimulatorViewEffect.PermissionRequired
            }
        }
    }

    @Nested
    inner class `ClearForm event` {

        @Test
        fun `resets title and body but preserves channels and templates`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(PushSimulatorViewEvent.UpdateTitle("Test Title"))
            viewModel.sendEvent(PushSimulatorViewEvent.UpdateBody("Test Body"))
            viewModel.sendEvent(PushSimulatorViewEvent.AddAction("Reply"))

            val channelsBefore = viewModel.uiState.value.channels
            val channelIdBefore = viewModel.uiState.value.selectedChannelId

            viewModel.sendEvent(PushSimulatorViewEvent.ClearForm)

            val state = viewModel.uiState.value
            state.title shouldBe ""
            state.body shouldBe ""
            state.actions.shouldBeEmpty()
            state.priority shouldBe NotificationPriority.DEFAULT
            state.channels shouldBe channelsBefore
            state.selectedChannelId shouldBe channelIdBefore
        }
    }
}
