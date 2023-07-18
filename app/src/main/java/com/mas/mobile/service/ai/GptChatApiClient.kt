package com.mas.mobile.service.ai

import com.mas.mobile.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GptChatApiClient {
    private val retrofit: Retrofit by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Authorization", "Bearer ${BuildConfig.API_KEY}")
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val gptChatService: GptChatService by lazy {
        retrofit.create(GptChatService::class.java)
    }
}