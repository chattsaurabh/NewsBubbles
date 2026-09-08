package com.mobiwiki.news.bubbles.model

enum class NewsCategory(
    val apiValue: String
) {
    BUSINESS("business"),
    ENTERTAINMENT("entertainment"),
    GENERAL("general"),
    HEALTH("health"),
    SCIENCE("science"),
    SPORTS("sports"),
    TECHNOLOGY("technology");

    val displayName: String
        get() = apiValue.replaceFirstChar { it.uppercaseChar() }
}
