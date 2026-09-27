package com.example.data.models

object AchievementsProvider {
  val allAchievements: List<Achievement> = listOf(
    Achievement(
      id = "ach_first_challenge",
      titleEn = "First Step in Knowledge",
      titleAr = "الخطوة الأولى في العلم",
      descEn = "Complete your first Islamic challenge.",
      descAr = "أكملت أول تحدٍ إسلامي بنجاح.",
      iconEmoji = "🏆",
      targetValue = 1,
      xpReward = 50,
      coinReward = 30
    ),
    Achievement(
      id = "ach_streak_3",
      titleEn = "3-Day Steadfastness",
      titleAr = "حماس الأيام الثلاثة",
      descEn = "Play for three consecutive days.",
      descAr = "ثابرت على التحدي ٣ أيام متتالية.",
      iconEmoji = "🔥",
      targetValue = 3,
      xpReward = 80,
      coinReward = 50
    ),
    Achievement(
      id = "ach_streak_7",
      titleEn = "Weekly Perseverance",
      titleAr = "مثابرة الأسبوع المبارك",
      descEn = "Maintain a 7-day knowledge streak.",
      descAr = "حافظت على التعلم لمدة ٧ أيام متواصلة.",
      iconEmoji = "🌟",
      targetValue = 7,
      xpReward = 150,
      coinReward = 100
    ),
    Achievement(
      id = "ach_perfect_10",
      titleEn = "Flawless Round",
      titleAr = "إتقان بلا خطأ",
      descEn = "Answer all 10 questions correctly in a single challenge.",
      descAr = "أجبت على جميع الأسئلة العشرة بشكل صحيح.",
      iconEmoji = "💯",
      targetValue = 10,
      xpReward = 120,
      coinReward = 80
    ),
    Achievement(
      id = "ach_questions_50",
      titleEn = "Diligent Student",
      titleAr = "طالب مجتهد",
      descEn = "Answer 50 total questions.",
      descAr = "أجبت عن ٥٠ سؤالاً في مختلف الأبواب.",
      iconEmoji = "📖",
      targetValue = 50,
      xpReward = 100,
      coinReward = 60
    ),
    Achievement(
      id = "ach_knowledge_seeker",
      titleEn = "Seeker of Wisdom",
      titleAr = "باحث عن الحكمة",
      descEn = "Answer 100 total questions.",
      descAr = "أجبت عن ١٠٠ سؤال في شتى العلوم والآداب.",
      iconEmoji = "🧠",
      targetValue = 100,
      xpReward = 200,
      coinReward = 150
    ),
    Achievement(
      id = "ach_quran_explorer",
      titleEn = "Quran Explorer",
      titleAr = "مستكشف آيات الذكر",
      descEn = "Master questions from the Holy Quran category.",
      descAr = "أتقنت أسئلة سورة وآيات القرآن الكريم.",
      iconEmoji = "🕌",
      targetValue = 10,
      xpReward = 100,
      coinReward = 75
    ),
    Achievement(
      id = "ach_seerah_student",
      titleEn = "Seerah Companion",
      titleAr = "محب السيرة النبوية",
      descEn = "Learn about the life of Prophet Muhammad ﷺ.",
      descAr = "تعرّفت على سيرة الحبيب المصطفى ﷺ وأصحابه.",
      iconEmoji = "🕋",
      targetValue = 10,
      xpReward = 100,
      coinReward = 75
    )
  )
}
