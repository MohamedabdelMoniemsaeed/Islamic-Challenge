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
      optionsEn = listOf("Your father", "Your eldest brother", "Your closest teacher", "Your mother"),
      optionsAr = listOf("أبوك", "أخوك الأكبر", "معلمك", "أمك"),
      correctAnswerIndex = 3,
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
        "Needed only in business trade",
        "Brings worldly reputation only",
        "Leads to virtue and Paradise",
        "Optional when talking to others"
      ),
      optionsAr = listOf(
        "الصدق مطلوب في البيع فقط",
        "الصدق يجلب الشهرة الدنيوية",
        "الصدق يهدي إلى البر والجنة",
        "الصدق مستحب في المجالس فقط"
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
      optionsEn = listOf("Do not sleep early", "Do not travel alone", "Do not eat excess", "Do not get angry"),
      optionsAr = listOf("لا تنم في النهار", "لا تسافر وحدك", "لا تكثر الطعام", "لا تغضب"),
      correctAnswerIndex = 3,
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
        "Modesty weakens personality",
        "Modesty brings only good",
        "Modesty carries no reward",
        "Modesty fits private life only"
      ),
      optionsAr = listOf(
        "الحياء يضعف شخصية المؤمن",
        "الحياء لا يأتي إلا بخير",
        "الحياء لا ثواب عليه للناس",
        "الحياء خاص بالخلوات فقط"
      ),
      correctAnswerIndex = 1,
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
        "Like throwing stones in water",
        "Like eating your dead brother",
        "Like walking without a guide"
      ),
      optionsAr = listOf(
        "كإحراق شجرة مثمرة",
        "كإلقاء حجر في بئر عميقة",
        "كأكل لحم الأخ الميت",
        "كالمشي في الصحراء بلا زاد"
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
        "He loses only worldly wealth",
        "He misses rewards of charity",
        "He faces a minor warning only",
        "He will not enter Paradise"
      ),
      optionsAr = listOf(
        "تنقص بركة ماله في الدنيا",
        "يحرم من مضاعفة الصدقات",
        "ينال عقوبة دنيوية بسيطة فقط",
        "لا يدخل الجنة قاطع رحم"
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
      optionsEn = listOf("Golden palaces", "Guarantee of Paradise", "Lifelong health", "Treasures of Rome"),
      optionsAr = listOf("قصور من الياقوت", "ضمنت له الجنة", "العافية من الأمراض", "كنوز كسرى وقيصر"),
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
        "All three speak together loudly",
        "Two do not whisper leaving third",
        "The third person leaves right away",
        "The youngest remains fully silent"
      ),
      optionsAr = listOf(
        "أن يتحدث الجميع بصوت مرتفع",
        "ألا يتناجى اثنان دون الثالث",
        "أن يغادر الشخص الثالث المكان",
        "أن يسكت الأصغر في المجلس"
      ),
      correctAnswerIndex = 1,
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
        "Lies, breaks promises, betrays trust",
        "Dresses white, walks fast, wakes early",
        "Speaks little, fasts often, prays long",
        "Travels often, reads books, works hard"
      ),
      optionsAr = listOf(
        "إذا حدث كذب وإذا وعد أخلف وإذا اؤتمن خان",
        "يلبس البياض ويسرع في المشي ويقوم الليل",
        "يطيل الصلاة ويقلل الكلام ويكثر الصدقة",
        "يسافر في الأرض ويقرأ الكتب ويصوم النهار"
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
        "Eat with both hands from the center",
        "Eat standing and drink quickly",
        "Begin eating from the far side",
        "Say Bismillah and eat with right hand"
      ),
      optionsAr = listOf(
        "الأكل باليدين ومن وسط الإناء",
        "الأكل والشرب في وضع الوقوف",
        "الأكل من الجانب البعيد عنك",
        "التسمية والأكل باليمين ومما يليك"
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
        "A duty limited to city workers",
        "A neutral habit with no reward",
        "An act of charity and part of faith",
        "An act required only during Eid"
      ),
      optionsAr = listOf(
        "عمل خاص بعمال النظافة فقط",
        "عادة اجتماعية لا ثواب فيها",
        "صدقة وشعبة من شعب الإيمان",
        "أمر مستحب في الأعياد فقط"
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
      optionsEn = listOf("Once then leave", "Three times max", "Ten times max", "Keep on knocking"),
      optionsAr = listOf("مرة واحدة ثم ينصرف", "ثلاث مرات فقط", "عشر مرات فقط", "الاستمرار بلا توقف"),
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
        "Who defeats men in wrestling",
        "Who lifts heavy mountain rocks",
        "Who owns weapons and wealth",
        "Who controls himself when angry"
      ),
      optionsAr = listOf(
        "الذي يصرع الرجال في المصارعة",
        "الذي يحمل الأثقال الضخمة",
        "الذي يملك الأسلحة والقوة فقط",
        "الذي يملك نفسه عند الغضب"
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
        "Brings mutual love and Paradise",
        "Rewarded only among close kin",
        "A custom limited to celebrations",
        "Said only during daylight hours"
      ),
      optionsAr = listOf(
        "يؤلف القلوب ويدخل الجنة",
        "يثاب عليه مع الأقارب فقط",
        "عادة اجتماعية في المناسبات",
        "يقال في النهار دون الليل"
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
        "Palaces built of pure silver",
        "Exempted from daily duties",
        "Close to the Prophet in Paradise",
        "Forgiven only for minor slips"
      ),
      optionsAr = listOf(
        "له قصور من الفضة في الدنيا",
        "تسقط عنه سائر التكاليف",
        "قريب من النبي في الجنة",
        "يعفى من الصدقة والزكاة"
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
