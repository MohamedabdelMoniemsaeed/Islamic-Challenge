package com.example.data.repository

import com.example.data.datasource.AdhkarQuestionData
import com.example.data.datasource.GeneralQuestionData
import com.example.data.datasource.MannersQuestionData
import com.example.data.datasource.ProphetsQuestionData
import com.example.data.datasource.QuranQuestionData
import com.example.data.datasource.RamadanQuestionData
import com.example.data.datasource.SeerahQuestionData
import com.example.data.datasource.WorshipQuestionData
import com.example.core.utils.DateUtils
import com.example.data.models.GameMode
import com.example.data.models.Question
import com.example.data.models.QuestionDifficulty
import com.example.data.models.QuizCategory

class QuestionRepository {

  private val initialQuestions: List<Question> = listOf(
    // 📖 QURAN CATEGORY
    Question(
      id = "q_qur_1",
      questionEn = "How many Surahs are there in the Holy Quran?",
      questionAr = "كم عدد سور القرآن الكريم؟",
      optionsEn = listOf("110", "114", "112", "120"),
      optionsAr = listOf("١١٠", "١١٤", "١١٢", "١٢٠"),
      correctAnswerIndex = 1,
      explanationEn = "The Holy Quran contains 114 Surahs, beginning with Surah Al-Fatihah and concluding with Surah An-Nas.",
      explanationAr = "يتألف القرآن الكريم من ١١٤ سورة، تبدأ بسورة الفاتحة وتختتم بسورة الناس.",
      category = QuizCategory.QURAN,
      difficulty = QuestionDifficulty.EASY,
      source = "Holy Quran Structure",
      hintEn = "It is between 110 and 115.",
      hintAr = "العدد يقع بين ١١٠ و١١٥."
    ),
    Question(
      id = "q_qur_2",
      questionEn = "What were the first verses of the Quran revealed to the Prophet Muhammad ﷺ?",
      questionAr = "ما هي أول آيات أُنزلت على النبي محمد ﷺ في غار حراء؟",
      optionsEn = listOf("Surah Al-Fatihah", "Surah Al-Baqarah", "First 5 verses of Surah Al-Alaq", "Surah Al-Ikhlas"),
      optionsAr = listOf("سورة الفاتحة", "سورة البقرة", "الآيات الخمس الأولى من سورة العلق", "سورة الإخلاص"),
      correctAnswerIndex = 2,
      explanationEn = "The first revelation began with: 'Read in the name of your Lord who created...' (Surah Al-Alaq 96:1-5).",
      explanationAr = "بدأ الوحي بقول الله تعالى: {اقْرَأْ بِاسْمِ رَبِّكَ الَّذِي خَلَقَ} من سورة العلق.",
      category = QuizCategory.QURAN,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari (4953)",
      hintEn = "It starts with the command 'Iqra' (Read).",
      hintAr = "تبدأ بالأمر الإلهي (اقْرَأْ)."
    ),
    Question(
      id = "q_qur_3",
      questionEn = "Which Surah is the longest in the Holy Quran?",
      questionAr = "ما هي أطول سورة في القرآن الكريم؟",
      optionsEn = listOf("Surah Al-Imran", "Surah Al-Baqarah", "Surah An-Nisa", "Surah Al-Ma'idah"),
      optionsAr = listOf("سورة آل عمران", "سورة البقرة", "سورة النساء", "سورة المائدة"),
      correctAnswerIndex = 1,
      explanationEn = "Surah Al-Baqarah is the longest Surah in the Quran with 286 verses.",
      explanationAr = "سورة البقرة هي أطول سورة في القرآن الكريم وتتكون من ٢٨٦ آية كريمة.",
      category = QuizCategory.QURAN,
      difficulty = QuestionDifficulty.EASY,
      source = "Holy Quran",
      hintEn = "Named after the cow mentioned in Prophet Musa's story.",
      hintAr = "سُميت تخليداً لقصة بقرة بني إسرائيل مع نبي الله موسى عليه السلام."
    ),
    Question(
      id = "q_qur_4",
      questionEn = "Which Surah in the Holy Quran does not begin with the Basmalah?",
      questionAr = "ما هي السورة الوحيدة في القرآن التي لا تبدأ بالبسملة؟",
      optionsEn = listOf("Surah At-Tawbah", "Surah Al-Anfal", "Surah Yunus", "Surah Al-Qalam"),
      optionsAr = listOf("سورة التوبة (براءة)", "سورة الأنفال", "سورة يونس", "سورة القلم"),
      correctAnswerIndex = 0,
      explanationEn = "Surah At-Tawbah (Bara'ah) is the only Surah in the Quran that does not begin with Bismillahir-Rahmanir-Rahim.",
      explanationAr = "سورة التوبة هي السورة الوحيدة التي خلت فاتحتها من البسملة.",
      category = QuizCategory.QURAN,
      difficulty = QuestionDifficulty.MEDIUM,
      source = "Ijma' of Quranic Scholars",
      hintEn = "Also known as Surah Bara'ah.",
      hintAr = "تُعرف أيضاً بسورة براءة."
    ),
    Question(
      id = "q_qur_5",
      questionEn = "Which Surah contains two Basmalahs (Bismillahir-Rahmanir-Rahim)?",
      questionAr = "ما هي السورة التي وردت فيها البسملة مرتين؟",
      optionsEn = listOf("Surah An-Nahl", "Surah An-Naml", "Surah Maryam", "Surah Saba"),
      optionsAr = listOf("سورة النحل", "سورة النمل", "سورة مريم", "سورة سبأ"),
      correctAnswerIndex = 1,
      explanationEn = "Surah An-Naml has the opening Basmalah and another at verse 30 in Prophet Sulaiman's letter: 'In the name of Allah, the Most Gracious, the Most Merciful'.",
      explanationAr = "وردت البسملة في سورة النمل في بدايتها وفي الآية ٣٠ في كتاب سليمان عليه السلام لبلقيس: {إِنَّهُ مِنْ سُلَيْمَانَ وَإِنَّهُ بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ}.",
      category = QuizCategory.QURAN,
      difficulty = QuestionDifficulty.HARD,
      source = "Surah An-Naml 27:30",
      hintEn = "Named after the ant that warned its colony.",
      hintAr = "سُميت باسم الحشرة الصغيرة التي خاطبت قومها."
    ),
    Question(
      id = "q_qur_6",
      questionEn = "Which verse is considered the greatest verse in the Quran?",
      questionAr = "ما هي أعظم آية في كتاب الله تعالى؟",
      optionsEn = listOf("Ayat al-Dayn", "Ayat al-Kursi", "Surah Al-Fatihah v.1", "Surah Al-Ikhlas v.1"),
      optionsAr = listOf("آية الدين", "آية الكرسي", "أول الفاتحة", "أول سورة الإخلاص"),
      correctAnswerIndex = 1,
      explanationEn = "Ayat al-Kursi (The Verse of the Throne, 2:255) is affirmed by the Prophet ﷺ as the greatest verse in the Book of Allah.",
      explanationAr = "آية الكرسي (سورة البقرة: ٢٥٥) أخبر النبي ﷺ أُبي بن كعب أنها أعظم آية في كتاب الله.",
      category = QuizCategory.QURAN,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Muslim (810)",
      hintEn = "It resides in Surah Al-Baqarah (verse 255).",
      hintAr = "تقع في سورة البقرة الآية رقم ٢٥٥."
    ),

    // 🕋 SEERAH CATEGORY
    Question(
      id = "q_see_1",
      questionEn = "In which city was Prophet Muhammad ﷺ born?",
      questionAr = "في أي مدينة وُلد النبي محمد ﷺ؟",
      optionsEn = listOf("Al-Madinah", "Makkah Al-Mukarramah", "Jerusalem", "Ta'if"),
      optionsAr = listOf("المدينة المنورة", "مكة المكرمة", "القدس", "الطائف"),
      correctAnswerIndex = 1,
      explanationEn = "Prophet Muhammad ﷺ was born in Makkah Al-Mukarramah in the Year of the Elephant.",
      explanationAr = "ولد الحبيب المصطفى ﷺ في مكة المكرمة في عام الفيل.",
      category = QuizCategory.SEERAH,
      difficulty = QuestionDifficulty.EASY,
      source = "Ar-Raheeq Al-Makhtum",
      hintEn = "The city where the Holy Kaaba is located.",
      hintAr = "البلد الحرام الذي يحتضن الكعبة المشرفة."
    ),
    Question(
      id = "q_see_2",
      questionEn = "Who was the first woman to embrace Islam?",
      questionAr = "من هي أول امرأة آمنت برسالة الإسلام وبرسول الله ﷺ؟",
      optionsEn = listOf("Aishah bint Abi Bakr", "Khadijah bint Khuwaylid", "Fatimah az-Zahra", "Asma bint Abi Bakr"),
      optionsAr = listOf("عائشة بنت أبي بكر", "خديجة بنت خويلد", "فاطمة الزهراء", "أسماء بنت أبي بكر"),
      correctAnswerIndex = 1,
      explanationEn = "Mother of the Believers, Khadijah bint Khuwaylid (may Allah be pleased with her), was the very first person and first woman to believe in the Prophet ﷺ.",
      explanationAr = "أم المؤمنين خديجة بنت خويلد رضي الله عنها أول من آمن بالرسول ﷺ ودعمه بمالها ونفسها.",
      category = QuizCategory.SEERAH,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari (3)",
      hintEn = "The beloved first wife of the Prophet ﷺ.",
      hintAr = "زوجة النبي ﷺ الأولى وأم أغلب أولاده رضي الله عنها."
    ),
    Question(
      id = "q_see_3",
      questionEn = "Who was the companion that accompanied the Prophet ﷺ during the Hijrah to Madinah?",
      questionAr = "من الصحابي الجليل الذي رافق النبي ﷺ في رحلة الهجرة النبوية المباركة؟",
      optionsEn = listOf("Umar ibn Al-Khattab", "Ali ibn Abi Talib", "Abu Bakr As-Siddiq", "Uthman ibn Affan"),
      optionsAr = listOf("عمر بن الخطاب", "علي بن أبي طالب", "أبو بكر الصديق", "عثمان بن عفان"),
      correctAnswerIndex = 2,
      explanationEn = "Abu Bakr As-Siddiq (may Allah be pleased with him) accompanied the Prophet ﷺ in the Cave of Thawr and throughout the Hijrah.",
      explanationAr = "أبو بكر الصديق رضي الله عنه ثاني اثنين إذ هما في الغار ورفيق الهجرة النبوية.",
      category = QuizCategory.SEERAH,
      difficulty = QuestionDifficulty.EASY,
      source = "Surah At-Tawbah 9:40",
      hintEn = "Known as As-Siddiq (The Truthful).",
      hintAr = "أول الخلفاء الراشدين وصاحب لقب (الصديق)."
    ),
    Question(
      id = "q_see_4",
      questionEn = "Who was chosen as the first Muezzin (caller to prayer) in Islam?",
      questionAr = "من هو مؤذن رسول الله ﷺ وأول من أذن للصلاة في الإسلام؟",
      optionsEn = listOf("Salman Al-Farsi", "Bilal ibn Rabah", "Ammar ibn Yasir", "Zayd ibn Harithah"),
      optionsAr = listOf("سلمان الفارسي", "بلال بن رباح", "عمار بن ياسر", "زيد بن حارثة"),
      correctAnswerIndex = 1,
      explanationEn = "The Prophet ﷺ chose Bilal ibn Rabah (may Allah be pleased with him) for his beautiful, resonant voice and steadfast faith.",
      explanationAr = "اختار النبي ﷺ بلال بن رباح الحبشي رضي الله عنه لنداوة صوته وثباته على التوحيد.",
      category = QuizCategory.SEERAH,
      difficulty = QuestionDifficulty.EASY,
      source = "Sunan Abi Dawud (499)",
      hintEn = "Famous for steadfastly proclaiming 'Ahad, Ahad' (One, One).",
      hintAr = "الصحابي الصابر الذي نادى (أحدٌ أحد)."
    ),
    Question(
      id = "q_see_5",
      questionEn = "What was the name of the treaty signed between Muslims and Quraysh in 6 AH?",
      questionAr = "ما اسم الصلح الشهير الذي عقده النبي ﷺ مع قريش في السنة السادسة للهجرة؟",
      optionsEn = listOf("Treaty of Yathrib", "Treaty of Al-Hudaybiyyah", "Pledge of Aqabah", "Pact of Najran"),
      optionsAr = listOf("ميثاق يثرب", "صلح الحديبية", "بيعة العقبة", "عهد نجران"),
      correctAnswerIndex = 1,
      explanationEn = "The Treaty of Al-Hudaybiyyah was hailed in the Quran as a clear victory (Fath Mubeen) paving the way for the peaceful spread of Islam.",
      explanationAr = "صلح الحديبية سمّاه الله تعالى في سورة الفتح {فَتْحًا مُّبِينًا} لما ترتب عليه من أمان وانتشار للدعوة.",
      category = QuizCategory.SEERAH,
      difficulty = QuestionDifficulty.MEDIUM,
      source = "Surah Al-Fath 48:1",
      hintEn = "Took place at a well/location near Makkah called Hudaybiyyah.",
      hintAr = "سُمي باسم المكان والآبار التي نزل عندها المسلمون قرب مكة."
    ),

    // 🕌 PROPHETS CATEGORY
    Question(
      id = "q_pro_1",
      questionEn = "Who was the first Prophet and human created by Allah?",
      questionAr = "من هو أول إنسان ونبي خلقه الله سبحانه وتعالى؟",
      optionsEn = listOf("Prophet Nuh", "Prophet Ibrahim", "Prophet Adam", "Prophet Idris"),
      optionsAr = listOf("نبي الله نوح", "نبي الله إبراهيم", "نبي الله آدم", "نبي الله إدريس"),
      correctAnswerIndex = 2,
      explanationEn = "Prophet Adam (peace be upon him) was the father of humanity and the first Prophet.",
      explanationAr = "آدم عليه السلام هو أبو البشر وأول الأنبياء عليهم جميعاً السلام.",
      category = QuizCategory.PROPHETS,
      difficulty = QuestionDifficulty.EASY,
      source = "Surah Al-Baqarah 2:30-34",
      hintEn = "Father of mankind.",
      hintAr = "أبو البشر وخُلق من طين."
    ),
    Question(
      id = "q_pro_2",
      questionEn = "Which Prophet was commanded by Allah to build an Ark to preserve the believers?",
      questionAr = "من هو النبي الذي أمره الله ببناء السفينة للنجاة مع المؤمنين من الطوفان؟",
      optionsEn = listOf("Prophet Hud", "Prophet Nuh", "Prophet Salih", "Prophet Lut"),
      optionsAr = listOf("نبي الله هود", "نبي الله نوح", "نبي الله صالح", "نبي الله لوط"),
      correctAnswerIndex = 1,
      explanationEn = "Prophet Nuh (Noah, peace be upon him) built the Ark under Allah's inspiration and guidance before the Great Flood.",
      explanationAr = "نوح عليه السلام صنع الفلك بأمر الله ووحيه لنجاة المؤمنين وزوجين من كل كائن حي.",
      category = QuizCategory.PROPHETS,
      difficulty = QuestionDifficulty.EASY,
      source = "Surah Hud 11:37",
      hintEn = "He called his people patiently for 950 years.",
      hintAr = "لبث في قومه يدعوهم تسعمائة وخمسين عاماً."
    ),
    Question(
      id = "q_pro_3",
      questionEn = "Which Prophet is known by the noble title Khalilullah (The Intimate Friend of Allah)?",
      questionAr = "من هو النبي الملقب بـ (خليل الله) وأبو الأنبياء؟",
      optionsEn = listOf("Prophet Ibrahim", "Prophet Musa", "Prophet Dawud", "Prophet Isa"),
      optionsAr = listOf("نبي الله إبراهيم", "نبي الله موسى", "نبي الله داود", "نبي الله عيسى"),
      correctAnswerIndex = 0,
      explanationEn = "Prophet Ibrahim (Abraham, peace be upon him) is called Khalilullah, as mentioned in the Quran (4:125).",
      explanationAr = "إبراهيم عليه السلام خليل الرحمن وأبو الأنبياء كما ورد: {وَاتَّخَذَ اللَّهُ إِبْرَاهِيمَ خَلِيلًا}.",
      category = QuizCategory.PROPHETS,
      difficulty = QuestionDifficulty.EASY,
      source = "Surah An-Nisa 4:125",
      hintEn = "He and his son Ismail raised the foundations of the Kaaba.",
      hintAr = "بنى الكعبة المشرفة مع ابنه إسماعيل عليهما السلام."
    ),
    Question(
      id = "q_pro_4",
      questionEn = "Which Prophet was swallowed by a large fish and called upon Allah in three layers of darkness?",
      questionAr = "من هو النبي صاحب الحوت الذي دعا ربه في ظلمات البحر والليل والبطن؟",
      optionsEn = listOf("Prophet Yunus", "Prophet Ayyub", "Prophet Zakariyya", "Prophet Yahya"),
      optionsAr = listOf("نبي الله يونس", "نبي الله أيوب", "نبي الله زكريا", "نبي الله يحيى"),
      correctAnswerIndex = 0,
      explanationEn = "Prophet Yunus (Jonah, peace be upon him) called out: 'There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.'",
      explanationAr = "يونس عليه السلام (ذو النون) دعا في بطن الحوت: {لَّا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ}.",
      category = QuizCategory.PROPHETS,
      difficulty = QuestionDifficulty.MEDIUM,
      source = "Surah Al-Anbiya 21:87",
      hintEn = "Also referred to as Dhun-Nun.",
      hintAr = "سُمي بذي النون (صاحب الحوت)."
    ),
    Question(
      id = "q_pro_5",
      questionEn = "Which Prophet spoke to people from the cradle as a miraculous sign by Allah's permission?",
      questionAr = "من هو النبي الذي كلّم الناس في المهد صبياً بإذن الله وتبرئةً لأمه الطاهرة؟",
      optionsEn = listOf("Prophet Yahya", "Prophet Isa (Jesus)", "Prophet Yusuf", "Prophet Sulaiman"),
      optionsAr = listOf("نبي الله يحيى", "نبي الله عيسى ابن مريم", "نبي الله يوسف", "نبي الله سليمان"),
      correctAnswerIndex = 1,
      explanationEn = "Prophet Isa ibn Maryam (peace be upon him) spoke in infancy saying: 'Indeed, I am the servant of Allah. He has given me the Scripture and made me a prophet.'",
      explanationAr = "عيسى ابن مريم عليه السلام كلّم الناس في المهد قائلاً: {إِنِّي عَبْدُ اللَّهِ آتَانِيَ الْكِتَابَ وَجَعَلَنِي نَبِيًّا}.",
      category = QuizCategory.PROPHETS,
      difficulty = QuestionDifficulty.EASY,
      source = "Surah Maryam 19:30",
      hintEn = "Son of Maryam (Mary).",
      hintAr = "ابن مريم العذراء الصديقة عليها السلام."
    ),

    // 🤲 WORSHIP CATEGORY
    Question(
      id = "q_wor_1",
      questionEn = "How many obligatory daily prayers (Fard) are prescribed for Muslims?",
      questionAr = "كم عدد الصلوات المفروضة على المسلم في اليوم والليلة؟",
      optionsEn = listOf("3", "5", "7", "4"),
      optionsAr = listOf("٣", "٥", "٧", "٤"),
      correctAnswerIndex = 1,
      explanationEn = "The five daily obligatory prayers are Fajr, Dhuhr, Asr, Maghrib, and Isha.",
      explanationAr = "الصلوات الخمس المفروضة هي: الفجر، والظهر، والعصر، والمغرب، والعشاء.",
      category = QuizCategory.WORSHIP,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari & Muslim",
      hintEn = "Second pillar of Islam, five in number.",
      hintAr = "الركن الثاني من أركان الإسلام، خمس صلوات."
    ),
    Question(
      id = "q_wor_2",
      questionEn = "What is the dry ablution called when clean earth/sand is used if water is unavailable?",
      questionAr = "ماذا يُسمى التطهر بالصعيد الطيب (التراب الطاهر) عند فقدان الماء أو العجز عنه؟",
      optionsEn = listOf("Ghusl", "Tayammum", "Istinja", "Taharah"),
      optionsAr = listOf("الغسل", "التيمم", "الاستنجاء", "الاستجمار"),
      correctAnswerIndex = 1,
      explanationEn = "Tayammum is the permissible symbolic dry ablution when water cannot be found or safely used.",
      explanationAr = "التيمم رخصة شرعية باستخدام التراب الطاهر عند تعذر استعمال الماء.",
      category = QuizCategory.WORSHIP,
      difficulty = QuestionDifficulty.EASY,
      source = "Surah Al-Ma'idah 5:6",
      hintEn = "Mentioned in Surah An-Nisa and Al-Ma'idah.",
      hintAr = "ورد حكمه في سورة المائدة {فَتَيَمَّمُوا صَعِيدًا طَيِّبًا}."
    ),
    Question(
      id = "q_wor_3",
      questionEn = "Towards what direction do Muslims face during their prayers?",
      questionAr = "إلى أي جهة يتوجه المسلمون في صلواتهم حول العالم؟",
      optionsEn = listOf("The East", "The Holy Kaaba in Makkah", "Mount Sinai", "Jerusalem currently"),
      optionsAr = listOf("المشرق", "الكعبة المشرفة بمكة المكرمة", "طور سيناء", "جهة الشمال دائماً"),
      correctAnswerIndex = 1,
      explanationEn = "Muslims unite by facing the Qiblah, which is the Holy Kaaba in Makkah Al-Mukarramah.",
      explanationAr = "يتوجه المسلمون شطر المسجد الحرام حيث الكعبة المشرفة وهي القبلة الموحدة.",
      category = QuizCategory.WORSHIP,
      difficulty = QuestionDifficulty.EASY,
      source = "Surah Al-Baqarah 2:144",
      hintEn = "The Qiblah established by Allah.",
      hintAr = "القبلة التي أمر الله بها: {فَوَلِّ وَجْهَكَ شَطْرَ الْمَسْجِدِ الْحَرَامِ}."
    ),
    Question(
      id = "q_wor_4",
      questionEn = "What is the standard minimum rate of Zakat on saved surplus wealth after one lunar year?",
      questionAr = "ما هو المقدار الواجب إخراجه في زكاة المال المدخر بعد مرور الحول وبلوغ النصاب؟",
      optionsEn = listOf("2.5% (One fortieth)", "5%", "10%", "1%"),
      optionsAr = listOf("٢٫٥٪ (ربع العشر)", "٥٪ (نصف العشر)", "١٠٪", "١٪"),
      correctAnswerIndex = 0,
      explanationEn = "The annual rate of Zakat on eligible financial wealth is 2.5% (one fortieth) after reaching the Nisab and passing of one lunar year.",
      explanationAr = "المقدار الواجب إخراجه في زكاة النقود وعروض التجارة هو ٢٫٥٪ (ربع العشر) بعد استيفاء الشروط.",
      category = QuizCategory.WORSHIP,
      difficulty = QuestionDifficulty.MEDIUM,
      source = "Sunnah & Consensus of Jurists",
      hintEn = "Two and a half percent.",
      hintAr = "ربع العشر أي ٢٫٥ بالمئة."
    ),

    // 🌙 RAMADAN CATEGORY
    Question(
      id = "q_ram_1",
      questionEn = "What is the pre-dawn meal eaten before fasting called?",
      questionAr = "ما اسم الوجبة المباركة التي يتناولها الصائم قبيل طلوع الفجر؟",
      optionsEn = listOf("Iftar", "Suhoor", "Walimah", "Ghadah"),
      optionsAr = listOf("الإفطار", "السحور", "الوليمة", "العشاء"),
      correctAnswerIndex = 1,
      explanationEn = "The Prophet ﷺ said: 'Take Suhoor, for indeed in Suhoor there is blessing.'",
      explanationAr = "السحور هو الطعام المتناول قبل الفجر، وقال ﷺ: {تسحروا فإن في السحور بركة}.",
      category = QuizCategory.RAMADAN,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari (1923)",
      hintEn = "Eaten late in the night before Fajr prayer.",
      hintAr = "وجبة البركة قبل أذان الفجر."
    ),
    Question(
      id = "q_ram_2",
      questionEn = "Which blessed night in Ramadan is better than a thousand months?",
      questionAr = "ما هي الليلة المباركة في شهر رمضان التي هي خير من ألف شهر؟",
      optionsEn = listOf("Laylat al-Isra", "Laylat al-Qadr (Night of Decree)", "Mid-Sha'ban", "Night of Arafah"),
      optionsAr = listOf("ليلة الإسراء", "ليلة القدر", "ليلة النصف من شعبان", "ليلة الجمعة الأولى"),
      correctAnswerIndex = 1,
      explanationEn = "Surah Al-Qadr states: 'The Night of Decree is better than a thousand months' (97:3).",
      explanationAr = "ليلة القدر أنزل الله فيها القرآن وجعل عبادتها خيراً من ألف شهر.",
      category = QuizCategory.RAMADAN,
      difficulty = QuestionDifficulty.EASY,
      source = "Surah Al-Qadr 97:1-3",
      hintEn = "Has an entire Surah named after it in the 30th Juz.",
      hintAr = "سورة كاملة في الجزء الثلاثين سُميت باسمها."
    ),
    Question(
      id = "q_ram_3",
      questionEn = "What is the special gate in Paradise reserved exclusively for those who fast?",
      questionAr = "ما اسم الباب الخاص في الجنة الذي يدخل منه الصائمون فقط يوم القيامة؟",
      optionsEn = listOf("Bab al-Jihad", "Bab ar-Rayyan", "Bab as-Sadaqah", "Bab as-Salah"),
      optionsAr = listOf("باب الجهاد", "باب الريان", "باب الصدقة", "باب الصلاة"),
      correctAnswerIndex = 1,
      explanationEn = "The Prophet ﷺ said: 'In Paradise there is a gate called Ar-Rayyan, through which only those who fast will enter on the Day of Resurrection.'",
      explanationAr = "قال ﷺ: {إن في الجنة باباً يقال له الريان، يدخل منه الصائمون يوم القيامة لا يدخل منه أحد غيرهم}.",
      category = QuizCategory.RAMADAN,
      difficulty = QuestionDifficulty.MEDIUM,
      source = "Sahih Al-Bukhari (1896)",
      hintEn = "Name derived from quench/refreshment.",
      hintAr = "اسمه مشتق من الارتواء بعد العطش."
    ),

    // 📿 ADHKAR & DUAS CATEGORY
    Question(
      id = "q_adh_1",
      questionEn = "What should a Muslim say before starting any good action or meal?",
      questionAr = "ما هي التسمية المشروعة التي يستفتح بها المسلم طعامه وعمله؟",
      optionsEn = listOf("Alhamdulillah", "Bismillah (In the name of Allah)", "Astaghfirullah", "Allahu Akbar"),
      optionsAr = listOf("الحمد لله", "بسم الله", "أستغفر الله", "الله أكبر"),
      correctAnswerIndex = 1,
      explanationEn = "Saying 'Bismillah' invokes Allah's name and blesses whatever permissible act one begins.",
      explanationAr = "التسمية (بسم الله) تُبارك العمل وتطرد الشيطان وتستجلب العون الإلهي.",
      category = QuizCategory.ADHKAR,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Muslim (2017)",
      hintEn = "It means 'In the name of Allah'.",
      hintAr = "معناها البدء باسم الله العظيم."
    ),
    Question(
      id = "q_adh_2",
      questionEn = "What dua does the Quran teach us to ask Allah for an increase in beneficial knowledge?",
      questionAr = "ما هو الدعاء القرآني الجامع لطلب الزيادة في العلم النافع؟",
      optionsEn = listOf("Rabbana aatina fid-dunya hasanah", "Rabbi zidni 'ilma", "Rabbi hab li hukma", "Rabbi ishrah li sadri"),
      optionsAr = listOf("ربنا آتنا في الدنيا حسنة", "رَّبِّ زِدْنِي عِلْمًا", "رب هب لي حكماً", "رب اشرح لي صدري"),
      correctAnswerIndex = 1,
      explanationEn = "Allah instructed the Prophet ﷺ in Surah Ta-Ha (20:114): 'And say: My Lord, increase me in knowledge.'",
      explanationAr = "أمر الله نبيه ﷺ في سورة طه: {وَقُل رَّبِّ زِدْنِي عِلْمًا} وهي الآية الوحيدة التي أُمر فيها بطلب الزيادة من شيء.",
      category = QuizCategory.ADHKAR,
      difficulty = QuestionDifficulty.EASY,
      source = "Surah Ta-Ha 20:114",
      hintEn = "Surah Ta-Ha verse 114.",
      hintAr = "سورة طه آية رقم ١١٤."
    ),
    Question(
      id = "q_adh_3",
      questionEn = "When someone sneezes and praises Allah ('Alhamdulillah'), what should the listener respond?",
      questionAr = "إذا عطس المسلم فحمد الله، فماذا يقول له أخوه الذي سمعه؟",
      optionsEn = listOf("Barak Allahu feek", "YarhamukAllah (May Allah have mercy on you)", "Jazak Allahu khayr", "As-salamu alaykum"),
      optionsAr = listOf("بارك الله فيك", "يَرْحَمُكَ اللَّهُ", "جزاك الله خيراً", "حياك الله"),
      correctAnswerIndex = 1,
      explanationEn = "The Prophet ﷺ taught: When one sneezes and says 'Alhamdulillah', his brother should respond 'YarhamukAllah'.",
      explanationAr = "قال ﷺ: {فإذا عطس أحدكم وحمد الله فحق على كل مسلم سمعه أن يقول له: يرحمك الله}.",
      category = QuizCategory.ADHKAR,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari (6224)",
      hintEn = "A supplication for Allah's mercy upon them.",
      hintAr = "دعاء له بنيل رحمة الله ومغفرته."
    ),

    // 👨👩👧 ISLAMIC MANNERS
    Question(
      id = "q_man_1",
      questionEn = "How does Islam guide believers to treat their parents, especially in old age?",
      questionAr = "كيف وجّه الإسلام أبناء المسلمين للتعامل مع الوالدين خاصة عند الكبر؟",
      optionsEn = listOf("With utter respect, gentle speech and kindness", "With indifference", "Only support them financially without speaking", "Argue with them when disagreed"),
      optionsAr = listOf("بالبر الكامل والقول الكريم وخفض جناح الذل لهما", "بإهمال مشاعرهما", "بالمساعدة المالية فقط دون كلام", "بمقاطعتهما عند الخلاف"),
      correctAnswerIndex = 0,
      explanationEn = "The Quran commands: 'Do not say to them [so much as], 'uff', and do not repel them but speak to them a noble word.'",
      explanationAr = "أمر الله تعالى: {فَلَا تَقُل لَّهُمَا أُفٍّ وَلَا تَنْهَرْهُمَا وَقُل لَّهُمَا قَوْلًا كَرِيمًا} (الإسراء: ٢٣).",
      category = QuizCategory.MANNERS,
      difficulty = QuestionDifficulty.EASY,
      source = "Surah Al-Isra 17:23",
      hintEn = "Never say even 'uff' of impatience to them.",
      hintAr = "نهى الله عن قول كلمة (أُفٍّ) لهما."
    ),
    Question(
      id = "q_man_2",
      questionEn = "What did the Prophet ﷺ describe a warm smile to your fellow brother as?",
      questionAr = "بماذا وصف النبي ﷺ ابتسامة المسلم الصادقة في وجه أخيه؟",
      optionsEn = listOf("An act of charity (Sadaqah)", "A neutral deed", "Only customary behavior", "A formal gesture"),
      optionsAr = listOf("صدقة يؤجر عليها", "عادة دنيوية مجردة", "أمر لا ثواب فيه", "تصرف رسمي"),
      correctAnswerIndex = 0,
      explanationEn = "The Prophet ﷺ said: 'Smiling in the face of your brother is an act of charity for you.'",
      explanationAr = "قال رسول الله ﷺ: {تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ}.",
      category = QuizCategory.MANNERS,
      difficulty = QuestionDifficulty.EASY,
      source = "Jami` at-Tirmidhi (1956)",
      hintEn = "It is considered a rewarding good deed (Sadaqah).",
      hintAr = "تُعد في ميزان الحسنات كبذل الصدقة."
    ),
    Question(
      id = "q_man_3",
      questionEn = "How did the Prophet ﷺ emphasize the rights of the neighbor?",
      questionAr = "كيف أكد النبي ﷺ على عِظَم حق الجار وحسن معاملته؟",
      optionsEn = listOf("He said Jibril kept recommending the neighbor until he thought Jibril would grant him inheritance", "Only greeting them during Eid", "Ignoring their problems", "Only if they are relatives"),
      optionsAr = listOf("أخبر أن جبريل ما زال يوصيه بالجار حتى ظن أنه سيورثه", "بالسلام عليهم فقط في الأعياد", "تجنب معرفة أحوالهم", "فقط إذا كانوا من الأقارب"),
      correctAnswerIndex = 0,
      explanationEn = "The Prophet ﷺ said: 'Jibril kept recommending good treatment of the neighbor to me until I thought he would give him a share of inheritance.'",
      explanationAr = "قال ﷺ: {ما زال جبريل يوصيني بالجار حتى ظننت أنه سيورثه}.",
      category = QuizCategory.MANNERS,
      difficulty = QuestionDifficulty.MEDIUM,
      source = "Sahih Al-Bukhari (6014)",
      hintEn = "Recommended persistently by Angel Jibril.",
      hintAr = "أوصى به أمين الوحي جبريل عليه السلام مراراً وتكراراً."
    ),

    // 🧠 GENERAL KNOWLEDGE CATEGORY
    Question(
      id = "q_gen_1",
      questionEn = "What is the first month of the Islamic Hijri calendar?",
      questionAr = "ما هو الشهر الأول في ترتيب شهور التقويم الهجري الإسلامي؟",
      optionsEn = listOf("Ramadan", "Muharram", "Safar", "Dhul-Hijjah"),
      optionsAr = listOf("رمضان", "المُحَرَّم", "صفر", "ذو الحجة"),
      correctAnswerIndex = 1,
      explanationEn = "Muharram is the first month of the Islamic lunar calendar and one of the four sacred months.",
      explanationAr = "شهر الله المحرم هو غرة السنة الهجرية وأول شهورها وأحد الأشهر الحرم الأربعة.",
      category = QuizCategory.GENERAL,
      difficulty = QuestionDifficulty.EASY,
      source = "Islamic Lunar Calendar",
      hintEn = "A sacred month whose 10th day is Ashura.",
      hintAr = "شهر حرام يصوم المسلمون يومه العاشر (عاشوراء)."
    ),
    Question(
      id = "q_gen_2",
      questionEn = "What is the name of the miraculous spring that gushed forth in Makkah for Lady Hajar and infant Ismail?",
      questionAr = "ما اسم عين الماء المباركة التي تفجرت في مكة بأمر الله لإسماعيل وأمه هاجر عليها السلام؟",
      optionsEn = listOf("Zamzam", "Kawthar", "Tasneem", "Salsabeel"),
      optionsAr = listOf("زمزم", "الكوثر", "تسنيم", "سلسبيل"),
      correctAnswerIndex = 0,
      explanationEn = "The blessed Well of Zamzam in the Holy Mosque of Makkah sprang forth under the guidance of Angel Jibril for Hajar and Ismail.",
      explanationAr = "بئر زمزم المباركة في الحرم المكي الشريف نبعت بفضل الله كرامةً لهاجر ورضيعها إسماعيل.",
      category = QuizCategory.GENERAL,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari (3364)",
      hintEn = "Pilgrims drink from it in the Sacred Mosque.",
      hintAr = "ماء مبارك يحرص الحجاج والمعتمرون على الشرب منه والتزود به."
    ),
    Question(
      id = "q_gen_3",
      questionEn = "Which noble companion was given the title 'Dhun-Nurayn' (Possessor of Two Lights)?",
      questionAr = "أي الصحابة الكرام لُقب بـ (ذي النورين) لزواجه من ابنتي رسول الله ﷺ؟",
      optionsEn = listOf("Umar ibn Al-Khattab", "Uthman ibn Affan", "Ali ibn Abi Talib", "Abu Ubaidah"),
      optionsAr = listOf("عمر بن الخطاب", "عثمان بن عفان", "علي بن أبي طالب", "أبو عبيدة بن الجراح"),
      correctAnswerIndex = 1,
      explanationEn = "Uthman ibn Affan (may Allah be pleased with him) was titled Dhun-Nurayn because he married two daughters of the Prophet ﷺ (Ruqayyah, and after her passing, Umm Kulthum).",
      explanationAr = "عثمان بن عفان رضي الله عنه ثالث الخلفاء الراشدين وجامع المصحف، لُقب بذي النورين.",
      category = QuizCategory.GENERAL,
      difficulty = QuestionDifficulty.MEDIUM,
      source = "Al-Bidayah wan-Nihayah",
      hintEn = "Third Caliph of Islam who unified the written Quran.",
      hintAr = "الخليفة الراشد الثالث رضي الله عنه."
    )
  )

