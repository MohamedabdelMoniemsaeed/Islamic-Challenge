package com.example.core.adhkar

enum class AdhkarCategory(val titleAr: String, val titleEn: String) {
  MORNING("أذكار الصباح", "Morning Adhkar"),
  EVENING("أذكار المساء", "Evening Adhkar"),
  AFTER_PRAYER("بعد الصلاة", "Post-Prayer")
}

data class DhikrItem(
  val id: String,
  val category: AdhkarCategory,
  val textAr: String,
  val translationEn: String,
  val transliterationEn: String,
  val targetCount: Int,
  val referenceAr: String,
  val referenceEn: String
)

data class TasbihPhrase(
  val id: String,
  val textAr: String,
  val transliterationEn: String,
  val meaningEn: String
)

object AdhkarRepository {

  val TASBIH_PHRASES = listOf(
    TasbihPhrase("subhanallah", "سُبْحَانَ اللَّهِ", "SubhanAllah", "Glory be to Allah"),
    TasbihPhrase("alhamdulillah", "الْحَمْدُ لِلَّهِ", "Alhamdulillah", "All praise is due to Allah"),
    TasbihPhrase("allahuakbar", "اللَّهُ أَكْبَرُ", "Allahu Akbar", "Allah is the Greatest"),
    TasbihPhrase("lailaha", "لَا إِلَٰهَ إِلَّا اللَّهُ", "La ilaha illa Allah", "There is no deity worthy of worship except Allah"),
    TasbihPhrase("astaghfirullah", "أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ", "Astaghfirullah Al-Azim", "I seek forgiveness from Allah, the Almighty"),
    TasbihPhrase("salawat", "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ", "Allahumma salli 'ala Muhammad", "O Allah, send peace & blessings upon Prophet Muhammad ﷺ")
  )

