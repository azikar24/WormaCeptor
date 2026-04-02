package com.azikar24.wormaceptor.feature.dependenciesinspector.vm

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.azikar24.wormaceptor.core.engine.DependenciesInspectorEngine
import com.azikar24.wormaceptor.domain.entities.DependencyCategory
import com.azikar24.wormaceptor.domain.entities.DependencyInfo
import com.azikar24.wormaceptor.domain.entities.DependencySummary
import com.azikar24.wormaceptor.domain.entities.DetectionMethod
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
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
class DependenciesInspectorViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val dependenciesFlow = MutableStateFlow<List<DependencyInfo>>(emptyList())
    private val summaryFlow = MutableStateFlow(DependencySummary.empty())
    private val isLoadingFlow = MutableStateFlow(false)
    private val errorFlow = MutableStateFlow<String?>(null)

    private val engine = mockk<DependenciesInspectorEngine>(relaxed = true) {
        every { dependencies } returns dependenciesFlow
        every { summary } returns summaryFlow
        every { isLoading } returns isLoadingFlow
        every { error } returns errorFlow
    }

    private lateinit var viewModel: DependenciesInspectorViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DependenciesInspectorViewModel(engine)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeDependency(
        name: String = "OkHttp",
        groupId: String? = "com.squareup.okhttp3",
        artifactId: String? = "okhttp",
        version: String? = "4.12.0",
        category: DependencyCategory = DependencyCategory.NETWORKING,
        packageName: String = "okhttp3",
    ) = DependencyInfo(
        name = name,
        groupId = groupId,
        artifactId = artifactId,
        version = version,
        category = category,
        detectionMethod = DetectionMethod.VERSION_FIELD,
        packageName = packageName,
        isDetected = true,
        description = "$name library",
        website = null,
    )

    private suspend fun <T> ReceiveTurbine<T>.awaitUntil(predicate: (T) -> Boolean): T {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }

    @Nested
    inner class `initial state` {

        @Test
        fun `selectedCategory is null`() = runTest {
            viewModel.uiState.value.selectedCategory shouldBe null
        }

        @Test
        fun `searchQuery is empty`() = runTest {
            viewModel.uiState.value.searchQuery shouldBe ""
        }

        @Test
        fun `showVersionedOnly is false`() = runTest {
            viewModel.uiState.value.showVersionedOnly shouldBe false
        }

        @Test
        fun `selectedDependency is null`() = runTest {
            viewModel.uiState.value.selectedDependency shouldBe null
        }

        @Test
        fun `filteredDependencies is empty`() = runTest {
            viewModel.uiState.test {
                awaitItem().filteredDependencies.shouldBeEmpty()
            }
        }
    }

    @Nested
    inner class `SetSelectedCategory event` {

        @Test
        fun `updates selected category`() = runTest {
            viewModel.sendEvent(DependenciesInspectorViewEvent.SetSelectedCategory(DependencyCategory.NETWORKING))

            viewModel.uiState.value.selectedCategory shouldBe DependencyCategory.NETWORKING
        }

        @Test
        fun `setting null clears the category filter`() = runTest {
            viewModel.sendEvent(DependenciesInspectorViewEvent.SetSelectedCategory(DependencyCategory.NETWORKING))
            viewModel.sendEvent(DependenciesInspectorViewEvent.SetSelectedCategory(null))

            viewModel.uiState.value.selectedCategory shouldBe null
        }

        @Test
        fun `filters dependencies by category`() = runTest {
            val networkDep = makeDependency(name = "OkHttp", category = DependencyCategory.NETWORKING)
            val uiDep = makeDependency(name = "Compose", category = DependencyCategory.UI_FRAMEWORK)
            dependenciesFlow.value = listOf(networkDep, uiDep)
            viewModel.sendEvent(DependenciesInspectorViewEvent.SetSelectedCategory(DependencyCategory.NETWORKING))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredDependencies.size == 1 && it.filteredDependencies.first().name == "OkHttp"
                }
                state.filteredDependencies shouldHaveSize 1
                state.filteredDependencies.first().name shouldBe "OkHttp"
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `SetSearchQuery event` {

        @Test
        fun `updates search query`() = runTest {
            viewModel.sendEvent(DependenciesInspectorViewEvent.SetSearchQuery("okhttp"))

            viewModel.uiState.value.searchQuery shouldBe "okhttp"
        }

        @Test
        fun `filters by name case-insensitively`() = runTest {
            val okhttp = makeDependency(name = "OkHttp")
            val retrofit = makeDependency(
                name = "Retrofit",
                groupId = "com.squareup.retrofit2",
                artifactId = "retrofit",
                packageName = "retrofit2",
            )
            dependenciesFlow.value = listOf(okhttp, retrofit)
            viewModel.sendEvent(DependenciesInspectorViewEvent.SetSearchQuery("okhttp"))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredDependencies.size == 1 && it.filteredDependencies.first().name == "OkHttp"
                }
                state.filteredDependencies shouldHaveSize 1
                state.filteredDependencies.first().name shouldBe "OkHttp"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters by packageName`() = runTest {
            val okhttp = makeDependency(name = "OkHttp", packageName = "okhttp3")
            val retrofit = makeDependency(name = "Retrofit", packageName = "retrofit2")
            dependenciesFlow.value = listOf(okhttp, retrofit)
            viewModel.sendEvent(DependenciesInspectorViewEvent.SetSearchQuery("retrofit2"))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredDependencies.size == 1 && it.filteredDependencies.first().name == "Retrofit"
                }
                state.filteredDependencies shouldHaveSize 1
                state.filteredDependencies.first().name shouldBe "Retrofit"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters by groupId`() = runTest {
            val okhttp = makeDependency(name = "OkHttp", groupId = "com.squareup.okhttp3")
            val gson = makeDependency(
                name = "Gson",
                groupId = "com.google.code.gson",
                packageName = "com.google.gson",
            )
            dependenciesFlow.value = listOf(okhttp, gson)
            viewModel.sendEvent(DependenciesInspectorViewEvent.SetSearchQuery("squareup"))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredDependencies.size == 1 && it.filteredDependencies.first().name == "OkHttp"
                }
                state.filteredDependencies shouldHaveSize 1
                state.filteredDependencies.first().name shouldBe "OkHttp"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters by artifactId`() = runTest {
            val okhttp = makeDependency(name = "OkHttp", artifactId = "okhttp")
            val gson = makeDependency(
                name = "Gson",
                artifactId = "gson",
                packageName = "com.google.gson",
            )
            dependenciesFlow.value = listOf(okhttp, gson)
            viewModel.sendEvent(DependenciesInspectorViewEvent.SetSearchQuery("gson"))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredDependencies.size == 1 && it.filteredDependencies.first().name == "Gson"
                }
                state.filteredDependencies shouldHaveSize 1
                state.filteredDependencies.first().name shouldBe "Gson"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `blank query returns all dependencies`() = runTest {
            val okhttp = makeDependency(name = "OkHttp")
            val retrofit = makeDependency(
                name = "Retrofit",
                groupId = "com.squareup.retrofit2",
                artifactId = "retrofit",
                packageName = "retrofit2",
            )
            dependenciesFlow.value = listOf(okhttp, retrofit)
            viewModel.sendEvent(DependenciesInspectorViewEvent.SetSearchQuery("okhttp"))

            viewModel.uiState.test {
                val filtered = awaitUntil { it.filteredDependencies.size == 1 }
                filtered.filteredDependencies shouldHaveSize 1

                viewModel.sendEvent(DependenciesInspectorViewEvent.SetSearchQuery(""))
                val state = awaitUntil { it.filteredDependencies.size == 2 }
                state.filteredDependencies shouldHaveSize 2
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `SetShowVersionedOnly event` {

        @Test
        fun `updates showVersionedOnly flag`() = runTest {
            viewModel.sendEvent(DependenciesInspectorViewEvent.SetShowVersionedOnly(true))

            viewModel.uiState.value.showVersionedOnly shouldBe true
        }

        @Test
        fun `filters to only dependencies with a version`() = runTest {
            val withVersion = makeDependency(name = "OkHttp", version = "4.12.0")
            val withoutVersion = makeDependency(name = "Timber", version = null, packageName = "timber.log")
            dependenciesFlow.value = listOf(withVersion, withoutVersion)
            viewModel.sendEvent(DependenciesInspectorViewEvent.SetShowVersionedOnly(true))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredDependencies.size == 1 && it.filteredDependencies.first().name == "OkHttp"
                }
                state.filteredDependencies shouldHaveSize 1
                state.filteredDependencies.first().version shouldBe "4.12.0"
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `SelectDependency and DismissDetail events` {

        @Test
        fun `SelectDependency sets the selected dependency`() = runTest {
            val dep = makeDependency(name = "OkHttp")

            viewModel.sendEvent(DependenciesInspectorViewEvent.SelectDependency(dep))

            viewModel.uiState.value.selectedDependency shouldBe dep
        }

        @Test
        fun `DismissDetail clears the selected dependency`() = runTest {
            viewModel.sendEvent(DependenciesInspectorViewEvent.SelectDependency(makeDependency(name = "OkHttp")))
            viewModel.sendEvent(DependenciesInspectorViewEvent.DismissDetail)

            viewModel.uiState.value.selectedDependency shouldBe null
        }
    }

    @Nested
    inner class `engine delegation` {

        @Test
        fun `Refresh event delegates to engine`() {
            viewModel.sendEvent(DependenciesInspectorViewEvent.Refresh)

            verify { engine.refresh() }
        }

        @Test
        fun `exportAsText delegates to engine`() {
            every { engine.exportAsText() } returns "Dependencies Report"

            val result = viewModel.exportAsText()

            result shouldBe "Dependencies Report"
            verify { engine.exportAsText() }
        }

        @Test
        fun `isLoading reflects engine state`() = runTest {
            viewModel.uiState.test {
                awaitItem().isLoading shouldBe false
                isLoadingFlow.value = true
                awaitUntil { it.isLoading }.isLoading shouldBe true
            }
        }

        @Test
        fun `error reflects engine state`() = runTest {
            viewModel.uiState.test {
                awaitItem().error shouldBe null
                errorFlow.value = "Scan failed"
                awaitUntil { it.error == "Scan failed" }.error shouldBe "Scan failed"
            }
        }
    }

    @Nested
    inner class `combined filtering` {

        @Test
        fun `category and search query combine`() = runTest {
            val networkOk = makeDependency(
                name = "OkHttp",
                category = DependencyCategory.NETWORKING,
                packageName = "okhttp3",
            )
            val networkRetro = makeDependency(
                name = "Retrofit",
                groupId = "com.squareup.retrofit2",
                artifactId = "retrofit",
                category = DependencyCategory.NETWORKING,
                packageName = "retrofit2",
            )
            val uiOk = makeDependency(
                name = "OkCompose",
                groupId = "com.example.okcompose",
                artifactId = "okcompose",
                category = DependencyCategory.UI_FRAMEWORK,
                packageName = "okcompose",
            )
            dependenciesFlow.value = listOf(networkOk, networkRetro, uiOk)
            viewModel.sendEvent(DependenciesInspectorViewEvent.SetSelectedCategory(DependencyCategory.NETWORKING))
            viewModel.sendEvent(DependenciesInspectorViewEvent.SetSearchQuery("ok"))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredDependencies.size == 1 && it.filteredDependencies.first().name == "OkHttp"
                }
                state.filteredDependencies shouldHaveSize 1
                state.filteredDependencies.first().name shouldBe "OkHttp"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `category and versionedOnly combine`() = runTest {
            val networkWithVer = makeDependency(
                name = "OkHttp",
                category = DependencyCategory.NETWORKING,
                version = "4.12.0",
            )
            val networkNoVer = makeDependency(
                name = "Retrofit",
                category = DependencyCategory.NETWORKING,
                version = null,
                packageName = "retrofit2",
            )
            val uiWithVer = makeDependency(
                name = "Compose",
                category = DependencyCategory.UI_FRAMEWORK,
                version = "1.0.0",
                packageName = "compose",
            )
            dependenciesFlow.value = listOf(networkWithVer, networkNoVer, uiWithVer)
            viewModel.sendEvent(DependenciesInspectorViewEvent.SetSelectedCategory(DependencyCategory.NETWORKING))
            viewModel.sendEvent(DependenciesInspectorViewEvent.SetShowVersionedOnly(true))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredDependencies.size == 1 && it.filteredDependencies.first().name == "OkHttp"
                }
                state.filteredDependencies shouldHaveSize 1
                state.filteredDependencies.first().name shouldBe "OkHttp"
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
