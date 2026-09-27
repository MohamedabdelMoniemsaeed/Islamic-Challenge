package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalizationManager
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.IslamicGeometricBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  language: AppLanguage,
  onSplashFinished: () -> Unit
) {
  val strings = LocalizationManager.get(language)
  val scaleAnim = remember { Animatable(0.8f) }
  val pulseAnim = remember { Animatable(1f) }

  LaunchedEffect(Unit) {
    scaleAnim.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
    )
    pulseAnim.animateTo(
      targetValue = 1.06f,
      animationSpec = infiniteRepeatable(
        animation = tween(1200),
        repeatMode = RepeatMode.Reverse
      )
    )
  }

  LaunchedEffect(Unit) {
    delay(2200L)
    onSplashFinished()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(EmeraldDeep, EmeraldDark, Color(0xFF021B13))
        )
      ),
    contentAlignment = Alignment.Center
  ) {
    IslamicGeometricBackground(
      patternColor = GoldAccent.copy(alpha = 0.12f),
      strokeWidth = 1.2f
    )

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .scale(scaleAnim.value)
        .padding(32.dp)
    ) {
      Text(
        text = strings.bismillah,
        color = GoldLight,
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 28.dp)
      )

      Box(
        modifier = Modifier
          .size(140.dp)
          .scale(pulseAnim.value)
          .clip(CircleShape)
          .background(GoldAccent.copy(alpha = 0.15f))
          .padding(8.dp),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.ic_app_foreground_img),
          contentDescription = "Islamic Challenge Emblem",
          modifier = Modifier
            .size(110.dp)
            .clip(CircleShape)
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      Text(
        text = strings.appTitle,
        color = Color.White,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = strings.appSubtitle,
        color = GoldLight.copy(alpha = 0.85f),
        fontSize = 15.sp,
        textAlign = TextAlign.Center
      )
    }
  }
}
