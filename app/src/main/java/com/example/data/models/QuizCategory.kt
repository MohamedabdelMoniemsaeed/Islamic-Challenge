package com.example.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

enum class QuizCategory(
  val id: String,
  val titleEn: String,
  val titleAr: String,
  val descEn: String,
  val descAr: String,
  val icon: ImageVector
) {
  QURAN(
    id = "quran",
    titleEn = "Holy Quran",
    titleAr = "القرآن الكريم",
    descEn = "Surahs, Ayahs, revelation & facts",
    descAr = "السور والآيات والمكي والمدني",
    icon = Icons.Filled.AutoStories
  ),
  SEERAH(
    id = "seerah",
    titleEn = "Prophetic Seerah",
    titleAr = "السيرة النبوية",
    descEn = "Life of Prophet Muhammad ﷺ & companions",
    descAr = "سيرة المصطفى ﷺ وأصحابه الكرام",
    icon = Icons.Filled.HistoryEdu
  ),
  PROPHETS(
    id = "prophets",
    titleEn = "Stories of Prophets",
    titleAr = "قصص الأنبياء",
    descEn = "Prophets mentioned in Quran & their miracles",
    descAr = "أنبياء الله في القرآن ومعجزاتهم",
    icon = Icons.Filled.Bookmark
  ),
  WORSHIP(
    id = "worship",
    titleEn = "Worship & Fiqh",
    titleAr = "العبادات والفقه",
    descEn = "Salah, Wudu, Fasting, Zakat & Hajj",
    descAr = "الصلاة والوضوء والزكاة والحج",
    icon = Icons.Filled.Mosque
  ),
  RAMADAN(
    id = "ramadan",
    titleEn = "Ramadan & Fasting",
    titleAr = "رمضان المبارك",
    descEn = "Fasting, Suhoor, Iftar & Laylat al-Qadr",
    descAr = "الصيام والسحور والإفطار وليلة القدر",
    icon = Icons.Filled.NightsStay
  ),
  ADHKAR(
    id = "adhkar",
    titleEn = "Adhkar & Duas",
    titleAr = "الأذكار والأدعية",
    descEn = "Morning, evening & situational supplications",
    descAr = "أذكار الصباح والمساء والأدعية المأثورة",
    icon = Icons.Filled.Favorite
  ),
  MANNERS(
    id = "manners",
    titleEn = "Islamic Manners",
    titleAr = "الآداب والأخلاق",
    descEn = "Honesty, parents, charity & kind conduct",
    descAr = "بر الوالدين وحسن الجوار والأمانة",
    icon = Icons.Filled.CardGiftcard
  ),
  GENERAL(
    id = "general",
    titleEn = "General Knowledge",
    titleAr = "معارف إسلامية عامة",
    descEn = "Islamic history, geography & landmarks",
    descAr = "التاريخ والمعالم والحضارة الإسلامية",
    icon = Icons.Filled.Star
  )
}
