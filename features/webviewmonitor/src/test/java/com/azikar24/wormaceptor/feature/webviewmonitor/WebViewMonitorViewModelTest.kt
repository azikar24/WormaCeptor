package com.azikar24.wormaceptor.feature.webviewmonitor

import app.cash.turbine.test
import com.azikar24.wormaceptor.core.engine.WebViewMonitorEngine
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewRequestStats
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType
import com.azikar24.wormaceptor.feature.webviewmonitor.vm.WebViewMonitorViewEffect
import com.azikar24.wormaceptor.feature.webviewmonitor.vm.WebViewMonitorViewEvent
import com.azikar24.wormaceptor.feature.webviewmonitor.vm.WebViewMonitorViewModel
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WebViewMonitorViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val isEnabledFlow = MutableStateFlow(true)
    private val requestsFlow = MutableStateFlow<List<WebViewRequest>>(emptyList())
    private val statsFlow = MutableStateFlow(WebViewRequestStats.empty())
    private val resourceTypeFilterFlow = MutableStateFlow<Set<WebViewResourceType>>(emptySet())

    private val engine = mockk<WebViewMonitorEngine>(relaxed = true) {
        every { isEnabled } returns isEnabledFlow
        every { requests } returns requestsFlow
        every { stats } returns statsFlow
        every { resourceTypeFilter } returns resourceTypeFilterFlow
    }

    private lateinit var viewModel: WebViewMonitorViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = WebViewMonitorViewModel(engine)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeSampleRequest(
        id: String = "req-1",
        url: String = "https://example.com/api/data",
        method: String = "GET",
        resourceType: WebViewResourceType = WebViewResourceType.XHR,
    ) = WebViewRequest(
        id = id,
        url = url,
        method = method,
        headers = emptyMap(),
        timestamp = System.currentTimeMillis(),
        webViewId = "webview-1",
        resourceType = resourceType,
    )

    @Nested
    inner class `uiState initial values` {

        @Test
        fun `requests starts empty`() = runTest {
            viewModel.uiState.test {
                awaitItem().requests.shouldBeEmpty()
            }
        }

        @Test
        fun `stats starts as empty stats`() = runTest {
            viewModel.uiState.test {
                awaitItem().stats shouldBe WebViewRequestStats.empty()
            }
        }

        @Test
        fun `searchQuery starts as empty string`() = runTest {
            viewModel.uiState.test {
                awaitItem().searchQuery shouldBe ""
            }
        }

        @Test
        fun `selectedRequest starts as null`() = runTest {
            viewModel.uiState.test {
                awaitItem().selectedRequest.shouldBeNull()
            }
        }
    }

    @Nested
    inner class `engine observation` {

        @Test
        fun `reflects engine request updates`() = runTest {
            val request = makeSampleRequest()

            viewModel.uiState.test {
                awaitItem().requests.shouldBeEmpty()
                requestsFlow.value = listOf(request)
                awaitItem().requests shouldHaveSize 1
            }
        }
    }

    @Nested
    inner class `SetSearchQuery event` {

        @Test
        fun `updates searchQuery in state`() = runTest {
            viewModel.uiState.test {
                awaitItem().searchQuery shouldBe ""
                viewModel.sendEvent(WebViewMonitorViewEvent.SetSearchQuery("api"))
                awaitItem().searchQuery shouldBe "api"
            }
        }
    }

    @Nested
    inner class `SelectRequest event` {

        @Test
        fun `sets selectedRequest in state`() = runTest {
            val request = makeSampleRequest()

            viewModel.uiState.test {
                awaitItem().selectedRequest.shouldBeNull()
                viewModel.sendEvent(WebViewMonitorViewEvent.SelectRequest(request))
                awaitItem().selectedRequest shouldBe request
            }
        }

        @Test
        fun `emits NavigateToDetail effect`() = runTest {
            val request = makeSampleRequest()

            viewModel.effects.test {
                viewModel.sendEvent(WebViewMonitorViewEvent.SelectRequest(request))
                awaitItem() shouldBe WebViewMonitorViewEffect.NavigateToDetail
            }
        }
    }

    @Nested
    inner class `ClearSelection event` {

        @Test
        fun `resets selectedRequest to null`() = runTest {
            val request = makeSampleRequest()

            viewModel.uiState.test {
                awaitItem().selectedRequest.shouldBeNull()
                viewModel.sendEvent(WebViewMonitorViewEvent.SelectRequest(request))
                awaitItem().selectedRequest shouldBe request
                viewModel.sendEvent(WebViewMonitorViewEvent.ClearSelection)
                awaitItem().selectedRequest.shouldBeNull()
            }
        }
    }

    @Nested
    inner class `filteredRequests` {

        @Test
        fun `initial value is empty list`() = runTest {
            viewModel.uiState.test {
                awaitItem().filteredRequests.shouldBeEmpty()
            }
        }

        @Test
        fun `filters by search query on URL`() = runTest {
            val matchingRequest = makeSampleRequest(id = "1", url = "https://api.example.com/users")
            val nonMatchingRequest = makeSampleRequest(id = "2", url = "https://cdn.example.com/image.png")
            requestsFlow.value = listOf(matchingRequest, nonMatchingRequest)

            viewModel.sendEvent(WebViewMonitorViewEvent.SetSearchQuery("api"))

            viewModel.uiState.test {
                val items = awaitItem().filteredRequests
                items shouldHaveSize 1
                items.first().id shouldBe "1"
            }
        }

        @Test
        fun `filters by search query on method`() = runTest {
            val postRequest = makeSampleRequest(id = "1", method = "POST")
            val getRequest = makeSampleRequest(id = "2", method = "GET")
            requestsFlow.value = listOf(postRequest, getRequest)

            viewModel.sendEvent(WebViewMonitorViewEvent.SetSearchQuery("post"))

            viewModel.uiState.test {
                val items = awaitItem().filteredRequests
                items shouldHaveSize 1
                items.first().id shouldBe "1"
            }
        }

        @Test
        fun `filters by search query on host`() = runTest {
            val matchingRequest = makeSampleRequest(
                id = "1",
                url = "https://special-host.com/path",
            )
            val nonMatchingRequest = makeSampleRequest(
                id = "2",
                url = "https://other-host.com/path",
            )
            requestsFlow.value = listOf(matchingRequest, nonMatchingRequest)

            viewModel.sendEvent(WebViewMonitorViewEvent.SetSearchQuery("special"))

            viewModel.uiState.test {
                val items = awaitItem().filteredRequests
                items shouldHaveSize 1
                items.first().id shouldBe "1"
            }
        }

        @Test
        fun `empty search shows all requests`() = runTest {
            val req1 = makeSampleRequest(id = "1")
            val req2 = makeSampleRequest(id = "2")
            requestsFlow.value = listOf(req1, req2)

            viewModel.sendEvent(WebViewMonitorViewEvent.SetSearchQuery(""))

            viewModel.uiState.test {
                awaitItem().filteredRequests shouldHaveSize 2
            }
        }
    }

    @Nested
    inner class `engine delegation` {

        @Test
        fun `EnsureEnabled toggles engine when disabled`() {
            isEnabledFlow.value = false
            viewModel.sendEvent(WebViewMonitorViewEvent.EnsureEnabled)
            verify { engine.toggle() }
        }

        @Test
        fun `ToggleResourceTypeFilter delegates to engine`() {
            viewModel.sendEvent(WebViewMonitorViewEvent.ToggleResourceTypeFilter(WebViewResourceType.SCRIPT))
            verify { engine.toggleResourceTypeFilter(WebViewResourceType.SCRIPT) }
        }

        @Test
        fun `ClearFilters resets search and delegates to engine`() = runTest {
            viewModel.sendEvent(WebViewMonitorViewEvent.SetSearchQuery("api"))
            viewModel.sendEvent(WebViewMonitorViewEvent.ClearFilters)

            viewModel.uiState.test {
                awaitItem().searchQuery shouldBe ""
            }
            verify { engine.clearFilters() }
        }

        @Test
        fun `ClearRequests delegates to engine`() {
            viewModel.sendEvent(WebViewMonitorViewEvent.ClearRequests)
            verify { engine.clearRequests() }
        }
    }
}
