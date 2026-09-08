package com.mobiwiki.news.bubbles.model

import org.junit.Assert.assertEquals
import org.junit.Test

class NewsCategoryTest {

    @Test
    fun entries_matchNewsApiValuesAndDisplayNames() {
        assertEquals(
            listOf(
                Triple(NewsCategory.BUSINESS, "business", "Business"),
                Triple(NewsCategory.ENTERTAINMENT, "entertainment", "Entertainment"),
                Triple(NewsCategory.GENERAL, "general", "General"),
                Triple(NewsCategory.HEALTH, "health", "Health"),
                Triple(NewsCategory.SCIENCE, "science", "Science"),
                Triple(NewsCategory.SPORTS, "sports", "Sports"),
                Triple(NewsCategory.TECHNOLOGY, "technology", "Technology")
            ),
            NewsCategory.entries.map { category ->
                Triple(category, category.apiValue, category.displayName)
            }
        )
    }
}
