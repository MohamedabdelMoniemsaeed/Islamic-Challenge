package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.ads.AdMobInterstitialManager
import com.example.ui.components.AdMobBannerBar
import com.example.core.localization.AppLanguage
import com.example.data.models.GameMode
import com.example.data.models.QuizCategory
import com.example.ui.screens.CategoryScreen
import com.example.ui.screens.DailyRewardScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.PrayerTimesScreen
import com.example.ui.screens.ProfileAndStatsScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.ResultsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.PrayerTimesViewModel
import com.example.ui.viewmodels.QuizViewModel

object Routes {
  const val SPLASH = "splash"
  const val ONBOARDING = "onboarding"
  const val HOME = "home"
  const val CATEGORIES = "categories"
  const val QUIZ = "quiz"
  const val RESULTS = "results"
  const val DAILY_REWARD = "daily_reward"
  const val PROFILE_STATS = "profile_stats"
  const val SETTINGS = "settings"
  const val PRAYER_TIMES = "prayer_times"
}

@Composable
fun AppNavigation(
  mainViewModel: MainViewModel,
  navController: NavHostController = rememberNavController()
) {
  val language by mainViewModel.language.collectAsState()
  val profile by mainViewModel.playerProfile.collectAsState()
  val unlockedAchievements by mainViewModel.unlockedAchievements.collectAsState()
  val categoryProgress by mainViewModel.categoryProgress.collectAsState()
  val soundEnabled by mainViewModel.soundEnabled.collectAsState()
  val musicEnabled by mainViewModel.musicEnabled.collectAsState()
  val vibrationEnabled by mainViewModel.vibrationEnabled.collectAsState()
  val soundVolume by mainViewModel.soundVolume.collectAsState()
  val isDarkMode by mainViewModel.isDarkMode.collectAsState()
  val notificationsEnabled by mainViewModel.notificationsEnabled.collectAsState()
  val showInAppBanner by mainViewModel.showInAppBanner.collectAsState()

  val quizViewModel = remember {
    QuizViewModel(
      questionRepository = mainViewModel.questionRepository,
      playerRepository = mainViewModel.playerRepository,
      audioManager = mainViewModel.audioManager
    )
  }

  val context = LocalContext.current
  val interstitialManager = remember { AdMobInterstitialManager(context.applicationContext) }
  val prayerTimesViewModel: PrayerTimesViewModel = viewModel()

  val layoutDirection = if (language == AppLanguage.ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr
  val currentBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = currentBackStackEntry?.destination?.route
  val showBottomAdBar = currentRoute != null && currentRoute != Routes.SPLASH && currentRoute != Routes.ONBOARDING

  CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
    Scaffold(
      bottomBar = {
        if (showBottomAdBar) {
          AdMobBannerBar()
        }
      },
      contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
      NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .consumeWindowInsets(innerPadding),
        enterTransition = { fadeIn(animationSpec = tween(280)) },
        exitTransition = { fadeOut(animationSpec = tween(280)) }
      ) {
      // 1. Splash Screen
      composable(Routes.SPLASH) {
        SplashScreen(
          language = language,
          onSplashFinished = {
            if (profile.hasCompletedOnboarding) {
              navController.navigate(Routes.HOME) {
                popUpTo(Routes.SPLASH) { inclusive = true }
              }
            } else {
              navController.navigate(Routes.ONBOARDING) {
                popUpTo(Routes.SPLASH) { inclusive = true }
              }
            }
          }
        )
      }

      // 2. Onboarding Screen
      composable(Routes.ONBOARDING) {
        OnboardingScreen(
          language = language,
          onComplete = {
            mainViewModel.completeOnboarding()
            navController.navigate(Routes.HOME) {
              popUpTo(Routes.ONBOARDING) { inclusive = true }
            }
          }
        )
      }

      // 3. Home Screen
      composable(Routes.HOME) {
        val canClaim = mainViewModel.playerRepository.canClaimDailyReward(profile)
        androidx.compose.runtime.LaunchedEffect(canClaim, notificationsEnabled) {
          if (canClaim && notificationsEnabled) {
            mainViewModel.notificationManager.sendDailyRewardNotification(language == AppLanguage.ARABIC)
          }
        }

        HomeScreen(
          profile = profile,
          language = language,
          canClaimReward = canClaim,
          showInAppBanner = showInAppBanner,
          isDailyChallengeCompleted = mainViewModel.playerRepository.isDailyChallengeCompletedToday(profile),
          unlockedAchievements = unlockedAchievements,
          onStartMode = { mode, category ->
            quizViewModel.startQuiz(mode, category)
            navController.navigate(Routes.QUIZ)
          },
          onNavigateToCategories = { navController.navigate(Routes.CATEGORIES) },
          onNavigateToAchievements = { navController.navigate(Routes.PROFILE_STATS) },
          onNavigateToDailyReward = { navController.navigate(Routes.DAILY_REWARD) },
          onNavigateToProfileStats = { navController.navigate(Routes.PROFILE_STATS) },
          onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
          onNavigateToPrayerTimes = { navController.navigate(Routes.PRAYER_TIMES) },
          onDismissInAppBanner = { mainViewModel.toggleInAppBanner(false) }
        )
      }

      // 4. Categories Screen
      composable(Routes.CATEGORIES) {
        CategoryScreen(
          language = language,
          progressMap = categoryProgress,
          onCategorySelected = { category ->
            quizViewModel.startQuiz(GameMode.CATEGORY_CHALLENGE, category)
            navController.navigate(Routes.QUIZ)
          },
          onBack = { navController.popBackStack() }
        )
      }

      // 5. Quiz Screen
      composable(Routes.QUIZ) {
        QuizScreen(
          quizViewModel = quizViewModel,
          profile = profile,
          language = language,
          soundEnabled = soundEnabled,
          vibrationEnabled = vibrationEnabled,
          onQuizFinished = {
            val activity = context.findActivity()
            interstitialManager.showInterstitialIfReady(activity) {
              navController.navigate(Routes.RESULTS) {
                popUpTo(Routes.QUIZ) { inclusive = true }
              }
            }
          },
          onExit = {
            navController.navigate(Routes.HOME) {
              popUpTo(Routes.HOME) { inclusive = false }
              launchSingleTop = true
            }
          }
        )
      }

      // 6. Results Screen
      composable(Routes.RESULTS) {
        val resultState by quizViewModel.resultState.collectAsState()
        val res = resultState
        if (res != null) {
          ResultsScreen(
            result = res,
            language = language,
            onPlayAgain = {
              quizViewModel.startQuiz(res.mode, res.category)
              navController.navigate(Routes.QUIZ) {
                popUpTo(Routes.RESULTS) { inclusive = true }
              }
            },
            onReturnHome = {
              val activity = context.findActivity()
              interstitialManager.showInterstitialIfReady(activity) {
                navController.navigate(Routes.HOME) {
                  popUpTo(Routes.HOME) { inclusive = true }
                }
              }
            },
            onChooseCategory = {
              navController.navigate(Routes.CATEGORIES) {
                popUpTo(Routes.RESULTS) { inclusive = true }
              }
            }
          )
        }
      }

      // 7. Daily Reward Screen
      composable(Routes.DAILY_REWARD) {
        DailyRewardScreen(
          profile = profile,
          language = language,
          canClaim = mainViewModel.playerRepository.canClaimDailyReward(profile),
          onClaim = { mainViewModel.claimDailyReward() },
          onBack = { navController.popBackStack() }
        )
      }

      // 8. Profile & Statistics Screen
      composable(Routes.PROFILE_STATS) {
        val displayAchievements = mainViewModel.getDisplayAchievements(unlockedAchievements)
        ProfileAndStatsScreen(
          profile = profile,
          achievements = displayAchievements,
          language = language,
          onBack = { navController.popBackStack() }
        )
      }

      // 9. Settings Screen
      composable(Routes.SETTINGS) {
        SettingsScreen(
          currentLanguage = language,
          soundEnabled = soundEnabled,
          musicEnabled = musicEnabled,
          vibrationEnabled = vibrationEnabled,
          soundVolume = soundVolume,
          isDarkMode = isDarkMode,
          notificationsEnabled = notificationsEnabled,
          showInAppBanner = showInAppBanner,
          onLanguageChange = { mainViewModel.setLanguage(it) },
          onSoundToggle = { mainViewModel.toggleSound(it) },
          onMusicToggle = { mainViewModel.toggleMusic(it) },
          onVibrationToggle = { mainViewModel.toggleVibration(it) },
          onSoundVolumeChange = { mainViewModel.setSoundVolume(it) },
          onDarkModeToggle = { mainViewModel.setDarkMode(it) },
          onNotificationsToggle = { mainViewModel.toggleNotifications(it) },
          onInAppBannerToggle = { mainViewModel.toggleInAppBanner(it) },
          onSendTestNotification = { mainViewModel.sendTestNotification() },
          onResetProgress = { mainViewModel.resetAllProgress() },
          onBack = { navController.popBackStack() }
        )
      }

      // 10. Prayer Times and Adhan Screen
      composable(Routes.PRAYER_TIMES) {
        PrayerTimesScreen(
          viewModel = prayerTimesViewModel,
          onBack = { navController.popBackStack() }
        )
      }
    }
  }
}
}

private fun Context.findActivity(): Activity? {
  var current = this
  while (current is ContextWrapper) {
    if (current is Activity) return current
    current = current.baseContext
  }
  return null
}
