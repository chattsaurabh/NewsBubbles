package com.mobiwiki.news.bubbles.di

import com.mobiwiki.news.bubbles.data.NewsApiClient
import com.mobiwiki.news.bubbles.data.NewsApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNewsApiService(): NewsApiService = NewsApiClient.service
}
