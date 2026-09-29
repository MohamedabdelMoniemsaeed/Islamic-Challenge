package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalizationManager
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.IslamicGeometricBackground
import kotlinx.coroutines.launch

data class OnboardingPageData(
  val title: String,
  val desc: String,
  val icon: ImageVector
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
  language: AppLanguage,
  onComplete: () -> Unit
) {
  val strings = LocalizationManager.get(language)
  val pages = listOf(
    OnboardingPageData(
      title = strings.onbTitle1,
      desc = strings.onbDesc1,
      icon = Icons.AutoMirrored.Filled.MenuBook
    ),
    OnboardingPageData(
      title = strings.onbTitle2,
      desc = strings.onbDesc2,
      icon = Icons.Filled.EmojiEvents
    ),
    OnboardingPageData(
      title = strings.onbTitle3,
      desc = strings.onbDesc3,
      icon = Icons.Filled.AutoAwesome
    )
  )

  val pagerState = rememberPagerState(pageCount = { pages.size })
  val scope = rememberCoroutineScope()

  val view = LocalView.current
  if (!view.isInEditMode) {
    DisposableEffect(Unit) {
      val window = (view.context as? Activity)?.window
      val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
      val originalLightStatus = insetsController?.isAppearanceLightStatusBars
      val originalLightNav = insetsController?.isAppearanceLightNavigationBars
      insetsController?.isAppearanceLightStatusBars = false
      insetsController?.isAppearanceLightNavigationBars = false
      onDispose {
        originalLightStatus?.let { insetsController.isAppearanceLightStatusBars = it }
        originalLightNav?.let { insetsController.isAppearanceLightNavigationBars = it }
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(EmeraldDark, EmeraldPrimary, Color(0xFF003828))
        )
      )
  ) {
    IslamicGeometricBackground(
      patternColor = GoldAccent.copy(alpha = 0.09f),
      strokeWidth = 1f
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top row: Skip button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        if (pagerState.currentPage < pages.size - 1) {
          TextButton(
            onClick = onComplete,
            modifier = Modifier.testTag("skip_onboarding_btn")
          ) {
            Text(
              text = strings.skip,
              color = Color.White.copy(alpha = 0.8f),
              fontWeight = FontWeight.Medium
            )
          }
        } else {
          Spacer(modifier = Modifier.height(48.dp))
        }
      }

      // Middle: Pager
      HorizontalPager(
        state = pagerState,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) { pageIdx ->
        val page = pages[pageIdx]
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
        ) {
          Box(
            modifier = Modifier
              .size(120.dp)
              .clip(CircleShape)
              .background(GoldAccent.copy(alpha = 0.2f))
              .padding(16.dp),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = page.icon,
              contentDescription = null,
              tint = GoldAccent,
              modifier = Modifier.size(64.dp)
            )
          }

          Spacer(modifier = Modifier.height(36.dp))

          Text(
            text = page.title,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = page.desc,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
          )
        }
      }

      // Bottom: Indicators and Next / Start button
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
      ) {
        // Dot indicators
        Row(
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(bottom = 28.dp)
        ) {
          repeat(pages.size) { i ->
            val isSelected = pagerState.currentPage == i
            Box(
              modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(if (isSelected) 10.dp else 8.dp)
                .clip(CircleShape)
                .background(if (isSelected) GoldAccent else Color.White.copy(alpha = 0.35f))
            )
          }
        }

        Button(
          onClick = {
            if (pagerState.currentPage < pages.size - 1) {
              scope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
              }
            } else {
              onComplete()
            }
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = GoldAccent,
            contentColor = EmeraldDark
          ),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("onboarding_action_btn")
        ) {
          Text(
            text = if (pagerState.currentPage == pages.size - 1) strings.getStarted else strings.nextQuestion,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
