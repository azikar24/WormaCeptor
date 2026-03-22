package com.azikar24.wormaceptor.infra.persistence.sqlite

import app.cash.turbine.test
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryWebViewMonitorRepositoryTest {

    private val repository = InMemoryWebViewMonitorRepository()

    private fun createRequest(
        id: String = "req_1",
        url: String = "https://example.com/page",
        method: String = "GET",
        headers: Map<String, String> = mapOf("Accept" to "text/html"),
        timestamp: Long = System.currentTimeMillis(),
        webViewId: String = "webview_1",
        resourceType: WebViewResourceType = WebViewResourceType.DOCUMENT,
        statusCode: Int? = null,
        errorMessage: String? = null,
        duration: Long? = null,
    ) = WebViewRequest(
        id = id,
        url = url,
        method = method,
        headers = headers,
        timestamp = timestamp,
        webViewId = webViewId,
        resourceType = resourceType,
        statusCode = statusCode,
        errorMessage = errorMessage,
        duration = duration,
    )

    @Nested
    inner class `saveRequest` {

        @Test
        fun `adds a request that can be observed`() = runTest {
            val request = createRequest(id = "save_test")

            repository.saveRequest(request)

            repository.observeRequests().test {
                val items = awaitItem()
                items shouldHaveSize 1
                items.first().id shouldBe "save_test"
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `can save multiple requests`() = runTest {
            repository.saveRequest(createRequest(id = "r1", timestamp = 1000L))
            repository.saveRequest(createRequest(id = "r2", timestamp = 2000L))
            repository.saveRequest(createRequest(id = "r3", timestamp = 3000L))

            repository.observeRequests().test {
                awaitItem() shouldHaveSize 3
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `preserves all request fields`() = runTest {
            val request = WebViewRequest(
                id = "full_req",
                url = "https://api.example.com/data",
                method = "POST",
                headers = mapOf("Content-Type" to "application/json"),
                timestamp = 999L,
                webViewId = "wv_42",
                resourceType = WebViewResourceType.XHR,
                isForMainFrame = true,
                hasGesture = true,
                isRedirect = false,
                statusCode = 200,
                responseHeaders = mapOf("X-Request-Id" to "abc123"),
                errorMessage = null,
                mimeType = "application/json",
                encoding = "utf-8",
                contentLength = 1024L,
                duration = 150L,
            )

            repository.saveRequest(request)

            repository.observeRequests().test {
                val saved = awaitItem().first()
                saved.id shouldBe "full_req"
                saved.url shouldBe "https://api.example.com/data"
                saved.method shouldBe "POST"
                saved.headers shouldBe mapOf("Content-Type" to "application/json")
                saved.timestamp shouldBe 999L
                saved.webViewId shouldBe "wv_42"
                saved.resourceType shouldBe WebViewResourceType.XHR
                saved.isForMainFrame shouldBe true
                saved.hasGesture shouldBe true
                saved.isRedirect shouldBe false
                saved.statusCode shouldBe 200
                saved.responseHeaders shouldBe mapOf("X-Request-Id" to "abc123")
                saved.mimeType shouldBe "application/json"
                saved.encoding shouldBe "utf-8"
                saved.contentLength shouldBe 1024L
                saved.duration shouldBe 150L
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class `updateRequest` {

        @Test
        fun `overwrites an existing request with same ID`() = runTest {
            val original = createRequest(id = "upd_1", statusCode = null)
            repository.saveRequest(original)

            val updated = original.copy(statusCode = 200, duration = 100L)
            repository.updateRequest(updated)

            repository.observeRequests().test {
                val items = awaitItem()
                items shouldHaveSize 1
                items.first().statusCode shouldBe 200
                items.first().duration shouldBe 100L
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `adds request if ID does not exist`() = runTest {
            val request = createRequest(id = "new_via_update")

            repository.updateRequest(request)

            repository.observeRequests().test {
                val items = awaitItem()
                items shouldHaveSize 1
                items.first().id shouldBe "new_via_update"
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class `observeRequests` {

        @Test
        fun `emits empty list initially`() = runTest {
            repository.observeRequests().test {
                awaitItem().shouldBeEmpty()
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `returns requests sorted by timestamp descending`() = runTest {
            repository.saveRequest(createRequest(id = "oldest", timestamp = 1000L))
            repository.saveRequest(createRequest(id = "newest", timestamp = 3000L))
            repository.saveRequest(createRequest(id = "middle", timestamp = 2000L))

            repository.observeRequests().test {
                val items = awaitItem()
                items[0].id shouldBe "newest"
                items[1].id shouldBe "middle"
                items[2].id shouldBe "oldest"
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class `clearRequests` {

        @Test
        fun `removes all requests`() = runTest {
            repository.saveRequest(createRequest(id = "r1"))
            repository.saveRequest(createRequest(id = "r2"))

            repository.clearRequests()

            repository.observeRequests().test {
                awaitItem().shouldBeEmpty()
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `is safe to call on empty repository`() = runTest {
            repository.clearRequests()

            repository.observeRequests().test {
                awaitItem().shouldBeEmpty()
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class `clearRequestsForWebView` {

        @Test
        fun `removes only requests from the specified webViewId`() = runTest {
            repository.saveRequest(createRequest(id = "r1", webViewId = "wv_a"))
            repository.saveRequest(createRequest(id = "r2", webViewId = "wv_b"))
            repository.saveRequest(createRequest(id = "r3", webViewId = "wv_a"))

            repository.clearRequestsForWebView("wv_a")

            repository.observeRequests().test {
                val items = awaitItem()
                items shouldHaveSize 1
                items.first().id shouldBe "r2"
                items.first().webViewId shouldBe "wv_b"
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `does not affect requests from other webviews`() = runTest {
            repository.saveRequest(createRequest(id = "r1", webViewId = "wv_keep"))

            repository.clearRequestsForWebView("wv_other")

            repository.observeRequests().test {
                awaitItem() shouldHaveSize 1
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class `deleteOldest` {

        @Test
        fun `keeps only the newest requests up to keepCount`() = runTest {
            repository.saveRequest(createRequest(id = "old", timestamp = 1000L))
            repository.saveRequest(createRequest(id = "mid", timestamp = 2000L))
            repository.saveRequest(createRequest(id = "new", timestamp = 3000L))

            repository.deleteOldest(keepCount = 2)

            repository.observeRequests().test {
                val items = awaitItem()
                items shouldHaveSize 2
                items.map { it.id } shouldBe listOf("new", "mid")
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `does nothing when count is already within limit`() = runTest {
            repository.saveRequest(createRequest(id = "r1", timestamp = 1000L))
            repository.saveRequest(createRequest(id = "r2", timestamp = 2000L))

            repository.deleteOldest(keepCount = 5)

            repository.observeRequests().test {
                awaitItem() shouldHaveSize 2
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `keeps zero requests when keepCount is zero`() = runTest {
            repository.saveRequest(createRequest(id = "r1", timestamp = 1000L))
            repository.saveRequest(createRequest(id = "r2", timestamp = 2000L))

            repository.deleteOldest(keepCount = 0)

            repository.observeRequests().test {
                awaitItem().shouldBeEmpty()
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `keeps exactly one when keepCount is 1`() = runTest {
            repository.saveRequest(createRequest(id = "old", timestamp = 1000L))
            repository.saveRequest(createRequest(id = "new", timestamp = 3000L))
            repository.saveRequest(createRequest(id = "mid", timestamp = 2000L))

            repository.deleteOldest(keepCount = 1)

            repository.observeRequests().test {
                val items = awaitItem()
                items shouldHaveSize 1
                items.first().id shouldBe "new"
                cancelAndConsumeRemainingEvents()
            }
        }
    }
}
