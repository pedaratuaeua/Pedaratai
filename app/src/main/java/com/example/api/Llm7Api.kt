package com.example.api

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ChatMessageItem(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessageItem>,
    val temperature: Double = 0.7
)

@JsonClass(generateAdapter = true)
data class ChatChoice(
    val index: Int? = null,
    val message: ChatMessageItem
)

@JsonClass(generateAdapter = true)
data class ChatResponse(
    val choices: List<ChatChoice>
)

@JsonClass(generateAdapter = true)
data class ImageRequest(
    val model: String,
    val prompt: String,
    val n: Int = 1,
    val size: String = "1024x1024"
)

@JsonClass(generateAdapter = true)
data class ImageData(
    val url: String
)

@JsonClass(generateAdapter = true)
data class ImageResponse(
    val data: List<ImageData>
)

@JsonClass(generateAdapter = true)
data class ModelItem(
    val id: String
)

@JsonClass(generateAdapter = true)
data class ModelsResponse(
    val data: List<ModelItem>
)

interface Llm7Api {
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") authHeader: String,
        @Body request: ChatRequest
    ): ChatResponse

    @POST("images/generations")
    suspend fun generateImage(
        @Header("Authorization") authHeader: String,
        @Body request: ImageRequest
    ): ImageResponse

    @GET("models")
    suspend fun getModels(
        @Header("Authorization") authHeader: String
    ): ModelsResponse
}

object RetrofitClient {
    fun createService(baseUrl: String, okHttpClient: OkHttpClient): Llm7Api {
        val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(sanitizedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(Llm7Api::class.java)
    }

    fun createOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }
}
