package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.data.models.QuizCategory
import com.example.ui.theme.EmeraldPrimary

@Composable
fun CategoriesSection(
  isArabic: Boolean,
  strings: AppStrings,
  onCategorySelected: (QuizCategory) -> Unit,
  onViewAllCategories: () -> Unit,
  modifier: Modifier = Modifier
) {
  val allCategories = QuizCategory.values().toList()

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (isArabic) "📚 المجالات القرآنية والإسلامية" else "📚 Islamic Subjects",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Bold
      )

      TextButton(
        onClick = onViewAllCategories,
        modifier = Modifier.testTag("view_all_categories_btn")
      ) {
        Text(
          text = if (isArabic) "عرض الكل" else "View All",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = EmeraldPrimary
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // 2-column grid of categories (4 rows of 2)
    val pairs = allCategories.chunked(2)
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      for (pair in pairs) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          for (category in pair) {
            CategoryHomeTile(
              category = category,
              isArabic = isArabic,
              onClick = { onCategorySelected(category) },
              modifier = Modifier.weight(1f)
            )
          }
          if (pair.size == 1) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }
  }
}

@Composable
private fun CategoryHomeTile(
  category: QuizCategory,
  isArabic: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
    modifier = modifier
      .height(82.dp)
      .clip(RoundedCornerShape(16.dp))
      .clickable { onClick() }
      .testTag("home_category_${category.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(EmeraldPrimary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = category.icon,
          contentDescription = null,
          tint = EmeraldPrimary,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (isArabic) category.titleAr else category.titleEn,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Text(
            text = if (isArabic) "١٠ أسئلة" else "10 questions",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }
    }
  }
}
