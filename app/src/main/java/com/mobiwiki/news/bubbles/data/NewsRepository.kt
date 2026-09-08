package com.mobiwiki.news.bubbles.data

import com.mobiwiki.news.bubbles.model.Article
import com.mobiwiki.news.bubbles.model.CategoryNews
import com.mobiwiki.news.bubbles.model.NewsCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val apiService: NewsApiService
) {

    suspend fun getTopHeadlines(category: NewsCategory): CategoryNews {
        val response = apiService.getTopHeadlines(category = category.apiValue)
        val categoryNews = CategoryNews(
            category = category,
            articles = response.articles.mapNotNull { it.toArticle() }
        )
        return categoryNews
    }

    private fun ArticleDto.toArticle(): Article? {
        val articleTitle = title?.trim()?.takeIf(String::isNotEmpty) ?: return null
        val articleSource = source.name?.trim()?.takeIf(String::isNotEmpty) ?: UNKNOWN_SOURCE

        return Article(
            title = articleTitle,
            description = description,
            sourceName = articleSource,
            imageUrl = urlToImage,
            articleUrl = url,
            publishedAt = publishedAt
        )
    }

    private companion object {
        const val UNKNOWN_SOURCE = "Unknown source"
    }
}
