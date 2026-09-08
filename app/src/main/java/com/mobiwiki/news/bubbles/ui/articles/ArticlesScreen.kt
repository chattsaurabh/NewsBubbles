package com.mobiwiki.news.bubbles.ui.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mobiwiki.news.bubbles.model.Article
import com.mobiwiki.news.bubbles.model.NewsCategory
import com.mobiwiki.news.bubbles.ui.theme.NewsBubblesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesScreen(
    uiState: ArticlesUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(uiState.category.displayName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState.isLoading && uiState.articles.isNotEmpty()) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(horizontal = 14.dp)
                                .size(20.dp)
                                .semantics {
                                    contentDescription = "Refreshing news"
                                },
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = onRefreshClick,
                            enabled = !uiState.isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh news"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                CenteredContent(modifier = Modifier.padding(innerPadding)) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null && uiState.articles.isEmpty() -> {
                CenteredContent(modifier = Modifier.padding(innerPadding)) {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onRetryClick) {
                        Text("Retry")
                    }
                }
            }

            uiState.articles.isEmpty() -> {
                CenteredContent(modifier = Modifier.padding(innerPadding)) {
                    Text(
                        text = "No articles available for this category.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                ArticlesList(
                    articles = uiState.articles,
                    errorMessage = uiState.errorMessage,
                    onRetryClick = onRetryClick,
                    contentPadding = innerPadding
                )
            }
        }
    }
}

@Composable
private fun ArticlesList(
    articles: List<Article>,
    errorMessage: String?,
    onRetryClick: () -> Unit,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            end = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (errorMessage != null) {
            item(key = "load-error") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = onRetryClick) {
                        Text("Retry")
                    }
                }
            }
        }

        itemsIndexed(
            items = articles,
            key = { index, article ->
                article.articleUrl?.takeIf { it.isNotBlank() }
                    ?: "${article.sourceName}:${article.title}:$index"
            }
        ) { _, article ->
            ArticleCard(
                article = article,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ArticleCard(
    article: Article,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        val imageUrl = article.imageUrl?.takeIf { it.isNotBlank() }
        var imageFailed by remember(imageUrl) { mutableStateOf(false) }

        if (imageUrl != null && !imageFailed) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentScale = ContentScale.FillWidth,
                onError = { imageFailed = true }
            )
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleLarge
            )

            article.description?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.size(2.dp))

            Text(
                text = article.sourceName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CenteredContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PopulatedArticlesPreview() {
    NewsBubblesTheme {
        ArticlesScreen(
            uiState = ArticlesUiState(
                category = NewsCategory.TECHNOLOGY,
                articles = listOf(
                    Article(
                        title = "A sample technology headline that wraps naturally",
                        description = "A short description gives readers more context about the story.",
                        sourceName = "Example News",
                        imageUrl = null,
                        articleUrl = "https://example.com/article",
                        publishedAt = "2026-09-02T12:00:00Z"
                    ),
                    Article(
                        title = "Another headline without a description",
                        description = null,
                        sourceName = "Daily Example",
                        imageUrl = null,
                        articleUrl = "https://example.com/another-article",
                        publishedAt = null
                    )
                )
            ),
            onBackClick = {},
            onRetryClick = {},
            onRefreshClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorArticlesPreview() {
    NewsBubblesTheme {
        ArticlesScreen(
            uiState = ArticlesUiState(
                category = NewsCategory.SCIENCE,
                errorMessage = "Unable to load news right now. Please try again."
            ),
            onBackClick = {},
            onRetryClick = {},
            onRefreshClick = {}
        )
    }
}
