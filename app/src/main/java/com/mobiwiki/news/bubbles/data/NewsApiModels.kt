package com.mobiwiki.news.bubbles.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class NewsApiResponse(
    val status: String?,
    val totalResults: Int?,
    val articles: List<ArticleDto>
)

@JsonClass(generateAdapter = false)
data class ArticleDto(
    val source: SourceDto,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?
)

@JsonClass(generateAdapter = false)
data class SourceDto(
    val id: String?,
    val name: String?
)
