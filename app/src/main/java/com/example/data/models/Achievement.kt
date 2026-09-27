package com.example.data.models

data class Achievement(
  val id: String,
  val titleEn: String,
  val titleAr: String,
  val descEn: String,
  val descAr: String,
  val iconEmoji: String,
  val targetValue: Int,
  val currentValue: Int = 0,
  val isUnlocked: Boolean = false,
  val xpReward: Int = 50,
  val coinReward: Int = 30
)
