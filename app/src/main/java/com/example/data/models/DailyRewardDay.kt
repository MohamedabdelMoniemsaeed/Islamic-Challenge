package com.example.data.models

data class DailyRewardDay(
  val dayNumber: Int,
  val titleEn: String,
  val titleAr: String,
  val rewardTextEn: String,
  val rewardTextAr: String,
  val coinAmount: Int,
  val xpAmount: Int,
  val lifelineBonusType: LifelineType? = null,
  val iconEmoji: String
)

val defaultDailyRewards = listOf(
  DailyRewardDay(1, "Day 1", "اليوم الأول", "+25 Coins", "+٢٥ قطعة ذهبية", 25, 20, null, "🪙"),
  DailyRewardDay(2, "Day 2", "اليوم الثاني", "+40 Coins", "+٤٠ قطعة ذهبية", 40, 30, null, "🪙"),
  DailyRewardDay(3, "Day 3", "اليوم الثالث", "+1 50/50 Lifeline", "+١ وسيلة ٥٠/٥٠", 30, 40, LifelineType.FIFTY_FIFTY, "✨"),
  DailyRewardDay(4, "Day 4", "اليوم الرابع", "+60 Coins", "+٦٠ قطعة ذهبية", 60, 50, null, "🪙"),
  DailyRewardDay(5, "Day 5", "اليوم الخامس", "+120 XP Boost", "+١٢٠ نقطة خبرة", 50, 120, null, "⚡"),
  DailyRewardDay(6, "Day 6", "اليوم السادس", "+100 Coins & +1 Hint", "+١٠٠ قطعة وتلميح", 100, 60, LifelineType.HINT, "🎁"),
  DailyRewardDay(7, "Day 7", "اليوم السابع", "Star Chest: +200 Coins & +200 XP", "صندوق التميز: ٢٠٠ قطعة و٢٠٠ خبرة", 200, 200, LifelineType.EXTRA_TIME, "🏆")
)
