/*
 * Copyright AziKar24 4/3/2023.
 */

package com.azikar24.wormaceptorapp

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptorapp.sampleservice.Data
import com.azikar24.wormaceptorapp.sampleservice.LoginData
import com.azikar24.wormaceptorapp.sampleservice.SampleApiService
import com.azikar24.wormaceptorapp.sampleservice.SampleContentService
import com.azikar24.wormaceptorapp.sampleservice.SampleWebSocketService
import com.azikar24.wormaceptorapp.sampleservice.VeryLargeData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class MainActivityViewModel : ViewModel() {

    fun goToGithub(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = context.getString(R.string.github_link).toUri()
        }
        context.startActivity(intent)
    }

    fun doHttpActivity(context: Context) {
        val api = SampleApiService.getInstance()

        val callBack = object : Callback<Void?> {
            override fun onResponse(call: Call<Void?>, response: Response<Void?>) = Unit
            override fun onFailure(call: Call<Void?>, t: Throwable) = Unit
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

    fun startWormaCeptor(context: Context) {
        context.startActivity(WormaCeptorApi.getLaunchIntent(context))
    }

    fun doContentTypeTests() {
        val api = SampleContentService.getInstance()

        val callBack = object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) = Unit
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) = Unit
        }

        // PDF
        api.pdf().enqueue(callBack)

        // Images
        api.image().enqueue(callBack)
        api.imagePng().enqueue(callBack)
        api.imageWebp().enqueue(callBack)
        api.imageGif().enqueue(callBack)

        // JSON
        api.json().enqueue(callBack)

        // XML
        api.xml().enqueue(callBack)

        // HTML
        api.html().enqueue(callBack)
    }

    fun doWebSocketTest() {
        SampleWebSocketService.connect()
    }

    fun simulateCrash() {
        val x = arrayOf("")
        x[4]
    }
}
