/*
 * Copyright AziKar24 2024.
 */

package com.azikar24.wormaceptorapp.sampleservice

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Sample API endpoints for testing content handling features:
 * - Image preview
 * - PDF viewer
 * - Various content types
 */
interface SampleContentApi {

    // PDF
    @GET
    fun pdf(@Url url: String = "https://www.stata.com/manuals/dsample.pdf"): Call<ResponseBody>

    // Images
    @GET
    fun image(@Url url: String = "https://picsum.photos/400/300"): Call<ResponseBody>

    @GET
    fun imagePng(@Url url: String = "https://placehold.co/400x300/png"): Call<ResponseBody>

    @GET
    fun imageWebp(@Url url: String = "https://www.gstatic.com/webp/gallery/1.webp"): Call<ResponseBody>

    @GET
    fun imageGif(@Url url: String = "https://media.giphy.com/media/3o7TKsQ8MgBdI7tD3y/giphy.gif"): Call<ResponseBody>

    // JSON
    @GET
    fun json(@Url url: String = "https://jsonplaceholder.typicode.com/posts/1"): Call<ResponseBody>

    // XML
    @GET
    fun xml(@Url url: String = "https://www.w3schools.com/xml/note.xml"): Call<ResponseBody>

    // HTML
    @GET
    fun html(@Url url: String = "https://example.com"): Call<ResponseBody>
}
