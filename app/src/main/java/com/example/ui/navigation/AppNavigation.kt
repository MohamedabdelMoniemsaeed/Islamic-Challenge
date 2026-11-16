package com.example.ui.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.ads.AdMobInterstitialManager
import com.example.core.localization.AppLanguage
import com.example.data.models.GameMode
import com.example.ui.components.AdMobBannerBar
import com.example.ui.screens.AdhkarTasbihScreen
import com.example.ui.screens.CategoryScreen
import com.example.ui.screens.DailyRewardScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
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
  const val ADHKAR_TASBIH = "adhkar_tasbih"
}

private data class BottomNavTab(
  val route: String,
  val labelAr: String,
  val labelEn: String,
  val icon: ImageVector
)

@Composable
fun AppNavigation(
  mainViewModel: MainViewModel,
  navController: NavHostController = rememberNavController(),
  pendingDeepLinkRoute: String? = null,
  onConsumeDeepLinkRoute: () -> Unit = {}
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
  val hourlySalawatEnabled by mainViewModel.hourlySalawatEnabled.collectAsState()

  val quizViewModel = remember {
    QuizViewModel(
      questionRepository = mainViewModel.questionRepository,
      playerRepository = mainViewModel.playerRepository,
      audioManager = mainViewModel.audioManager
    )
  }

  val context = LocalContext.current
  val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
  androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
      when (event) {
        androidx.lifecycle.Lifecycle.Event.ON_STOP -> mainViewModel.audioManager.onAppBackground()
        androidx.lifecycle.Lifecycle.Event.ON_START -> mainViewModel.audioManager.onAppForeground()
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  val interstitialManager = remember { AdMobInterstitialManager(context.applicationContext) }
  val prayerTimesViewModel: PrayerTimesViewModel = viewModel()
  val prayerUiState by prayerTimesViewModel.uiState.collectAsState()
  val isAr = language == AppLanguage.ARABIC

  LaunchedEffect(language) {
    prayerTimesViewModel.refreshTimes(isArabic = language == AppLanguage.ARABIC)
  }

  val layoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr
  val currentBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = currentBackStackEntry?.destination?.route

  val allowedDeepLinkRoutes = remember {
    setOf(
      Routes.HOME,
      Routes.CATEGORIES,
      Routes.PRAYER_TIMES,
      Routes.ADHKAR_TASBIH,
      Routes.PROFILE_STATS,
      Routes.SETTINGS,
      Routes.DAILY_REWARD
    )
  }

  LaunchedEffect(pendingDeepLinkRoute, currentRoute) {
    val target = pendingDeepLinkRoute
    if (!target.isNullOrBlank() &&
      target in allowedDeepLinkRoutes &&
      currentRoute != null &&
      currentRoute != Routes.SPLASH &&
      currentRoute != Routes.ONBOARDING
    ) {
      if (currentRoute != target) {
        navController.navigate(target) {
          popUpTo(Routes.HOME) { saveState = true }
          launchSingleTop = true
          restoreState = true
        }
      }
      onConsumeDeepLinkRoute()
    }
  }

  val bottomTabs = remember {
    listOf(
      BottomNavTab(Routes.HOME, "الرئيسية", "Home", Icons.Filled.Home),
      BottomNavTab(Routes.CATEGORIES, "الأقسام", "Topics", Icons.Filled.Category),
      BottomNavTab(Routes.PRAYER_TIMES, "الصلاة", "Prayer", Icons.Filled.AccessTime),
      BottomNavTab(Routes.ADHKAR_TASBIH, "الأذكار", "Adhkar", Icons.Filled.AutoAwesome),
      BottomNavTab(Routes.PROFILE_STATS, "ملفي", "Profile", Icons.Filled.Person)
    )
  }

  val showBottomNavBar = currentRoute in bottomTabs.map { it.route }
  val showBottomAdBar = currentRoute != null &&
    currentRoute != Routes.SPLASH &&
    currentRoute != Routes.ONBOARDING

  CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
    Scaffold(
      bottomBar = {
        if (showBottomAdBar || showBottomNavBar) {
          Column {
            if (showBottomAdBar) {
              AdMobBannerBar(includeNavigationBarsPadding = !showBottomNavBar)
            }
            if (showBottomNavBar) {
              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
              NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars
              ) {
                bottomTabs.forEach { tab ->
                  val selected = currentRoute == tab.route
                  NavigationBarItem(
                    selected = selected,
                    onClick = {
                      if (!selected) {
                        navController.navigate(tab.route) {
                          popUpTo(Routes.HOME) { saveState = true }
                          launchSingleTop = true
                          restoreState = true
                        }
                      }
                    },
                    icon = {
                      Icon(
                        imageVector = tab.icon,
                        contentDescription = if (isAr) tab.labelAr else tab.labelEn
                      )
                    },
                    label = {
                      Text(
                        text = if (isAr) tab.labelAr else tab.labelEn,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }
                  )
                }
              }
            }
          }
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
        enterTransition = { fadeIn(animationSpec = tween(250)) },
        exitTransition = { fadeOut(animationSpec = tween(250)) }
      ) {
        // 1. Splash Screen
        composable(Routes.SPLASH) {
          SplashScreen(
            language = language,
            onSplashFinished = {
              if (profile.hasCompletedOnboarding) {
                val target = pendingDeepLinkRoute?.takeIf { it in allowedDeepLinkRoutes }
                navController.navigate(Routes.HOME) {
                  popUpTo(Routes.SPLASH) { inclusive = true }
                }
                if (target != null && target != Routes.HOME) {
                  navController.navigate(target) {
                    launchSingleTop = true
                  }
                }
                if (pendingDeepLinkRoute != null) {
                  onConsumeDeepLinkRoute()
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

          HomeScreen(
            profile = profile,
            language = language,
            canClaimReward = canClaim,
            showInAppBanner = showInAppBanner,
            isDailyChallengeCompleted = mainViewModel.playerRepository.isDailyChallengeCompletedToday(profile),
            unlockedAchievements = unlockedAchievements,
            nextPrayer = prayerUiState.nextPrayer,
            prayerCountdown = prayerUiState.countdownString,
            hijriDate = prayerUiState.hijriDateString,
            cityName = if (isAr) prayerUiState.selectedCity.nameAr else prayerUiState.selectedCity.nameEn,
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
            onNavigateToAdhkarTasbih = { navController.navigate(Routes.ADHKAR_TASBIH) },
            onPurchaseLifeline = { mainViewModel.purchaseLifeline(it) },
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

        // 6. Results Screen (no duplicate interstitial on return home)
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
                navController.navigate(Routes.HOME) {
                  popUpTo(Routes.HOME) { inclusive = true }
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
            categoryProgress = categoryProgress,
            onPurchaseLifeline = { mainViewModel.purchaseLifeline(it) },
            onBack = if (navController.previousBackStackEntry != null) {
              { navController.popBackStack() }
            } else {
              null
            }
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
            hourlySalawatEnabled = hourlySalawatEnabled,
            currentLocationLabel = if (isAr) prayerUiState.selectedCity.nameAr else prayerUiState.selectedCity.nameEn,
            isDetectingGps = prayerUiState.isDetectingGps,
            onDetectGpsLocation = { onMsg ->
              prayerTimesViewModel.detectLocationWithGps(isArabic = isAr, onResultMessage = onMsg)
            },
            onRefreshPermissions = { prayerTimesViewModel.refreshBackgroundStatus() },
            onLanguageChange = { mainViewModel.setLanguage(it) },
            onSoundToggle = { mainViewModel.toggleSound(it) },
            onMusicToggle = { mainViewModel.toggleMusic(it) },
            onVibrationToggle = { mainViewModel.toggleVibration(it) },
            onSoundVolumeChange = { mainViewModel.setSoundVolume(it) },
            onDarkModeToggle = { mainViewModel.setDarkMode(it) },
            onNotificationsToggle = { mainViewModel.toggleNotifications(it) },
            onInAppBannerToggle = { mainViewModel.toggleInAppBanner(it) },
            onHourlySalawatToggle = { mainViewModel.toggleHourlySalawat(it) },
            onSendTestNotification = { mainViewModel.sendTestNotification() },
            onSendTestSalawat = { mainViewModel.sendTestSalawatReminder() },
            onResetProgress = { mainViewModel.resetAllProgress() },
            onBack = { navController.popBackStack() }
          )
        }

        // 10. Prayer Times, Adhan & Qiblah Screen
        composable(Routes.PRAYER_TIMES) {
          PrayerTimesScreen(
            viewModel = prayerTimesViewModel,
            language = language,
            hourlySalawatEnabled = hourlySalawatEnabled,
            onHourlySalawatToggle = { mainViewModel.toggleHourlySalawat(it) },
            onTestSalawatReminder = { mainViewModel.sendTestSalawatReminder() },
            onBack = if (navController.previousBackStackEntry != null) {
              { navController.popBackStack() }
            } else {
              null
            }
          )
        }

        // 11. Daily Adhkar & Digital Tasbih Screen
        composable(Routes.ADHKAR_TASBIH) {
          AdhkarTasbihScreen(
            language = language,
            audioManager = mainViewModel.audioManager,
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled,
            onBack = if (navController.previousBackStackEntry != null) {
              { navController.popBackStack() }
            } else {
              null
            }
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
