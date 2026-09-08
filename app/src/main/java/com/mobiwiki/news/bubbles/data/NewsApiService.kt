package com.mobiwiki.news.bubbles.data

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("category") category: String
    ): NewsApiResponse
}
