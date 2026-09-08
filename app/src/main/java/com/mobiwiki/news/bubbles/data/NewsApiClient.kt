package com.mobiwiki.news.bubbles.data

import com.mobiwiki.news.bubbles.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NewsApiClient {

    private const val BASE_URL = "https://newsapi.org/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .header("X-Api-Key", BuildConfig.NEWS_API_KEY)
                .build()

            chain.proceed(request)
        }
        .build()

    val service: NewsApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(NewsApiService::class.java)
}
