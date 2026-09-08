package com.mobiwiki.news.bubbles.ui.articles

import androidx.lifecycle.SavedStateHandle
import com.mobiwiki.news.bubbles.data.ArticleDto
import com.mobiwiki.news.bubbles.data.NewsApiResponse
import com.mobiwiki.news.bubbles.data.NewsApiService
import com.mobiwiki.news.bubbles.data.NewsRepository
import com.mobiwiki.news.bubbles.data.SourceDto
import com.mobiwiki.news.bubbles.model.NewsCategory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ArticlesViewModelTest {

    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsSelectedCategoryAndPublishesSuccess() = runTest(testDispatcher.scheduler) {
        val service = FakeNewsApiService { response(article("Loaded article")) }
        val viewModel = viewModel(service)

        assertTrue(viewModel.uiState.value.isLoading)
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertEquals(NewsCategory.TECHNOLOGY, category)
            assertFalse(isLoading)
            assertNull(errorMessage)
            assertEquals("Loaded article", articles.single().title)
        }
        assertEquals(listOf("technology"), service.requestedCategories)
    }

    @Test
    fun init_treatsEmptyResponseAsSuccess() = runTest(testDispatcher.scheduler) {
        val viewModel = viewModel(FakeNewsApiService { response() })

        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertFalse(isLoading)
            assertTrue(articles.isEmpty())
            assertNull(errorMessage)
        }
    }

    @Test
    fun init_mapsHttpExceptionToApiMessage() = runTest(testDispatcher.scheduler) {
        val httpException = HttpException(
            Response.error<NewsApiResponse>(500, "server error".toResponseBody())
        )
        val viewModel = viewModel(FakeNewsApiService { throw httpException })

        advanceUntilIdle()

        assertEquals(
            "Unable to load news right now. Please try again.",
            viewModel.uiState.value.errorMessage
        )
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun retryAndRefresh_whileLoadingDoNotStartDuplicateRequests() =
        runTest(testDispatcher.scheduler) {
            val requestGate = CompletableDeferred<Unit>()
            val service = FakeNewsApiService {
                requestGate.await()
                response(article("Loaded once"))
            }
            val viewModel = viewModel(service)
            runCurrent()

            viewModel.retry()
            viewModel.refresh()

            assertEquals(1, service.callCount)
            requestGate.complete(Unit)
            advanceUntilIdle()
            assertEquals(1, service.callCount)
        }

    @Test
    fun savedStateHandle_parsesValidSportsCategory() = runTest(testDispatcher.scheduler) {
        val service = FakeNewsApiService { response() }
        val viewModel = viewModel(service, NewsCategory.SPORTS.name)

        advanceUntilIdle()

        assertEquals(NewsCategory.SPORTS, viewModel.uiState.value.category)
        assertEquals(listOf("sports"), service.requestedCategories)
    }

    @Test
    fun savedStateHandle_throwsWhenCategoryIsMissing() {
        assertThrows(IllegalArgumentException::class.java) {
            ArticlesViewModel(SavedStateHandle(), NewsRepository(FakeNewsApiService { response() }))
        }
    }

    @Test
    fun savedStateHandle_throwsWhenCategoryIsInvalid() {
        assertThrows(IllegalArgumentException::class.java) {
            ArticlesViewModel(
                SavedStateHandle(mapOf(ArticlesViewModel.CATEGORY_ARG to "NOT_A_CATEGORY")),
                NewsRepository(FakeNewsApiService { response() })
            )
        }
    }

    private fun viewModel(
        service: NewsApiService,
        category: String = NewsCategory.TECHNOLOGY.name
    ) = ArticlesViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf(ArticlesViewModel.CATEGORY_ARG to category)
        ),
        repository = NewsRepository(service)
    )

    private class FakeNewsApiService(
        private val handler: suspend (String) -> NewsApiResponse
    ) : NewsApiService {
        val requestedCategories = mutableListOf<String>()
        val callCount: Int get() = requestedCategories.size

        override suspend fun getTopHeadlines(category: String): NewsApiResponse {
            requestedCategories += category
            return handler(category)
        }
    }

    private companion object {
        fun response(vararg articles: ArticleDto) = NewsApiResponse(
            status = "ok",
            totalResults = articles.size,
            articles = articles.toList()
        )

        fun article(title: String) = ArticleDto(
            source = SourceDto(id = null, name = "Test Source"),
            author = null,
            title = title,
            description = "Description",
            url = "https://example.com/$title",
            urlToImage = null,
            publishedAt = null
        )
    }
}
