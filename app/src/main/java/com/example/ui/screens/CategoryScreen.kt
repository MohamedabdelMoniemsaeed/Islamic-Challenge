package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalizationManager
import com.example.data.local.CategoryProgressEntity
import com.example.data.models.QuizCategory
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
  language: AppLanguage,
  progressMap: Map<String, CategoryProgressEntity>,
  onCategorySelected: (QuizCategory) -> Unit,
  onBack: () -> Unit
) {
  val strings = LocalizationManager.get(language)
  val isAr = language.isRtl

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = strings.chooseCategory,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("categories_back_btn")) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("category_list"),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(QuizCategory.entries) { cat ->
        val progress = progressMap[cat.id]
        val isCompleted = progress?.isCompleted == true

        CategoryItemCard(
          category = cat,
          isArabic = isAr,
          isCompleted = isCompleted,
          bestScore = progress?.bestScore ?: 0,
          onClick = { onCategorySelected(cat) }
        )
      }
    }
  }
}

@Composable
private fun CategoryItemCard(
  category: QuizCategory,
  isArabic: Boolean,
  isCompleted: Boolean,
  bestScore: Int,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("category_card_${category.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(EmeraldPrimary.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(28.dp)
          )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isArabic) category.titleAr else category.titleEn,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = if (isArabic) category.descAr else category.descEn,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2
          )
          if (bestScore > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (isArabic) "أعلى نتيجة: $bestScore" else "Best Score: $bestScore",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = EmeraldPrimary
            )
          }
        }
      }

      if (isCompleted) {
        Icon(
          imageVector = Icons.Filled.CheckCircle,
          contentDescription = "Completed",
          tint = EmeraldPrimary,
          modifier = Modifier
            .padding(start = 8.dp)
            .size(24.dp)
        )
      } else {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = EmeraldPrimary.copy(alpha = 0.1f),
          modifier = Modifier.padding(start = 8.dp)
        ) {
          Text(
            text = if (isArabic) "بدء" else "Play",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = EmeraldPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          )
        }
      }
    }
  }
}
