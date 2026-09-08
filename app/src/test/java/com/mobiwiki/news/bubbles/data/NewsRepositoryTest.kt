package com.mobiwiki.news.bubbles.data

import com.mobiwiki.news.bubbles.model.NewsCategory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class NewsRepositoryTest {

    @Test
    fun getTopHeadlines_mapsDtoAndPreservesRequestedCategory() = runBlocking {
        val service = FakeNewsApiService(response = response(article()))
        val repository = NewsRepository(service)

        val result = repository.getTopHeadlines(NewsCategory.TECHNOLOGY)

        assertEquals(NewsCategory.TECHNOLOGY, result.category)
        assertEquals(1, result.articles.size)
        with(result.articles.single()) {
            assertEquals("Test title", title)
            assertEquals("Test description", description)
            assertEquals("Test Source", sourceName)
            assertEquals("https://example.com/image.jpg", imageUrl)
            assertEquals("https://example.com/article", articleUrl)
            assertEquals("2026-09-02T12:00:00Z", publishedAt)
        }
    }

    @Test
    fun getTopHeadlines_passesCategoryApiValue() = runBlocking {
        val service = FakeNewsApiService()

        NewsRepository(service).getTopHeadlines(NewsCategory.TECHNOLOGY)

        assertEquals(listOf("technology"), service.requestedCategories)
    }

    private class FakeNewsApiService(
        var response: NewsApiResponse = response()
    ) : NewsApiService {
        var failure: Exception? = null
        val requestedCategories = mutableListOf<String>()

        override suspend fun getTopHeadlines(category: String): NewsApiResponse {
            requestedCategories += category
            failure?.let { throw it }
            return response
        }
    }

    private companion object {
        fun response(vararg articles: ArticleDto) = NewsApiResponse(
            status = "ok",
            totalResults = articles.size,
            articles = articles.toList()
        )

        fun article(
            title: String? = "Test title",
            sourceName: String? = "Test Source"
        ) = ArticleDto(
            source = SourceDto(id = "source-id", name = sourceName),
            author = "Test Author",
            title = title,
            description = "Test description",
            url = "https://example.com/article",
            urlToImage = "https://example.com/image.jpg",
            publishedAt = "2026-09-02T12:00:00Z"
        )
    }
}
