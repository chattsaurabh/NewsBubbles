package com.mobiwiki.news.bubbles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mobiwiki.news.bubbles.ui.navigation.AppNavigation
import com.mobiwiki.news.bubbles.ui.theme.NewsBubblesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewsBubblesTheme {
                AppNavigation()
            }
        }
    }
}
