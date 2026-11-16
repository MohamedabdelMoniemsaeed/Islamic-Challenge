package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val CairoFontFamily = FontFamily(
  Font(R.font.cairo, FontWeight.Normal),
  Font(R.font.cairo, FontWeight.Medium),
  Font(R.font.cairo, FontWeight.SemiBold),
  Font(R.font.cairo, FontWeight.Bold),
  Font(R.font.cairo, FontWeight.ExtraBold)
)

val AmiriFontFamily = FontFamily(
  Font(R.font.amiri, FontWeight.Normal),
  Font(R.font.amiri, FontWeight.Bold)
)

val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = AmiriFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 36.sp,
    lineHeight = 44.sp
  ),
  headlineLarge = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 36.sp
  ),
  headlineMedium = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 30.sp
  ),
  headlineSmall = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 19.sp,
    lineHeight = 26.sp
  ),
  titleLarge = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 28.sp
  ),
  titleMedium = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    lineHeight = 24.sp
  ),
  titleSmall = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 21.sp
  ),
  bodySmall = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 18.sp
  ),
  labelLarge = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp
  ),
  labelMedium = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp
  ),
  labelSmall = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 15.sp
  )
)
