package com.mobiwiki.news.bubbles.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mobiwiki.news.bubbles.ui.articles.ArticlesScreen
import com.mobiwiki.news.bubbles.ui.articles.ArticlesViewModel
import com.mobiwiki.news.bubbles.ui.bubbles.BubblesScreen

private const val BUBBLES_ROUTE = "bubbles"
private const val CATEGORY_ARG = "category"
private const val ARTICLES_ROUTE = "articles/{$CATEGORY_ARG}"

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = BUBBLES_ROUTE,
        modifier = modifier
    ) {
        composable(BUBBLES_ROUTE) {
            BubblesScreen(
                onCategoryClick = { category ->
                    navController.navigate("articles/${category.name}") {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = ARTICLES_ROUTE,
            arguments = listOf(
                navArgument(CATEGORY_ARG) {
                    type = NavType.StringType
                }
            )
        ) {
            val viewModel: ArticlesViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            ArticlesScreen(
                uiState = uiState,
                onBackClick = navController::popBackStack,
                onRetryClick = viewModel::retry,
                onRefreshClick = viewModel::refresh
            )
        }
    }
}
