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
    fun pdf(@Url url: String = SampleDataConstants.Pdf.STATA_MANUAL): Call<ResponseBody>

    // Images
    @GET
    fun image(@Url url: String = SampleDataConstants.Images.PICSUM): Call<ResponseBody>

    @GET
    fun imagePng(@Url url: String = SampleDataConstants.Images.PLACEHOLDER_PNG): Call<ResponseBody>

    @GET
    fun imageWebp(@Url url: String = SampleDataConstants.Images.WEBP_GALLERY): Call<ResponseBody>

    @GET
    fun imageGif(@Url url: String = SampleDataConstants.Images.GIPHY_GIF): Call<ResponseBody>

    // JSON
    @GET
    fun json(@Url url: String = SampleDataConstants.Json.PLACEHOLDER_POST): Call<ResponseBody>

    // XML
    @GET
    fun xml(@Url url: String = SampleDataConstants.Xml.W3SCHOOLS_NOTE): Call<ResponseBody>

    // HTML
    @GET
    fun html(@Url url: String = SampleDataConstants.Html.EXAMPLE): Call<ResponseBody>
}
