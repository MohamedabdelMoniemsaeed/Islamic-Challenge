package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.IslamicChallengeTheme
import com.example.ui.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val mainViewModel: MainViewModel = viewModel()
      val isDarkMode by mainViewModel.isDarkMode.collectAsState()
      val systemInDark = isSystemInDarkTheme()
      val actualDark = isDarkMode ?: systemInDark

      IslamicChallengeTheme(darkTheme = actualDark) {
        AppNavigation(mainViewModel = mainViewModel)
      }
    }
  }
}

@androidx.compose.runtime.Composable
fun Greeting(name: String, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
  androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}
