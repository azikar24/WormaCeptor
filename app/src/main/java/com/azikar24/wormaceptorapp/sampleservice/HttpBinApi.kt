/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptorapp.sampleservice

import retrofit2.Call
import retrofit2.http.*


interface HttpBinApi {
    @GET("/get")
    fun get(): Call<Void?>

    @POST("/post")
    fun post(@Body body: Data?): Call<Void?>

    @POST("/post")
    fun post(@Body body: VeryLargeData?): Call<Void?>

    @POST("/post")
    @FormUrlEncoded
    @Headers("ContentType: application/x-www-form-urlencoded")
    fun postForm(@Field("param_string") string: String?, @Field("param_string_null") stringNil: String?, @Field("param_double") param2: Double, @Field("param_int") param3: Int, @Field("param_bool") param4: Boolean): Call<Void>

    @PATCH("/patch")
    fun patch(@Body body: Data?): Call<Void>

    @PUT("/put")
    @Headers("Cache-Control: max-age=640000", "Library: WormaCeptor", "Client: Sample", "X-Foo: Bar", "X-Ping: Pong")
    fun put(@Body body: Data?): Call<Void>

    @DELETE("/delete")
    fun delete(): Call<Void>

    @GET("/bearer")
    fun bearer(@Header("Authorization") token: String?): Call<Void>

    @GET("/status/{code}")
    fun status(@Path("code") code: Int): Call<Void>

    @GET("/stream/{lines}")
    fun stream(@Path("lines") lines: Int): Call<Void>

    @GET("/stream-bytes/{bytes}")
    fun streamBytes(@Path("bytes") bytes: Int): Call<Void>

    @GET("/delay/{seconds}")
    fun delay(@Path("seconds") seconds: Int): Call<Void>

    @GET("/redirect-to")
    fun redirectTo(@Query("url") url: String?): Call<Void>

    @GET("/redirect/{times}")
    fun redirect(@Path("times") times: Int): Call<Void>

    @GET("/relative-redirect/{times}")
    fun redirectRelative(@Path("times") times: Int): Call<Void>

    @GET("/absolute-redirect/{times}")
    fun redirectAbsolute(@Path("times") times: Int): Call<Void>

    @GET("/image")
    fun image(@Header("Accept") accept: String?): Call<Void>

    @GET("/gzip")
    fun gzip(): Call<Void>

    @GET("/xml")
    fun xml(): Call<Void>

    @GET("/encoding/utf8")
    fun utf8(): Call<Void>

    @GET("/deflate")
    fun deflate(): Call<Void>

    @GET("/cookies/set")
    fun cookieSet(@Query("k1") value: String?): Call<Void>

    @GET("/basic-auth/{user}/{passwd}")
    fun basicAuth(@Path("user") user: String?, @Path("passwd") passwd: String?): Call<Void>

    @GET("/drip")
    fun drip(@Query("numbytes") bytes: Int, @Query("duration") seconds: Int, @Query("delay") delay: Int, @Query("code") code: Int): Call<Void>

    @GET("/deny")
    fun deny(): Call<Void>

    @GET("/cache")
    fun cache(@Header("If-Modified-Since") ifModifiedSince: String?): Call<Void>

    @GET("/cache/{seconds}")
    fun cache(@Path("seconds") seconds: Int): Call<Void>
}
