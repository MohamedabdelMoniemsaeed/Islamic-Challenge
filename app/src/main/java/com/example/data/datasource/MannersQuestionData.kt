package com.example.data.datasource

import com.example.data.models.Question
import com.example.data.models.QuestionDifficulty
import com.example.data.models.QuizCategory

object MannersQuestionData {
  val questions: List<Question> = listOf(
    Question(
      id = "q_man_4",
      questionEn = "When a man asked the Prophet ﷺ 'Who is most deserving of my fine companionship?', what was the Prophet's reply?",
      questionAr = "عندما سأل رجل النبي ﷺ: (من أحق الناس بحسن صحابتي؟) فبماذا أجابه الحبيب المصطفى ﷺ أولاً؟",
      optionsEn = listOf("Your father", "Your mother", "Your eldest brother", "Your closest teacher"),
      optionsAr = listOf("أبوك", "أُمُّكَ (كررها ثلاثاً قبل أن يذكر الأب)", "أخوك الأكبر", "معلمك"),
      correctAnswerIndex = 1,
      explanationEn = "The Prophet ﷺ repeated: 'Your mother, then your mother, then your mother, then your father' highlighting the immense honor of maternal sacrifice.",
      explanationAr = "قال رسول الله ﷺ: {أمك، قال ثم من؟ قال: أمك، قال ثم من؟ قال: أمك، قال ثم من؟ قال: أبوك}، مقدماً الأم ثلاث مرات لعظم برها ومشقتها في الحمل والولادة والرضاع.",
      category = QuizCategory.MANNERS,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari (5971) & Sahih Muslim",
      hintEn = "Repeated three times before mentioning the father.",
      hintAr = "كررها ثلاث مرات تأكيداً لحقها العظيم."
    ),
    Question(
      id = "q_man_5",
      questionEn = "What did the Prophet ﷺ say regarding truthfulness (As-Sidq) in guiding to righteousness?",
      questionAr = "ماذا قال النبي ﷺ في فضل الصدق ومصير الصادقين في الحديث المتفق عليه؟",
      optionsEn = listOf(
        "Truthfulness is only needed in business transactions",
        "Truthfulness brings worldly fame only",
        "Truthfulness leads to righteousness, and righteousness leads to Paradise",
        "Truthfulness is optional with strangers"
      ),
      optionsAr = listOf(
        "الصدق مطلوب فقط في البيع والشراء",
        "الصدق يجلب الشهرة الدنيوية فقط",
        "إِنَّ الصِّدْقَ يَهْدِي إِلَى الْبِرِّ، وَإِنَّ الْبِرَّ يَهْدِي إِلَى الْجَنَّةِ",
        "الصدق أمر غير ملزم مع الغرباء"
      ),
      correctAnswerIndex = 2,
      explanationEn = "The Prophet ﷺ said: 'Truthfulness leads to righteousness, and righteousness leads to Paradise. A man keeps speaking the truth until he is recorded with Allah as a Siddiq (truthful).'",
      explanationAr = "قال رسول الله ﷺ: {إن الصدق يهدي إلى البر، وإن البر يهدي إلى الجنة، وإن الرجل ليصدق حتى يكتب عند الله صديقاً}.",
      category = QuizCategory.MANNERS,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari (6094) & Sahih Muslim",
      hintEn = "Truth leads to righteousness, which leads to Jannah.",
      hintAr = "الصدق طريق البر والبر مفتاح دخول الجنة ورضوان الله."
    ),
    Question(
      id = "q_man_6",
      questionEn = "What brief and momentous counsel did the Prophet ﷺ repeat three times to the man who asked: 'Advise me'?",
      questionAr = "ما هي الوصية النبوية البليغة التي كررها النبي ﷺ ثلاث مرات لرجل طلب نصيحة موجزة جامعة؟",
      optionsEn = listOf("Do not sleep early", "Do not get angry (La taghdab)", "Do not travel alone", "Do not eat dates"),
      optionsAr = listOf("لا تنم مبكراً", "لَا تَغْضَبْ (كررها مراراً)", "لا تسافر وحدك", "لا تأكل كثيراً"),
      correctAnswerIndex = 1,
      explanationEn = "A man asked the Prophet ﷺ: 'Advise me.' The Prophet replied: 'Do not get angry.' The man repeated his request several times, and the Prophet repeated: 'Do not get angry.'",
      explanationAr = "جاء رجل إلى النبي ﷺ فقال: أوصني، قال: {لا تغضب}. فردد مراراً، قال: {لا تغضب}، لحفظ سلامة العقل وصيانة العلاقات من الطيش والندم.",
      category = QuizCategory.MANNERS,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari (6116)",
      hintEn = "Controlling temper: 'La taghdab'.",
      hintAr = "التحكم في الانفعال وكظم الغيظ: (لا تغضب)."
    ),
    Question(
      id = "q_man_7",
      questionEn = "What did the Prophet ﷺ say about modesty (Al-Haya') in the religion of Islam?",
      questionAr = "ماذا قال النبي ﷺ عن خلق (الحياء) ومكانته في منظومة الإيمان؟",
      optionsEn = listOf(
        "Modesty is a sign of weakness",
        "Modesty should be abandoned in public",
        "Modesty has no reward",
        "Modesty brings nothing except good, and it is a branch of Faith"
      ),
      optionsAr = listOf(
        "الحياء ضعف في الشخصية",
        "الحياء من الأخلاق غير المرغوبة",
        "الحياء لا ثواب عليه في الآخرة",
        "الْحَيَاءُ لَا يَأْتِي إِلَّا بِخَيْرٍ، وَهُوَ شُعْبَةٌ مِنَ الْإِيمَانِ"
      ),
      correctAnswerIndex = 3,
      explanationEn = "The Prophet ﷺ stated: 'Modesty brings nothing but good' and in another narration: 'Modesty is a branch of faith.'",
      explanationAr = "قال رسول الله ﷺ: {الحياء لا يأتي إلا بخير}، وفي صحيح مسلم: {الحياء شعبة من الإيمان}، فهو يمنع من ارتكاب المعاصي ويدعو لكل مكرمة.",
      category = QuizCategory.MANNERS,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari (6117) & Sahih Muslim",
      hintEn = "It only brings good, and is a branch of Iman.",
      hintAr = "خلق إسلامي أصيل يزين صاحبه ولا يثمر إلا الخير."
    ),
    Question(
      id = "q_man_8",
      questionEn = "How does the Holy Quran describe backbiting (Gheebah) to vividly illustrate its abhorrence?",
      questionAr = "كيف صور القرآن الكريم شناعة وقبح الغيبة في سورة الحجرات؟",
      optionsEn = listOf(
        "Like burning a fruitful tree",
        "Like throwing stones into water",
        "Like eating the flesh of one's dead brother",
        "Like walking in the desert without water"
      ),
      optionsAr = listOf(
        "كإحراق شجرة مثمرة",
        "كإلقاء حجر في بئر عميقة",
        "كَأَكْلِ لَحْمِ الْأَخِ الْمَيْتِ",
        "كالمشي في الصحراء بغير زاد"
      ),
      correctAnswerIndex = 2,
      explanationEn = "Surah Al-Hujurat (49:12) asks: 'Would one of you like to eat the flesh of his dead brother? You would despise it. And fear Allah.'",
      explanationAr = "قال تعالى في سورة الحجرات: {وَلَا يَغْتَب بَّعْضُكُم بَعْضًا ۚ أَيُحِبُّ أَحَدُكُمْ أَن يَأْكُلَ لَحْمَ أَخِيهِ مَيْتًا فَكَرِهْتُمُوهُ} تشبيهاً منفراً لقطع دابر الغيبة.",
      category = QuizCategory.MANNERS,
      difficulty = QuestionDifficulty.EASY,
      source = "Surah Al-Hujurat (49:12)",
      hintEn = "Surah Al-Hujurat verse 12.",
      hintAr = "تشبيه بليغ بأكل لحم الأخ الميت للتنفير التام منها."
    ),
    Question(
      id = "q_man_9",
      questionEn = "What is the consequence of severing ties of kinship (Qat' ar-Rahim) according to authentic Hadith?",
      questionAr = "ما هو الوعيد النبوي الشديد الوارد في شأن قاطع الرحم؟",
      optionsEn = listOf(
        "His worldly wealth increases",
        "He is exempted from fasting",
        "It has no bearing on salvation",
        "The severer of ties will not enter Paradise"
      ),
      optionsAr = listOf(
        "تزداد بركة ماله",
        "يُعفى من أداء الصدقات",
        "أمر دنيوي لا علاقة له بالآخرة",
        "لَا يَدْخُلُ الْجَنَّةَ قَاطِعُ رَحِمٍ"
      ),
      correctAnswerIndex = 3,
      explanationEn = "The Prophet ﷺ said: 'The one who severs ties of kinship will not enter Paradise' (Sahih Al-Bukhari 5984), highlighting the paramount sanctity of familial bonds.",
      explanationAr = "قال رسول الله ﷺ: {لا يدخل الجنة قاطع رحم}، وحث الإسلام على صلة الأرحام ولو كان الأقارب يقطعونها إعلاءً للأخوة وصلة القرابة.",
      category = QuizCategory.MANNERS,
      difficulty = QuestionDifficulty.MEDIUM,
      source = "Sahih Al-Bukhari (5984) & Sahih Muslim",
      hintEn = "Will not enter Paradise.",
      hintAr = "حرمان من دخول الجنة مع الأولين لقطيعته ما أمر الله به أن يوصل."
    ),
    Question(
      id = "q_man_10",
      questionEn = "What did the Prophet ﷺ declare about the Muslim who guarantees what is between his jaws (tongue) and legs (chastity)?",
      questionAr = "بماذا وعد النبي ﷺ من يضمن ويحفظ ما بين لحييه (لسانه) وما بين رجليه (فرجه)؟",
      optionsEn = listOf("A golden crown", "I guarantee for him Paradise", "Lifelong immunity from illness", "Treasures of Rome"),
      optionsAr = listOf("تاجاً من الياقوت", "ضَمِنْتُ لَهُ الْجَنَّةَ", "العافية الدائمة من الأمراض", "كنوز كسرى وقيصر"),
      correctAnswerIndex = 1,
      explanationEn = "The Prophet ﷺ said: 'Whoever guarantees for me what is between his two jaws and what is between his two legs, I will guarantee for him Paradise.'",
      explanationAr = "قال رسول الله ﷺ: {من يضمن لي ما بين لحييه وما بين رجليه أضمن له الجنة}، صيانةً لعفة اللسان عن الفحش والكذب وعفة البدن عن الحرام.",
      category = QuizCategory.MANNERS,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari (6474)",
      hintEn = "Guaranteed Jannah by the Prophet ﷺ.",
      hintAr = "ضمان دخول جنات النعيم من الصادق المصدوق ﷺ."
    ),
    Question(
      id = "q_man_11",
      questionEn = "What is the Islamic etiquette when sitting in a gathering of three people?",
      questionAr = "ما هو الأدب النبوي الرفيع إذا كان ثلاثة أشخاص معاً في مكان واحد مراعاة للمشاعر؟",
      optionsEn = listOf(
        "All three must speak simultaneously",
        "They must stand up immediately",
        "Two should not whisper privately excluding the third, lest it grieves him",
        "The youngest must not speak at all"
      ),
      optionsAr = listOf(
        "أن يتكلم الجميع بصوت مرتفع معاً",
        "أن يغادر الثالث فوراً",
        "أَلَّا يَتَنَاجَى اثْنَانِ دُونَ الثَّالِثِ حَتَّى يَخْتَلِطُوا بِالنَّاسِ مَخَافَةَ أَنْ يُحْزِنَهُ",
        "أن يسكت الأصغر سناً دائماً"
      ),
      correctAnswerIndex = 2,
      explanationEn = "The Prophet ﷺ instructed: 'If you are three, then two should not whisper privately excluding the third until you mix with other people, for that would grieve him.'",
      explanationAr = "قال رسول الله ﷺ: {إذا كنتم ثلاثة فلا يتناجى اثنان دون الآخر حتى تختلطوا بالناس من أجل أن يحزنه}، أدباً إسلامياً رفيعاً لمنع إيغار الصدور وسوء الظن.",
      category = QuizCategory.MANNERS,
      difficulty = QuestionDifficulty.MEDIUM,
      source = "Sahih Al-Bukhari (6290) & Sahih Muslim",
      hintEn = "Two must not whisper to the exclusion of the third.",
      hintAr = "النهي عن تناجي اثنين دون الثالث مراعاة لخاطره ومشاعره."
    ),
    Question(
      id = "q_man_12",
      questionEn = "What are the three signs of a hypocrite (Munafiq) outlined in the authentic Hadith?",
      questionAr = "ما هي العلامات الثلاث لـ (المنافق) التي حذر النبي ﷺ من الاتصاف بها في الحديث المشهور؟",
      optionsEn = listOf(
        "When he speaks he lies, when he promises he breaks it, and when entrusted he betrays",
        "He dresses in white, walks fast, and wakes early",
        "He prays long, speaks little, and gives charity",
        "He travels frequently, reads books, and fasts in winter"
      ),
      optionsAr = listOf(
        "إِذَا حَدَّثَ كَذَبَ، وَإِذَا وَعَدَ أَخْلَفَ، وَإِذَا اؤْتُمِنَ خَانَ",
        "يلبس البياض، ويسرع في مشيه، ويستيقظ فجراً",
        "يطيل الصلاة، ويقلل الكلام، ويتصدق كثيراً",
        "يسافر كثيراً، ويقرأ الكتب، ويصوم في الشتاء"
      ),
      correctAnswerIndex = 0,
      explanationEn = "The Prophet ﷺ said: 'The signs of a hypocrite are three: whenever he speaks he lies, whenever he promises he breaks his promise, and whenever he is entrusted he betrays the trust.'",
      explanationAr = "قال رسول الله ﷺ: {آية المنافق ثلاث: إذا حدث كذب، وإذا وعد أخلف، وإذا اؤتمن خان}، فيجب على المؤمن الحذر من هذه الصفات الذميمة.",
      category = QuizCategory.MANNERS,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari (33) & Sahih Muslim",
      hintEn = "Lies, breaks promises, betrays trusts.",
      hintAr = "الكذب في الحديث وإخلاف العهود وخيانة الأمانات."
    ),
    Question(
      id = "q_man_13",
      questionEn = "What is the proper manner of eating taught by the Prophet ﷺ to Umar ibn Abi Salamah?",
      questionAr = "ما هي الآداب النبوية الثلاثة التي علمها النبي ﷺ لربيبه عمر بن أبي سلمة عند الأكل؟",
      optionsEn = listOf(
        "Eat with both hands, begin from the center, and speak loudly",
        "Eat while standing, drink quickly, and blow on hot food",
        "Begin with dessert, eat without washing hands, and avoid bread",
        "Say Bismillah, eat with your right hand, and eat from what is nearest to you"
      ),
      optionsAr = listOf(
        "كل باليدين معاً، وابدأ من وسط الإناء، وتكلم بصوت عالٍ",
        "كل قائماً، واشرب دفعة واحدة، وانفخ في الطعام الحار",
        "ابدأ بالحلويات، وكل بغير غسل اليدين، وتجنب الخبز",
        "سَمِّ اللَّهَ، وَكُلْ بِيَمِينِكَ، وَكُلْ مِمَّا يَلِيكَ"
      ),
      correctAnswerIndex = 3,
      explanationEn = "The Prophet ﷺ said to the young boy: 'O boy, mention the Name of Allah (say Bismillah), eat with your right hand, and eat from that which is nearest to you.'",
      explanationAr = "قال النبي ﷺ للغلام: {يا غلام، سمّ الله، وكل بيمينك، وكل مما يليك}، فكانت تلك طعمة المسلم وأدبه الرفيع على المائدة.",
      category = QuizCategory.MANNERS,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari (5376) & Sahih Muslim",
      hintEn = "Say Bismillah, eat with the right hand, and eat from what is in front of you.",
      hintAr = "التسمية أولاً، والأكل باليمين، وتناول الطعام مما يقارب يد الآكل."
    ),
    Question(
      id = "q_man_14",
      questionEn = "What did the Prophet ﷺ teach about removing harmful objects from pathways (Imatat al-Adha)?",
      questionAr = "ما مكانة وفضل (إماطة الأذى عن الطريق) في ميزان العمل الصالح والإيمان؟",
      optionsEn = listOf(
        "It is reserved only for government workers",
        "It is disliked to touch fallen branches",
        "It is an act of charity (Sadaqah) and the lowest branch of faith",
        "It has no religious reward"
      ),
      optionsAr = listOf(
        "عمل مقتصر على عمال النظافة فقط",
        "مكروه ولا ثواب فيه",
        "صَدَقَةٌ وَأَدْنَى شُعَبِ الْإِيمَانِ وَسَبَبٌ لِمَغْفِرَةِ الذُّنُوبِ",
        "أمر دنيوي لا صلة له بالإسلام"
      ),
      correctAnswerIndex = 2,
      explanationEn = "The Prophet ﷺ said: 'Removing harmful things from the road is an act of charity' and in Muslim: 'Faith has over seventy branches, the lowest of which is removing harm from the path.'",
      explanationAr = "قال رسول الله ﷺ: {وتميط الأذى عن الطريق صدقة}، وأخبر أن الإيمان بضع وسبعون شعبة أدناها إماطة الأذى عن الطريق رحمة بالمارة.",
      category = QuizCategory.MANNERS,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari (2989) & Sahih Muslim (35)",
      hintEn = "An act of charity and part of Iman.",
      hintAr = "صدقة يؤجر عليها العبد وتنم عن إحساسه بالمسؤولية المجتمعية."
    ),
    Question(
      id = "q_man_15",
      questionEn = "What did the Prophet ﷺ teach about seeking permission before entering homes (Isti'dhan)?",
      questionAr = "كم مرة يُسن الاستئذان وطرق الباب قبل الانصراف إذا لم يُؤذن للزائر؟",
      optionsEn = listOf("Once only", "Three times; if permitted enter, otherwise return", "Ten times", "Keep knocking until they open"),
      optionsAr = listOf("مرة واحدة فقط", "ثَلَاثَ مَرَّاتٍ، فَإِنْ أُذِنَ لَكَ وَإِلَّا فَارْجِعْ", "عشر مرات", "الاستمرار في الطرق حتى يفتحوا"),
      correctAnswerIndex = 1,
      explanationEn = "The Prophet ﷺ said: 'Seeking permission is three times; if permission is granted to you, enter; otherwise, go back.'",
      explanationAr = "قال رسول الله ﷺ: {الاستئذان ثلاث، فإن أُذن لك وإلا فارجع} صيانة لحرمات البيوت وخصوصية أهلها وراحتهم.",
      category = QuizCategory.MANNERS,
      difficulty = QuestionDifficulty.MEDIUM,
      source = "Sahih Al-Bukhari (6245) & Sahih Muslim",
      hintEn = "Three times max.",
      hintAr = "ثلاث مرات لا يزيد عليها حفظاً لحرمات البيوت."
    ),
    Question(
      id = "q_man_16",
      questionEn = "What is the definition of true strength according to the Prophet ﷺ in the authentic Hadith?",
      questionAr = "من هو القوي الحقيقي (الشديد) كما عرفه النبي ﷺ في الحديث الشريف؟",
      optionsEn = listOf(
        "The one who overpowers opponents in wrestling",
        "The one who lifts heavy boulders",
        "The one who accumulates the most weapons",
        "The one who controls himself during moments of anger"
      ),
      optionsAr = listOf(
        "الذي يصرع الرجال في المصارعة",
        "الذي يحمل الأثقال الضخمة",
        "الذي يملك أسلحة وقوة جسدية فقط",
        "الَّذِي يَمْلِكُ نَفْسَهُ عِنْدَ الْغَضَبِ"
      ),
      correctAnswerIndex = 3,
      explanationEn = "The Prophet ﷺ said: 'The strong man is not the one who overcomes people in wrestling; rather, the strong man is the one who controls himself when angry.'",
      explanationAr = "قال رسول الله ﷺ: {ليس الشديد بالصُّرَعَة، إنما الشديد الذي يملك نفسه عند الغضب}، فالقوة الحقيقية هي كبح النفس عن الطغيان والانتقام.",
      category = QuizCategory.MANNERS,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari (6114) & Sahih Muslim",
      hintEn = "The one who controls himself when angry.",
      hintAr = "مالك زمام نفسه ومشاعره عند فوران الغضب."
    ),
    Question(
      id = "q_man_17",
      questionEn = "What is the virtue of spreading the greeting of peace (Salam) among people known and unknown?",
      questionAr = "ما هو الفضل الثابت لإفشاء السلام بين المسلمين من عرفت ومن لم تعرف؟",
      optionsEn = listOf(
        "It spreads mutual love and leads to faith and entering Paradise",
        "It is only rewarded when greeting family members",
        "It is a modern political custom",
        "It should only be said during daytime"
      ),
      optionsAr = listOf(
        "سَبَبٌ لِشُيُوعِ الْمَحَبَّةِ وَكَمَالِ الْإِيمَانِ وَدُخُولِ الْجَنَّةِ",
        "يُثاب عليه فقط إذا كان المسلم من الأقارب",
        "عادة اجتماعية لا أجر فيها",
        "يُقال فقط في النهار دون الليل"
      ),
      correctAnswerIndex = 0,
      explanationEn = "The Prophet ﷺ said: 'You will not enter Paradise until you believe, and you will not believe until you love one another. Shall I guide you to something that if you do, you will love one another? Spread peace (Salam) amongst yourselves.'",
      explanationAr = "قال رسول الله ﷺ: {لا تدخلون الجنة حتى تؤمنوا، ولا تؤمنوا حتى تحابوا، أوَلا أدلكم على شيء إذا فعلتموه تحاببتم؟ أفشوا السلام بينكم}.",
      category = QuizCategory.MANNERS,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Muslim (54)",
      hintEn = "Creates love between hearts and opens the path to Jannah.",
      hintAr = "إفشاء السلام مفتاح المودة وتوثيق عرى الأخوة بين المؤمنين."
    ),
    Question(
      id = "q_man_18",
      questionEn = "What did the Prophet ﷺ state about the person who sponsors an orphan (Kafil al-Yatim)?",
      questionAr = "بماذا بشر النبي ﷺ كافل اليتيم والمنفق عليه في الجنة مشيراً بإصبعيه السبابة والوسطى؟",
      optionsEn = listOf(
        "He will receive palaces of silver only",
        "He will be excused from all duties",
        "He and the Prophet will be like these two fingers in Paradise",
        "He will not meet the Prophet"
      ),
      optionsAr = listOf(
        "له قصور من الفضة فقط",
        "تسقط عنه التكاليف",
        "أَنَا وَكَافِلُ الْيَتِيمِ فِي الْجَنَّةِ هَكَذَا (وَأَشَارَ بِالسَّبَّابَةِ وَالْوُسْطَى)",
        "لا يلقى النبي في الجنة"
      ),
      correctAnswerIndex = 2,
      explanationEn = "The Prophet ﷺ said: 'I and the one who looks after an orphan will be like this in Paradise,' and he held up his index and middle fingers slightly apart.",
      explanationAr = "قال رسول الله ﷺ: {أنا وكافل اليتيم في الجنة هكذا، وأشار بالسبابة والوسطى وفرج بينهما شيئاً}، بياناً لعلو منزلته وقربه الشديد من النبي ﷺ.",
      category = QuizCategory.MANNERS,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari (5304) & Muslim",
      hintEn = "Holding index and middle fingers together in companionship in Paradise.",
      hintAr = "مرافقة النبي ﷺ في الفردوس كقرب السبابة من الوسطى."
    )
  )
}
