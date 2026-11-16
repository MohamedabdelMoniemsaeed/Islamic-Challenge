package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.widgets.HomeScreenWidgetHelper
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.IslamicChallengeTheme
import com.example.ui.viewmodels.MainViewModel
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  private val pendingRouteState = mutableStateOf<String?>(null)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    pendingRouteState.value = intent?.getStringExtra(HomeScreenWidgetHelper.EXTRA_OPEN_ROUTE)
    CoroutineScope(Dispatchers.IO).launch {
      try {
        MobileAds.initialize(applicationContext) {}
      } catch (_: Throwable) {}
    }
    enableEdgeToEdge()
    setContent {
      val mainViewModel: MainViewModel = viewModel()
      val isDarkMode by mainViewModel.isDarkMode.collectAsState()
      val systemInDark = isSystemInDarkTheme()
      val actualDark = isDarkMode ?: systemInDark

      IslamicChallengeTheme(darkTheme = actualDark) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.surface
        ) {
          AppNavigation(
            mainViewModel = mainViewModel,
            pendingDeepLinkRoute = pendingRouteState.value,
            onConsumeDeepLinkRoute = { pendingRouteState.value = null }
          )
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    val route = intent.getStringExtra(HomeScreenWidgetHelper.EXTRA_OPEN_ROUTE)
    if (!route.isNullOrBlank()) {
      pendingRouteState.value = route
    }
  }
}

@androidx.compose.runtime.Composable
fun Greeting(name: String, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
  androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}
