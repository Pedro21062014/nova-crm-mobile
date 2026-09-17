package com.example

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val role: String,
    val content: String
)

data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>
)

data class ChatChoice(
    val message: ChatMessage
)

data class ChatCompletionResponse(
    val choices: List<ChatChoice>?
)

interface DeepSeekApiService {
    @POST("chat/completions")
    suspend fun getCompletions(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest,
        @Header("HTTP-Referer") referer: String = "http://localhost",
        @Header("X-Title") title: String = "Nova CRM Android Native"
    ): Response<ChatCompletionResponse>
}

object DeepSeekClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun createService(apiKey: String): DeepSeekApiService {
        // Automatically determine Base URL based on the API Key pattern
        val baseUrl = if (apiKey.startsWith("sk-or-")) {
            "https://openrouter.ai/api/v1/"
        } else {
            "https://api.deepseek.com/v1/"
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(DeepSeekApiService::class.java)
    }

    fun getModelName(apiKey: String): String {
        return if (apiKey.startsWith("sk-or-")) {
            "deepseek/deepseek-chat" // OpenRouter deepseek model identifier
        } else {
            "deepseek-chat" // Official DeepSeek model identifier
        }
    }
}
