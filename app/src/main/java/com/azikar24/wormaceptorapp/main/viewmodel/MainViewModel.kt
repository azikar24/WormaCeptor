package com.azikar24.wormaceptorapp.main.viewmodel

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptorapp.main.uimodel.MainViewEffect
import com.azikar24.wormaceptorapp.main.uimodel.MainViewEvent
import com.azikar24.wormaceptorapp.main.uimodel.MainViewState
import com.azikar24.wormaceptorapp.sampleservice.Data
import com.azikar24.wormaceptorapp.sampleservice.LoginData
import com.azikar24.wormaceptorapp.sampleservice.SampleApiService
import com.azikar24.wormaceptorapp.sampleservice.SampleContentService
import com.azikar24.wormaceptorapp.sampleservice.SampleWebSocketService
import com.azikar24.wormaceptorapp.sampleservice.VeryLargeData
import com.azikar24.wormaceptorapp.wormaceptorui.components.ToolStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

class MainViewModel : BaseViewModel<MainViewState, MainViewEffect, MainViewEvent>(MainViewState()) {

    override fun handleEvent(event: MainViewEvent) {
        when (event) {
            MainViewEvent.LaunchWormaCeptorClicked -> emitEffect(MainViewEffect.OpenWormaCeptor)
            MainViewEvent.GitHubClicked -> emitEffect(MainViewEffect.OpenGitHub)
            MainViewEvent.TestToolsClicked -> updateState { copy(showTestToolsSheet = true) }

            MainViewEvent.RunApiTestsClicked -> handleRunApiTests()
            MainViewEvent.WebSocketTestClicked -> handleWebSocketTest()

            MainViewEvent.TriggerCrashClicked -> {
                updateState { copy(showTestToolsSheet = false, showCrashDialog = true) }
            }
            MainViewEvent.TriggerLeakClicked -> {
                emitEffect(MainViewEffect.TriggerMemoryLeak)
                updateState { copy(leakStatus = ToolStatus.WaitingForAction) }
            }
            MainViewEvent.ThreadViolationClicked -> handleThreadViolation()

            MainViewEvent.LocationClicked -> {
                updateState { copy(showTestToolsSheet = false) }
                emitEffect(MainViewEffect.NavigateToLocation)
            }
            MainViewEvent.WebViewClicked -> {
                updateState { copy(showTestToolsSheet = false) }
                emitEffect(MainViewEffect.NavigateToWebView)
            }
            MainViewEvent.SecureStorageClicked -> {
                updateState { copy(showTestToolsSheet = false) }
                emitEffect(MainViewEffect.NavigateToSecureStorage)
            }

            MainViewEvent.CrashConfirmed -> {
                updateState { copy(showCrashDialog = false, isGlitchEffectActive = true) }
            }
            MainViewEvent.CrashDialogDismissed -> updateState { copy(showCrashDialog = false) }
            MainViewEvent.TestToolsSheetDismissed -> updateState { copy(showTestToolsSheet = false) }

            MainViewEvent.CheckLeakRotation -> handleCheckLeakRotation()
            MainViewEvent.GlitchAnimationCompleted -> emitEffect(MainViewEffect.SimulateCrash)
        }
    }

    private fun handleRunApiTests() {
        doHttpActivity()
        doContentTypeTests()
        viewModelScope.launch {
            updateState { copy(apiTestStatus = ToolStatus.Running) }
            delay(STATUS_RUNNING_DURATION)
            updateState { copy(apiTestStatus = ToolStatus.Done) }
            delay(STATUS_DONE_DURATION)
            updateState { copy(apiTestStatus = ToolStatus.Idle) }
        }
    }

    private fun handleWebSocketTest() {
        SampleWebSocketService.connect()
        viewModelScope.launch {
            updateState { copy(webSocketStatus = ToolStatus.Running) }
            delay(STATUS_RUNNING_DURATION)
            updateState { copy(webSocketStatus = ToolStatus.Done) }
            delay(STATUS_DONE_DURATION)
            updateState { copy(webSocketStatus = ToolStatus.Idle) }
        }
    }

