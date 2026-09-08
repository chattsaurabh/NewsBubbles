package com.mobiwiki.news.bubbles.model

data class Article(
    val title: String,
    val description: String?,
    val sourceName: String,
    val imageUrl: String?,
    val articleUrl: String?,
    val publishedAt: String?
)

data class CategoryNews(
    val category: NewsCategory,
    val articles: List<Article>
)
