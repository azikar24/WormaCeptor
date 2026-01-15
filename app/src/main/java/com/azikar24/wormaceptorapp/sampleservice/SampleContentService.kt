/*
 * Copyright AziKar24 2024.
 */

package com.azikar24.wormaceptorapp.sampleservice

import com.azikar24.wormaceptor.api.WormaCeptorInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object SampleContentService {

    fun getInstance(): SampleContentApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://example.com") // Base URL not used since all endpoints use @Url
            .client(getClient())
            .build()
        return retrofit.create(SampleContentApi::class.java)
    }

    private fun getClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                WormaCeptorInterceptor()
                    .showNotification(true)
                    .maxContentLength(500000L) // Larger limit for PDFs/images
                    .retainDataFor(WormaCeptorInterceptor.Period.FOREVER)
            )
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
            .build()
    }
}