  private val questionBank: List<Question> = initialQuestions +
    QuranQuestionData.questions +
    SeerahQuestionData.questions +
    ProphetsQuestionData.questions +
    WorshipQuestionData.questions +
    RamadanQuestionData.questions +
    AdhkarQuestionData.questions +
    MannersQuestionData.questions +
    GeneralQuestionData.questions

  fun getAllQuestions(): List<Question> = questionBank

  fun getQuestionsForCategory(category: QuizCategory, limit: Int = 10): List<Question> {
    val filtered = questionBank.filter { it.category == category }
    return filtered.shuffled().take(limit)
  }

  fun getQuestionsForMode(mode: GameMode, category: QuizCategory? = null): List<Question> {
    return when (mode) {
      GameMode.QUICK_CHALLENGE -> questionBank.shuffled().take(mode.defaultQuestionCount)
      GameMode.DAILY_CHALLENGE -> {
        // Daily deterministic seed using epoch day
        val epochDay = DateUtils.getTodayEpochDay()
        val rnd = java.util.Random(epochDay)
        questionBank.shuffled(rnd).take(mode.defaultQuestionCount)
      }
      GameMode.CATEGORY_CHALLENGE -> {
        val cat = category ?: QuizCategory.QURAN
        getQuestionsForCategory(cat, mode.defaultQuestionCount)
      }
      GameMode.TIME_CHALLENGE -> questionBank.shuffled().take(mode.defaultQuestionCount)
      GameMode.SURVIVAL_MODE -> questionBank.shuffled().take(mode.defaultQuestionCount)
      GameMode.LEVEL_MODE -> {
        val sorted = questionBank.sortedBy { it.difficulty.ordinal }
        sorted.take(mode.defaultQuestionCount)
      }
      GameMode.PRACTICE_MODE -> {
        if (category != null) {
          questionBank.filter { it.category == category }.ifEmpty { questionBank }
        } else {
          questionBank.shuffled()
        }.take(mode.defaultQuestionCount)
      }
    }
  }

  fun getQuestionCountForCategory(category: QuizCategory): Int {
    return questionBank.count { it.category == category }
  }
}
