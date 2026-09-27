package com.example.data.models

data class Question(
  val id: String,
  val questionEn: String,
  val questionAr: String,
  val optionsEn: List<String>,
  val optionsAr: List<String>,
  val correctAnswerIndex: Int,
  val explanationEn: String,
  val explanationAr: String,
  val category: QuizCategory,
  val difficulty: QuestionDifficulty = QuestionDifficulty.MEDIUM,
  val source: String,
  val hintEn: String = "",
  val hintAr: String = "",
  val xpReward: Int = difficulty.xpMultiplier,
  val coinReward: Int = difficulty.coinReward
) {
  fun getQuestion(isArabic: Boolean): String = if (isArabic) questionAr else questionEn
  fun getOptions(isArabic: Boolean): List<String> = if (isArabic) optionsAr else optionsEn
  fun getExplanation(isArabic: Boolean): String = if (isArabic) explanationAr else explanationEn
  fun getHint(isArabic: Boolean): String = if (isArabic) hintAr else hintEn
}
