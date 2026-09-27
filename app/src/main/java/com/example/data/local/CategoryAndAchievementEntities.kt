package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_progress")
data class CategoryProgressEntity(
  @PrimaryKey val categoryId: String,
  val questionsAnswered: Int = 0,
  val correctCount: Int = 0,
  val bestScore: Int = 0,
  val isCompleted: Boolean = false
)

@Entity(tableName = "unlocked_achievements")
data class AchievementEntity(
  @PrimaryKey val achievementId: String,
  val unlockedTimestamp: Long = System.currentTimeMillis()
)
