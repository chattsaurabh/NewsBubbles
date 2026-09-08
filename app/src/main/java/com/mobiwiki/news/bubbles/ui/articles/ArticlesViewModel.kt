package com.mobiwiki.news.bubbles.ui.articles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiwiki.news.bubbles.data.NewsRepository
import com.mobiwiki.news.bubbles.model.Article
import com.mobiwiki.news.bubbles.model.NewsCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

data class ArticlesUiState(
    val category: NewsCategory,
    val isLoading: Boolean = false,
    val articles: List<Article> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: NewsRepository
) : ViewModel() {

    private val category = savedStateHandle.categoryArgument()
    private val _uiState = MutableStateFlow(ArticlesUiState(category = category))
    val uiState: StateFlow<ArticlesUiState> = _uiState.asStateFlow()

    init {
        loadArticles()
    }

    fun retry() {
        loadArticles()
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        loadArticles()
    }

    private fun loadArticles() {
        if (_uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                val result = repository.getTopHeadlines(category)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    articles = result.articles,
                    errorMessage = null
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: IOException) {
                showError(NETWORK_ERROR_MESSAGE)
            } catch (_: HttpException) {
                showError(API_ERROR_MESSAGE)
            } catch (_: Exception) {
                showError(UNEXPECTED_ERROR_MESSAGE)
            }
        }
    }

    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message
        )
    }

    companion object {
        const val CATEGORY_ARG = "category"

        private const val NETWORK_ERROR_MESSAGE =
            "Unable to connect. Check your internet connection and try again."
        private const val API_ERROR_MESSAGE = "Unable to load news right now. Please try again."
        private const val UNEXPECTED_ERROR_MESSAGE = "Something went wrong. Please try again."
    }
}

private fun SavedStateHandle.categoryArgument(): NewsCategory {
    val value = get<String>(ArticlesViewModel.CATEGORY_ARG)
        ?: throw IllegalArgumentException("Missing category navigation argument")

    return try {
        NewsCategory.valueOf(value)
    } catch (exception: IllegalArgumentException) {
        throw IllegalArgumentException("Invalid category navigation argument: $value", exception)
    }
}
