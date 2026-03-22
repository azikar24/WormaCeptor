package com.azikar24.wormaceptor.feature.webviewmonitor

import app.cash.turbine.test
import com.azikar24.wormaceptor.core.engine.WebViewMonitorEngine
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewRequestStats
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType
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
    inner class `isEnabled` {

        @Test
        fun `initial value is true`() = runTest {
            viewModel.isEnabled.test {
                awaitItem() shouldBe true
            }
        }

        @Test
        fun `reflects engine state changes`() = runTest {
            viewModel.isEnabled.test {
                awaitItem() shouldBe true
                isEnabledFlow.value = false
                awaitItem() shouldBe false
            }
        }
    }

    @Nested
    inner class `requests` {

        @Test
        fun `initial value is empty list`() = runTest {
            viewModel.requests.test {
                awaitItem().shouldBeEmpty()
            }
        }

        @Test
        fun `reflects engine request updates`() = runTest {
            val request = makeSampleRequest()

            viewModel.requests.test {
                awaitItem().shouldBeEmpty()
                requestsFlow.value = listOf(request)
                awaitItem() shouldHaveSize 1
            }
        }
    }

    @Nested
    inner class `stats` {

        @Test
        fun `initial value is empty stats`() = runTest {
            viewModel.stats.test {
                awaitItem() shouldBe WebViewRequestStats.empty()
            }
        }
    }

    @Nested
    inner class `searchQuery` {

        @Test
        fun `initial value is empty string`() = runTest {
            viewModel.searchQuery.test {
                awaitItem() shouldBe ""
            }
        }

        @Test
        fun `updates when setSearchQuery called`() = runTest {
            viewModel.searchQuery.test {
                awaitItem() shouldBe ""
                viewModel.setSearchQuery("api")
                awaitItem() shouldBe "api"
            }
        }
    }

    @Nested
    inner class `selectedRequest` {

        @Test
        fun `initial value is null`() = runTest {
            viewModel.selectedRequest.test {
                awaitItem().shouldBeNull()
            }
        }

        @Test
        fun `selectRequest sets value`() = runTest {
            val request = makeSampleRequest()

            viewModel.selectedRequest.test {
                awaitItem().shouldBeNull()
                viewModel.selectRequest(request)
                awaitItem() shouldBe request
            }
        }

        @Test
        fun `clearSelection resets to null`() = runTest {
            val request = makeSampleRequest()

            viewModel.selectedRequest.test {
                awaitItem().shouldBeNull()
                viewModel.selectRequest(request)
                awaitItem() shouldBe request
                viewModel.clearSelection()
                awaitItem().shouldBeNull()
            }
        }
    }

    @Nested
    inner class `showFilters` {

        @Test
        fun `initial value is false`() = runTest {
            viewModel.showFilters.test {
                awaitItem() shouldBe false
            }
        }

        @Test
        fun `toggleFilters toggles from false to true`() = runTest {
            viewModel.showFilters.test {
                awaitItem() shouldBe false
                viewModel.toggleFilters()
                awaitItem() shouldBe true
            }
        }

        @Test
        fun `toggleFilters toggles back to false`() = runTest {
            viewModel.showFilters.test {
                awaitItem() shouldBe false
                viewModel.toggleFilters()
                awaitItem() shouldBe true
                viewModel.toggleFilters()
                awaitItem() shouldBe false
            }
        }
    }

    @Nested
    inner class `filteredRequests` {

        @Test
        fun `initial value is empty list`() = runTest {
            viewModel.filteredRequests.test {
                awaitItem().shouldBeEmpty()
            }
        }

        @Test
        fun `filters by search query on URL`() = runTest {
            val matchingRequest = makeSampleRequest(id = "1", url = "https://api.example.com/users")
            val nonMatchingRequest = makeSampleRequest(id = "2", url = "https://cdn.example.com/image.png")
            requestsFlow.value = listOf(matchingRequest, nonMatchingRequest)

            viewModel.setSearchQuery("api")

            viewModel.filteredRequests.test {
                val items = awaitItem()
                items shouldHaveSize 1
                items.first().id shouldBe "1"
            }
        }

        @Test
        fun `filters by search query on method`() = runTest {
            val postRequest = makeSampleRequest(id = "1", method = "POST")
            val getRequest = makeSampleRequest(id = "2", method = "GET")
            requestsFlow.value = listOf(postRequest, getRequest)

            viewModel.setSearchQuery("post")

            viewModel.filteredRequests.test {
                val items = awaitItem()
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

            viewModel.setSearchQuery("special")

            viewModel.filteredRequests.test {
                val items = awaitItem()
                items shouldHaveSize 1
                items.first().id shouldBe "1"
            }
        }

        @Test
        fun `empty search shows all requests`() = runTest {
            val req1 = makeSampleRequest(id = "1")
            val req2 = makeSampleRequest(id = "2")
            requestsFlow.value = listOf(req1, req2)

            viewModel.setSearchQuery("")

            viewModel.filteredRequests.test {
                awaitItem() shouldHaveSize 2
            }
        }
    }

    @Nested
    inner class `engine delegation` {

        @Test
        fun `toggleEnabled delegates to engine toggle`() {
            viewModel.toggleEnabled()
            verify { engine.toggle() }
        }

        @Test
        fun `toggleResourceTypeFilter delegates to engine`() {
            viewModel.toggleResourceTypeFilter(WebViewResourceType.SCRIPT)
            verify { engine.toggleResourceTypeFilter(WebViewResourceType.SCRIPT) }
        }

        @Test
        fun `clearFilters resets search and delegates to engine`() = runTest {
            viewModel.setSearchQuery("api")
            viewModel.clearFilters()

            viewModel.searchQuery.test {
                awaitItem() shouldBe ""
            }
            verify { engine.clearFilters() }
        }

        @Test
        fun `clearRequests delegates to engine`() {
            viewModel.clearRequests()
            verify { engine.clearRequests() }
        }
    }
}
