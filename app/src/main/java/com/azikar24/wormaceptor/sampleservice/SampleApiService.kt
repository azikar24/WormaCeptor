/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.sampleservice

import android.content.Context
import com.azikar24.wormaceptor.WormaCeptorInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SampleApiService {

    fun getInstance(context: Context): HttpbinApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://httpbin.org")
            .addConverterFactory(GsonConverterFactory.create())
            .client(getClient(context))
            .build()
        return retrofit.create(HttpbinApi::class.java)
    }

    private fun getClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                WormaCeptorInterceptor(context)
                    .showNotification(true)
                    .maxContentLength(250000L)
                    .retainDataFor(WormaCeptorInterceptor.Period.FOREVER)
                    .redactHeader("Authorization")
            )
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }
}