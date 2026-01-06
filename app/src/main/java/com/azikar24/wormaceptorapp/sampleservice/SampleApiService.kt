/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptorapp.sampleservice

import com.azikar24.wormaceptor.api.WormaCeptorInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SampleApiService {

    fun getInstance(): HttpBinApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://httpbin.org")
            .addConverterFactory(GsonConverterFactory.create())
            .client(getClient())
            .build()
        return retrofit.create(HttpBinApi::class.java)
    }

    private fun getClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                WormaCeptorInterceptor()
                    .showNotification(true)
                    .maxContentLength(250000L)
                    .retainDataFor(WormaCeptorInterceptor.Period.FOREVER)
                    .redactHeader("Authorization")
            )
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }
}