  val ALL_ADHKAR = listOf(
    // Morning
    DhikrItem(
      id = "m_sayyid",
      category = AdhkarCategory.MORNING,
      textAr = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَىٰ عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ.",
      transliterationEn = "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduka...",
      translationEn = "O Allah, You are my Lord, none has the right to be worshipped except You. You created me and I am Your servant... forgive me, for none forgives sins except You.",
      targetCount = 1,
      referenceAr = "سيد الاستغفار — رواه البخاري",
      referenceEn = "Master Supplication for Forgiveness — Sahih Al-Bukhari"
    ),
    DhikrItem(
      id = "m_asbahna",
      category = AdhkarCategory.MORNING,
      textAr = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ.",
      transliterationEn = "Asbahna wa asbahal-mulku lillah, walhamdu lillah...",
      translationEn = "We have reached the morning and at this very time all sovereignty belongs to Allah, and all praise is for Allah. None has the right to be worshipped except Allah alone.",
      targetCount = 1,
      referenceAr = "رواه مسلم",
      referenceEn = "Sahih Muslim"
    ),
    DhikrItem(
      id = "m_bismillah",
      category = AdhkarCategory.MORNING,
      textAr = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.",
      transliterationEn = "Bismillahil-ladhi la yadurru ma'as-mihi shay'un fil-ardi wa la fis-sama'i wa Huwas-Sami'ul-'Alim.",
      translationEn = "In the Name of Allah, with whose Name nothing on earth or in heaven can cause harm, and He is the All-Hearing, the All-Knowing.",
      targetCount = 3,
      referenceAr = "رواه أبو داود والترمذي",
      referenceEn = "Abu Dawud & At-Tirmidhi"
    ),
    DhikrItem(
      id = "m_raditu",
      category = AdhkarCategory.MORNING,
      textAr = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ ﷺ نَبِيًّا.",
      transliterationEn = "Raditu billahi Rabban, wa bil-Islami dinan, wa bi-Muhammadin ﷺ Nabiyyan.",
      translationEn = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad ﷺ as my Prophet.",
      targetCount = 3,
      referenceAr = "رواه أبو داود وأحمد",
      referenceEn = "Abu Dawud & Ahmad"
    ),
    DhikrItem(
      id = "m_subhan",
      category = AdhkarCategory.MORNING,
      textAr = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ.",
      transliterationEn = "SubhanAllahi wa bihamdihi, 'adada khalqihi, wa rida nafsihi, wa zinata 'arshihi, wa midada kalimatihi.",
      translationEn = "Glory and praise be to Allah, as numerous as His creation, as much as pleases Him, as weighty as His Throne, and as ink for His words.",
      targetCount = 3,
      referenceAr = "رواه مسلم",
      referenceEn = "Sahih Muslim"
    ),
    DhikrItem(
      id = "m_hasbi",
      category = AdhkarCategory.MORNING,
      textAr = "حَسْبِيَ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ، عَلَيْهِ تَوَكَّلْتُ، وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ.",
      transliterationEn = "HasbiyAllahu la ilaha illa Huwa, 'alayhi tawakkaltu, wa Huwa Rabbul-'Arshil-'Azim.",
      translationEn = "Allah is sufficient for me; none has the right to be worshipped except Him. Upon Him I rely, and He is the Lord of the Mighty Throne.",
      targetCount = 7,
      referenceAr = "سورة التوبة (١٢٩) — أبو داود",
      referenceEn = "Surah At-Tawbah (9:129) — Abu Dawud"
    ),

    // Evening
    DhikrItem(
      id = "e_amsayna",
      category = AdhkarCategory.EVENING,
      textAr = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ.",
      transliterationEn = "Amsayna wa amsal-mulku lillah, walhamdu lillah...",
      translationEn = "We have reached the evening and at this very time all sovereignty belongs to Allah, and all praise is for Allah. None has the right to be worshipped except Allah alone.",
      targetCount = 1,
      referenceAr = "رواه مسلم",
      referenceEn = "Sahih Muslim"
    ),
    DhikrItem(
      id = "e_kalimat",
      category = AdhkarCategory.EVENING,
      textAr = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ.",
      transliterationEn = "A'udhu bi-kalimatillahit-tammati min sharri ma khalaq.",
      translationEn = "I seek refuge in the perfect words of Allah from the evil of what He has created.",
      targetCount = 3,
      referenceAr = "رواه مسلم",
      referenceEn = "Sahih Muslim"
    ),
    DhikrItem(
      id = "e_bismillah",
      category = AdhkarCategory.EVENING,
      textAr = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.",
      transliterationEn = "Bismillahil-ladhi la yadurru ma'as-mihi shay'un fil-ardi wa la fis-sama'i...",
      translationEn = "In the Name of Allah, with whose Name nothing on earth or in heaven can cause harm, and He is the All-Hearing, the All-Knowing.",
      targetCount = 3,
      referenceAr = "رواه أبو داود والترمذي",
      referenceEn = "Abu Dawud & At-Tirmidhi"
    ),
    DhikrItem(
      id = "e_hayyu",
      category = AdhkarCategory.EVENING,
      textAr = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ وَلَا تَكِلْنِي إِلَىٰ نَفْسِي طَرْفَةَ عَيْنٍ.",
      transliterationEn = "Ya Hayyu ya Qayyumu birahmatika astaghith, aslih li sha'ni kullahu wa la takilni ila nafsi tarfata 'ayn.",
      translationEn = "O Ever-Living, O Sustainer of all, in Your mercy I seek relief; rectify all of my affairs and do not leave me to myself even for the blink of an eye.",
      targetCount = 1,
      referenceAr = "رواه النسائي والحاكم",
      referenceEn = "An-Nasa'i & Al-Hakim"
    ),
    DhikrItem(
      id = "e_tasbih100",
      category = AdhkarCategory.EVENING,
      textAr = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ.",
      transliterationEn = "SubhanAllahi wa bihamdihi.",
      translationEn = "Glory be to Allah and praise Him.",
      targetCount = 100,
      referenceAr = "متفق عليه",
      referenceEn = "Al-Bukhari & Muslim"
    ),

    // Post-Prayer
    DhikrItem(
      id = "p_istighfar",
      category = AdhkarCategory.AFTER_PRAYER,
      textAr = "أَسْتَغْفِرُ اللَّهَ (ثلاثاً)، اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ.",
      transliterationEn = "Astaghfirullah (3x). Allahumma Antas-Salam wa minkas-salam, tabarakta ya Dhal-Jalali wal-Ikram.",
      translationEn = "I seek forgiveness from Allah (3x). O Allah, You are Peace and from You comes peace; Blessed are You, O Possessor of Majesty and Honor.",
      targetCount = 1,
      referenceAr = "رواه مسلم",
      referenceEn = "Sahih Muslim"
    ),
    DhikrItem(
      id = "p_subhan",
      category = AdhkarCategory.AFTER_PRAYER,
      textAr = "سُبْحَانَ اللَّهِ.",
      transliterationEn = "SubhanAllah.",
      translationEn = "Glory be to Allah.",
      targetCount = 33,
      referenceAr = "رواه مسلم",
      referenceEn = "Sahih Muslim"
    ),
    DhikrItem(
      id = "p_hamd",
      category = AdhkarCategory.AFTER_PRAYER,
      textAr = "الْحَمْدُ لِلَّهِ.",
      transliterationEn = "Alhamdulillah.",
      translationEn = "All praise belongs to Allah.",
      targetCount = 33,
      referenceAr = "رواه مسلم",
      referenceEn = "Sahih Muslim"
    ),
    DhikrItem(
      id = "p_akbar",
      category = AdhkarCategory.AFTER_PRAYER,
      textAr = "اللَّهُ أَكْبَرُ.",
      transliterationEn = "Allahu Akbar.",
      translationEn = "Allah is the Greatest.",
      targetCount = 33,
      referenceAr = "رواه مسلم",
      referenceEn = "Sahih Muslim"
    ),
    DhikrItem(
      id = "p_tamam100",
      category = AdhkarCategory.AFTER_PRAYER,
      textAr = "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ.",
      transliterationEn = "La ilaha illAllahu wahdahu la sharika lah, lahul-mulku wa lahul-hamdu wa Huwa 'ala kulli shay'in Qadir.",
      translationEn = "None has the right to be worshipped except Allah alone, without partner; to Him belongs all sovereignty and praise, and He is over all things Omnipotent.",
      targetCount = 1,
      referenceAr = "تمام المائة بعد الصلاة — رواه مسلم",
      referenceEn = "Completing 100 after Salah — Sahih Muslim"
    )
  )
}
