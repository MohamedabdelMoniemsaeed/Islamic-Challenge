package com.example.data.models

enum class QuestionDifficulty(
  val labelEn: String,
  val labelAr: String,
  val xpMultiplier: Int,
  val coinReward: Int
) {
  EASY("Easy", "سهل", 10, 5),
  MEDIUM("Medium", "متوسط", 20, 10),
  HARD("Hard", "متقدم", 35, 15),
  EXPERT("Expert", "خبير", 50, 25)
}