    private fun handleThreadViolation() {
        emitEffect(MainViewEffect.TriggerThreadViolation)
        viewModelScope.launch {
            updateState { copy(threadViolationStatus = ToolStatus.Done) }
            delay(STATUS_DONE_DURATION)
            updateState { copy(threadViolationStatus = ToolStatus.Idle) }
        }
    }

    private fun handleCheckLeakRotation() {
        viewModelScope.launch {
            if (checkLeakRotationDetected()) {
                updateState { copy(leakStatus = ToolStatus.Done) }
                delay(STATUS_DONE_DURATION)
                updateState { copy(leakStatus = ToolStatus.Idle) }
            }
        }
    }

    private fun doHttpActivity() {
        val api = SampleApiService.getInstance()

        val callBack = object : Callback<Void?> {
            override fun onResponse(
                call: Call<Void?>,
                response: Response<Void?>,
            ) = Unit
            override fun onFailure(
                call: Call<Void?>,
                t: Throwable,
            ) = Unit
        }

        api.get().enqueue(callBack)
        api.login(LoginData("user@example.com", "secretPassword123", true)).enqueue(callBack)
        api.post(Data("posted")).enqueue(callBack)
        api.postForm("I Am String", null, 2.34567891, 1234, false).enqueue(callBack)
        api.patch(Data("patched")).enqueue(callBack)
        api.put(Data("put")).enqueue(callBack)
        api.delete().enqueue(callBack)
        api.status(201).enqueue(callBack)
        api.status(401).enqueue(callBack)
        api.status(500).enqueue(callBack)
        api.delay(9).enqueue(callBack)
        api.delay(15).enqueue(callBack)
        api.bearer(UUID.randomUUID().toString()).enqueue(callBack)
        api.redirectTo("https://http2.akamai.com").enqueue(callBack)
        api.redirect(3).enqueue(callBack)
        api.redirectRelative(2).enqueue(callBack)
        api.redirectAbsolute(4).enqueue(callBack)
        api.stream(500).enqueue(callBack)
        api.streamBytes(2048).enqueue(callBack)
        api.image("image/png").enqueue(callBack)
        api.gzip().enqueue(callBack)
        api.xml().enqueue(callBack)
        api.utf8().enqueue(callBack)
        api.deflate().enqueue(callBack)
        api.cookieSet("v").enqueue(callBack)
        api.basicAuth("me", "pass").enqueue(callBack)
        api.drip(512, 5, 1, 200).enqueue(callBack)
        api.deny().enqueue(callBack)
        api.cache("Mon").enqueue(callBack)
        api.cache(30).enqueue(callBack)
        api.post(VeryLargeData()).enqueue(callBack)
    }

    private fun doContentTypeTests() {
        val api = SampleContentService.getInstance()

        val callBack = object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>,
            ) = Unit
            override fun onFailure(
                call: Call<ResponseBody>,
                t: Throwable,
            ) = Unit
        }

        api.pdf().enqueue(callBack)
        api.image().enqueue(callBack)
        api.imagePng().enqueue(callBack)
        api.imageWebp().enqueue(callBack)
        api.imageGif().enqueue(callBack)
        api.json().enqueue(callBack)
        api.xml().enqueue(callBack)
        api.html().enqueue(callBack)
    }

    companion object {
        private const val STATUS_RUNNING_DURATION = 800L
        private const val STATUS_DONE_DURATION = 1500L

        private val _leakedActivities = mutableListOf<Any>()
        private val _leakAwaitingRotation = AtomicBoolean(false)

        fun registerLeak(activity: Any) {
            _leakedActivities.add(activity)
            _leakAwaitingRotation.set(true)
        }

        fun checkLeakRotationDetected(): Boolean {
            return _leakAwaitingRotation.getAndSet(false)
        }
    }
}
