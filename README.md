# News Bubbles

News Bubbles is a small Android app that shows the seven News API categories as animated bubbles. Tapping a bubble loads the latest headlines for that category.

## Features

- Seven continuously moving news category bubbles
- Bubble-to-bubble collisions and screen-edge bouncing
- An icon and label for each category
- Category selection with Compose Navigation
- Article list built with Jetpack Compose
- Article titles, optional descriptions and images, and source names
- Loading, empty, and error states
- Retry and refresh actions

## Tech

- Kotlin and Jetpack Compose
- Coroutines and StateFlow
- Navigation Compose
- Hilt
- Retrofit, Moshi, and OkHttp
- Coil

The bubble movement and collision logic is implemented directly in Kotlin. No physics library is used.

## Project structure

- `data` - News API client, Retrofit service, DTOs, and repository
- `model` - article and news category models
- `di` - Hilt network module
- `ui/bubbles` - animated bubble screen and physics
- `ui/articles` - article list and ViewModel
- `ui/navigation` - the two-screen navigation graph

The repository maps News API responses to the app models. `ArticlesViewModel` loads the selected category and exposes the screen state. Requests are made when a category is opened; there is no response cache or offline storage.

## News API key

Register for a key at [newsapi.org/register](https://newsapi.org/register).

The key is configured in `app/build.gradle.kts`.

Note: Typically API keys are kept in `local.properties`, but for the purposes of demonstration
and to allow this project to run without having reviewers to add the api key explicitly, as an 
exception, the api key for this project has been stored in `app/build.gradle.kts`

Gradle exposes this as `BuildConfig.NEWS_API_KEY`. The OkHttp client sends it in the `X-Api-Key` header.

## Run

1. Open the project in Android Studio.
2. Sync Gradle.
3. Run the app on an emulator or device running Android 9 (API 28) or newer.

## End
