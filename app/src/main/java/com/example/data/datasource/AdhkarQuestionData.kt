package com.example.data.datasource

import com.example.data.models.Question
import com.example.data.models.QuestionDifficulty
import com.example.data.models.QuizCategory

object AdhkarQuestionData {
  val questions: List<Question> = listOf(
    Question(
      id = "q_adh_4",
      questionEn = "What is the chief of supplications for forgiveness (Sayyid al-Istighfar) that the Prophet ﷺ taught?",
      questionAr = "ما هو الذكر النبوي الجليل المسمى بـ (سيد الاستغفار) الذي من قاله موقناً به فمات دخل الجنة؟",
      optionsEn = listOf(
        "Astaghfirullahal-Azim wa atubu ilayh",
        "Allahumma Anta Rabbi la ilaha illa Anta khalaqtani wa ana 'abduk...",
        "Rabbana zalamna anfusana",
        "Rabbighfir li wa liwalidayya"
      ),
      optionsAr = listOf(
        "أستغفر الله العظيم وأتوب إليه",
        "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ...",
        "ربنا ظلمنا أنفسنا وإن لم تغفر لنا وترحمنا لنكونن من الخاسرين",
        "رب اغفر لي ولوالدي وللمؤمنين والمؤمنات"
      ),
      correctAnswerIndex = 1,
      explanationEn = "Sayyid al-Istighfar begins: 'O Allah, You are my Lord, there is no deity except You. You created me and I am Your servant...'",
      explanationAr = "سيد الاستغفار أن يقول العبد: {اللهم أنت ربي لا إله إلا أنت خلقتني وأنا عبدك وأنا على عهدك ووعدك ما استطعت...}، ومن قاله موقناً به ومات في يومه أو ليلته فهو من أهل الجنة.",
      category = QuizCategory.ADHKAR,
      difficulty = QuestionDifficulty.MEDIUM,
      source = "Sahih Al-Bukhari (6306)",
      hintEn = "Starts with 'Allahumma Anta Rabbi la ilaha illa Anta'.",
      hintAr = "يبدأ بالاعتراف بالربوبية والعبودية: (اللهم أنت ربي لا إله إلا أنت)."
    ),
    Question(
      id = "q_adh_5",
      questionEn = "What did the Prophet ﷺ describe as a treasure from the treasures of Paradise (Kanz min kunuz al-Jannah)?",
      questionAr = "ما هي الكلمة العظيمة التي أخبر النبي ﷺ أبا موسى الأشعري أنها (كنز من كنوز الجنة)؟",
      optionsEn = listOf("SubhanAllah", "La hawla wa la quwwata illa billah", "Alhamdulillah", "Allahu Akbar"),
      optionsAr = listOf("سبحان الله وبحمده", "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", "الحمد لله رب العالمين", "الله أكبر كبيراً"),
      correctAnswerIndex = 1,
      explanationEn = "The Prophet ﷺ said: 'Shall I not guide you to a treasure from the treasures of Paradise? It is: La hawla wa la quwwata illa billah (There is no power nor strength except with Allah).'",
      explanationAr = "قال رسول الله ﷺ لأبي موسى الأشعري: {ألا أدلك على كلمة هي كنز من كنوز الجنة؟ لا حول ولا قوة إلا بالله}.",
      category = QuizCategory.ADHKAR,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari (4205) & Sahih Muslim",
      hintEn = "Expresses complete reliance on Allah: 'There is no power nor strength except with Allah'.",
      hintAr = "كلمة الحوقلة وإعلان التبرؤ من الحول والقوة إلا بالله تعالى."
    ),
    Question(
      id = "q_adh_6",
      questionEn = "What is the recommended dhikr count said after completing each prescribed prayer: SubhanAllah, Alhamdulillah, Allahu Akbar?",
      questionAr = "كم مرة يُسبح ويحمد ويكبر المسلم دبر كل صلاة مكتوبة كما جاء في الحديث المتفق عليه؟",
      optionsEn = listOf("10 times each", "25 times each", "33 times each", "99 times each"),
      optionsAr = listOf("١٠ مرات لكل منها", "٢٥ مرة", "٣٣ مرة لكل من التسبيح والتحميد والتكبير", "٩٩ مرة معاً"),
      correctAnswerIndex = 2,
      explanationEn = "Saying SubhanAllah (33 times), Alhamdulillah (33 times), Allahu Akbar (33 times), and concluding with the 100th: 'La ilaha illallah wahdahu la sharika lah...' expiates sins even if like foam of the sea.",
      explanationAr = "يسن أن يسبح الله ٣٣، ويحمده ٣٣، ويكبره ٣٣، ويتم المائة بقول: {لا إله إلا الله وحده لا شريك له له الملك وله الحمد وهو على كل شيء قدير} فتُغفر خطاياه وإن كانت مثل زبد البحر.",
      category = QuizCategory.ADHKAR,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Muslim (597)",
      hintEn = "Thirty-three of each, completed with the Kalimah to make 100.",
      hintAr = "ثلاث وثلاثون تسبيحة وتحميدة وتكبيرة، والتمام بالمائة تهليلة."
    ),
    Question(
      id = "q_adh_7",
      questionEn = "What supplication did the Prophet ﷺ teach us to say when leaving our home for complete protection?",
      questionAr = "ماذا يقول المسلم عند خروجه من بيته ليُقال له: (كُفيت وهُديت ووُقيت وتَنَحَّى عنه الشيطان)؟",
      optionsEn = listOf(
        "Subhanal-ladhi sakh-khara lana hadha",
        "Allahumma inni a'udhu bika minal-khubthi wal-khaba'ith",
        "Alhamdulillahilladhi ahyana ba'da ma amatana",
        "Bismillah, tawakkaltu 'alallah, wa la hawla wa la quwwata illa billah"
      ),
      optionsAr = listOf(
        "سبحان الذي سخر لنا هذا وما كنا له مقرنين",
        "اللهم إني أعوذ بك من الخبث والخبائث",
        "الحمد لله الذي أحيانا بعد ما أماتنا وإليه النشور",
        "بِسْمِ اللَّهِ، تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ"
      ),
      correctAnswerIndex = 3,
      explanationEn = "The Prophet ﷺ taught: When a person says 'In the name of Allah, I place my trust in Allah, and there is no might nor power except in Allah', the angels say: You have been guided, defended, and protected.",
      explanationAr = "قال رسول الله ﷺ: {إذا خرج الرجل من بيته فقال: بسم الله، توكلت على الله، لا حول ولا قوة إلا بالله، يقال له حينئذ: هُديت وكُفيت ووُقيت، وتنحى عنه الشيطان}.",
      category = QuizCategory.ADHKAR,
      difficulty = QuestionDifficulty.EASY,
      source = "Sunan Abi Dawud (5095) & At-Tirmidhi",
      hintEn = "Entrusting affairs to Allah upon stepping outside.",
      hintAr = "دعاء التوكل على الله والاستعانة به عند مفارقة المنزل."
    ),
    Question(
      id = "q_adh_8",
      questionEn = "What dua is recited when mounting a vehicle or embarking upon a journey?",
      questionAr = "ما هو الدعاء القرآني المسنون عند ركوب الدابة أو السيارة أو وسيلة السفر؟",
      optionsEn = listOf(
        "Rabbi zidni 'ilma",
        "Hasbunallahu wa ni'mal wakeel",
        "Subhanal-ladhi sakh-khara lana hadha wa ma kunna lahu muqrinin, wa inna ila Rabbina lamunqalibun",
        "Rabbana hab lana min azwajina"
      ),
      optionsAr = listOf(
        "رب زدني علماً",
        "حسبنا الله ونعم الوكيل",
        "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ * وَإِنَّا إِلَىٰ رَبِّنَا لَمُنقَلِبُونَ",
        "ربنا هب لنا من أزواجنا وذرياتنا قرة أعين"
      ),
      correctAnswerIndex = 2,
      explanationEn = "Surah Az-Zukhruf (43:13-14) contains the foundational prayer recited when boarding transport: 'Exalted is He who has subjected this to us, and we could not have [otherwise] subdued it.'",
      explanationAr = "دعاء الركوب والسفر مأخوذ من سورة الزخرف: {سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ * وَإِنَّا إِلَىٰ رَبِّنَا لَمُنقَلِبُونَ}.",
      category = QuizCategory.ADHKAR,
      difficulty = QuestionDifficulty.EASY,
      source = "Surah Az-Zukhruf (43:13-14) & Sahih Muslim (1342)",
      hintEn = "Praises Allah who subjected the transport for us.",
      hintAr = "الآيتان ١٣ و١٤ من سورة الزخرف."
    ),
    Question(
      id = "q_adh_9",
      questionEn = "What is the morning and evening supplication seeking preservation of faith, health, and worldly well-being ('Afiyah)?",
      questionAr = "ما هو الدعاء النبوي الجامع الذي لم يكن يدعه النبي ﷺ صباحاً ومساءً لسؤال العافية في الدين والدنيا والأهل والمال؟",
      optionsEn = listOf(
        "Allahumma barik lana feema razaqtana",
        "Rabbi inni lima anzalta ilayya min khayrin faqeer",
        "Allahumma tahhir qalbi minan-nifaq",
        "Allahumma inni as'alukal-'afwa wal-'afiyah fi deeni wa dunya-ya wa ahli wa mali"
      ),
      optionsAr = listOf(
        "اللهم بارك لنا فيما رزقتنا وقنا عذاب النار",
        "رب إني لما أنزلت إلي من خير فقير",
        "اللهم طهر قلبي من النفاق وعملي من الرياء",
        "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي دِينِي وَدُنْيَايَ وَأَهْلِي وَمَالِي"
      ),
      correctAnswerIndex = 3,
      explanationEn = "Ibn Umar reported that the Messenger of Allah ﷺ never omitted this dua morning and evening: 'O Allah, I ask You for pardon and well-being in my religion, worldly affairs, family, and wealth.'",
      explanationAr = "كان النبي ﷺ لا يدع هذه الكلمات حين يمسي وحين يصبح: {اللهم إني أسألك العافية في الدنيا والآخرة، اللهم إني أسألك العفو والعافية في ديني ودنياي وأهلي ومالي...}.",
      category = QuizCategory.ADHKAR,
      difficulty = QuestionDifficulty.MEDIUM,
      source = "Sunan Abi Dawud (5074) & Sunan Ibn Majah",
      hintEn = "Begins with asking for 'Afw and 'Afiyah in religion, world, family, and wealth.",
      hintAr = "سؤال الله تعالى العفو والعافية وستر العورات وتأمين الروعات."
    ),
    Question(
      id = "q_adh_10",
      questionEn = "What dua is recited upon waking up from sleep in the morning?",
      questionAr = "ما هو الذكر النبوي المأثور الذي يستفتح به المسلم يومه فور استيقاظه من النوم؟",
      optionsEn = listOf(
        "Bismika Allahumma amutu wa ahya",
        "Alhamdulillahil-ladhi ahyana ba'da ma amatana wa ilayhin-nushoor",
        "Alhamdulillahil-ladhi at'amana wa saqana",
        "Subhanaka Allahumma wa bihamdika ashhadu alla ilaha illa Anta"
      ),
      optionsAr = listOf(
        "باسمك اللهم أموت وأحيا",
        "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
        "الحمد لله الذي أطعمنا وسقانا وكفانا وآوانا",
        "سبحانك اللهم وبحمدك أشهد أن لا إله إلا أنت أستغفرك وأتوب إليك"
      ),
      correctAnswerIndex = 1,
      explanationEn = "Upon waking, the Prophet ﷺ used to say: 'Praise be to Allah Who brought us to life after causing us to die, and to Him is the ultimate resurrection.'",
      explanationAr = "كان النبي ﷺ إذا استيقظ من منامه قال: {الحمد لله الذي أحيانا بعد ما أماتنا وإليه النشور}، اعترافاً بنعمة تجدد الحياة.",
      category = QuizCategory.ADHKAR,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari (6312) & Sahih Muslim",
      hintEn = "Praises Allah who revived us after sleep.",
      hintAr = "حمد الله على رد الروح واستيقاظ البدن والتذكير بيوم النشور."
    ),
    Question(
      id = "q_adh_11",
      questionEn = "What is the Dua al-Karb (Supplication for distress and hardship) narrated by Ibn Abbas in Bukhari and Muslim?",
      questionAr = "ما هو (دعاء الكرب) العظيم الذي كان النبي ﷺ يلهج به عند الشدائد والأزمات؟",
      optionsEn = listOf(
        "Allahumma la sahla illa ma ja'altahu sahla",
        "HasbiyAllahu la ilaha illa Huwa 'alayhi tawakkaltu",
        "La ilaha illallahul-'Adheemul-Haleem, La ilaha illallahu Rabbul-'Arshil-'Adheem...",
        "Ya Hayyu Ya Qayyoom bi-rahmatika astagheeth"
      ),
      optionsAr = listOf(
        "اللهم لا سهل إلا ما جعلته سهلاً وأنت تجعل الحزن إذا شئت سهلاً",
        "حسبي الله لا إله إلا هو عليه توكلت وهو رب العرش العظيم",
        "لَا إِلَهَ إِلَّا اللَّهُ الْعَظِيمُ الْحَلِيمُ، لَا إِلَهَ إِلَّا اللَّهُ رَبُّ الْعَرْشِ الْعَظِيمِ...",
        "يا حي يا قيوم برحمتك أستغيث أصلح لي شأني كله"
      ),
      correctAnswerIndex = 2,
      explanationEn = "The Prophet ﷺ used to say at times of grief and distress: 'There is no god but Allah, the Great, the Tolerant. There is no god but Allah, Lord of the Magnificent Throne...'",
      explanationAr = "كان النبي ﷺ يدعو عند الكرب: {لا إله إلا الله العظيم الحليم، لا إله إلا الله رب العرش العظيم، لا إله إلا الله رب السماوات ورب الأرض ورب العرش الكريم}.",
      category = QuizCategory.ADHKAR,
      difficulty = QuestionDifficulty.HARD,
      source = "Sahih Al-Bukhari (6345) & Sahih Muslim (2730)",
      hintEn = "Proclaims the greatness of Allah and His Lordship over the Mighty Throne.",
      hintAr = "تهليل وتوحيد وتمجيد لعظمة الله وحلمه وربوبيته للعرش الكريم."
    ),
    Question(
      id = "q_adh_12",
      questionEn = "What is the closing supplication of any gathering (Kaffarat al-Majlis) that expiates idle talk during that assembly?",
      questionAr = "ما هو دعاء (كفارة المجلس) الذي يكفر ما قد يقع من لغو أو تقصير في الجلسات قبل الانصراف؟",
      optionsEn = listOf(
        "Rabbana taqabbal minna innaka Antas-Sami'ul-'Aleem",
        "Allahumma a'inna 'ala dhikrika wa shukrika",
        "Jazakumullahu khayran katheera",
        "Subhanaka Allahumma wa bihamdika, ash-hadu alla ilaha illa Anta, astaghfiruka wa atubu ilayk"
      ),
      optionsAr = listOf(
        "ربنا تقبل منا إنك أنت السميع العليم",
        "اللهم أعنا على ذكرك وشكرك وحسن عبادتك",
        "سبحان ربك رب العزة عما يصفون وسلام على المرسلين",
        "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا أَنْتَ، أَسْتَغْفِرُكَ وَأَتُوبُ إِلَيْكَ"
      ),
      correctAnswerIndex = 3,
      explanationEn = "The Prophet ﷺ said: 'Whoever sits in a gathering where there was much clamor and says before standing: Subhanaka Allahumma wa bihamdika..., whatever took place in that gathering will be forgiven for him.'",
      explanationAr = "قال رسول الله ﷺ: {من جلس في مجلس فكثر فيه لغطه فقال قبل أن يقوم من مجلسه ذلك: سبحانك اللهم وبحمدك أشهد أن لا إله إلا أنت أستغفرك وأتوب إليك إلا غفر له ما كان في مجلسه ذلك}.",
      category = QuizCategory.ADHKAR,
      difficulty = QuestionDifficulty.EASY,
      source = "Sunan At-Tirmidhi (3433) & Sahih Al-Jami'",
      hintEn = "Subhanaka Allahumma wa bihamdika, ash-hadu alla ilaha illa Anta...",
      hintAr = "يبدأ بالتسبيح والحمد والشهادة بالتوحيد ثم الاستغفار والتوبة."
    ),
    Question(
      id = "q_adh_13",
      questionEn = "What should a believer say when hearing the crowing of a rooster at night or dawn according to the Sunnah?",
      questionAr = "ماذا يُسن للمسلم أن يسأل ربه إذا سمع صياح الديكة بالليل أو الصباح؟",
      optionsEn = listOf(
        "Seek refuge from the devil",
        "Ask Allah for His bounty, for it has seen an angel",
        "Remain completely silent",
        "Say 'Subhanal-Malikil-Quddus'"
      ),
      optionsAr = listOf(
        "يتعوذ بالله من الشيطان الرجيم",
        "يسأل الله من فضله لأنها رأت مَلَكاً",
        "يسكت تماماً ولا يذكر شيئاً",
        "يقول سبحان الملك القدوس"
      ),
      correctAnswerIndex = 1,
      explanationEn = "The Prophet ﷺ instructed: 'When you hear the crowing of roosters, ask Allah for His bounty, for they have seen an angel; and when you hear the braying of a donkey, seek refuge in Allah from Satan.'",
      explanationAr = "قال رسول الله ﷺ: {إذا سمعتم صياح الديكة فاسألوا الله من فضله فإنها رأت مَلَكاً، وإذا سمعتم نهيق الحمار فتعوذوا بالله من الشيطان فإنه رأى شيطاناً}.",
      category = QuizCategory.ADHKAR,
      difficulty = QuestionDifficulty.MEDIUM,
      source = "Sahih Al-Bukhari (3303) & Sahih Muslim",
      hintEn = "Ask Allah for His bounty (fadl).",
      hintAr = "سؤال الله من فضله وإحسانه لأن الديك رأى ملكاً من ملائكة الرحمة."
    ),
    Question(
      id = "q_adh_14",
      questionEn = "What are the two light phrases on the tongue, heavy on the Scales, beloved to the Most Merciful?",
      questionAr = "ما هما الكلمتان الخفيفتان على اللسان، الثقيلتان في الميزان، الحبيبتان إلى الرحمن كما في ختام صحيح البخاري؟",
      optionsEn = listOf(
        "La ilaha illallah, Allahu Akbar",
        "Alhamdulillah, Astaghfirullah",
        "SubhanAllahi wa bihamdih, SubhanAllahil-'Adheem",
        "La hawla wa la quwwata illa billah"
      ),
      optionsAr = listOf(
        "لا إله إلا الله، والله أكبر",
        "الحمد لله، وأستغفر الله",
        "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
        "لا حول ولا قوة إلا بالله العلي العظيم"
      ),
      correctAnswerIndex = 2,
      explanationEn = "The concluding Hadith of Sahih Al-Bukhari states: 'Two words are light on the tongue, heavy on the Scale, beloved to the Most Merciful: SubhanAllahi wa bihamdih, SubhanAllahil-'Adheem.'",
      explanationAr = "ختم الإمام البخاري صحيحه بالحديث الشريف: {كلمتان خفيفتان على اللسان ثقيلتان في الميزان حبيبتان إلى الرحمن: سبحان الله وبحمده سبحان الله العظيم}.",
      category = QuizCategory.ADHKAR,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari (6682) & Sahih Muslim",
      hintEn = "Glorifying Allah and His supreme grandeur.",
      hintAr = "تسبيح الله بحمده مقروناً بتسبيح الله العظيم."
    ),
    Question(
      id = "q_adh_15",
      questionEn = "What is the supplication when visiting graves to greet the deceased believers and remind oneself of the hereafter?",
      questionAr = "ما هو السلام الشرعي والدعاء المأثور عند زيارة القبور والسلام على أهلها من المؤمنين؟",
      optionsEn = listOf(
        "Rabbana la tuzigh quloobana",
        "Allahumma a'inna 'ala sukratil-mawt",
        "Allahumma inni a'udhu bika min fitnatil-qabr",
        "As-salamu 'alaykum ahlad-diyari minal-mu'mineena wal-muslimeen..."
      ),
      optionsAr = listOf(
        "ربنا لا تزغ قلوبنا بعد إذ هديتنا",
        "اللهم أعنا على سكرات الموت",
        "اللهم إني أعوذ بك من فتنة القبر وعذاب القبر",
        "السَّلَامُ عَلَيْكُمْ أَهْلَ الدِّيَارِ مِنَ الْمُؤْمِنِينَ وَالْمُسْلِمِينَ، وَإِنَّا إِنْ شَاءَ اللَّهُ بِكُمْ لَاحِقُونَ..."
      ),
      correctAnswerIndex = 3,
      explanationEn = "The Prophet ﷺ taught companions when visiting the graveyard to say: 'Peace be upon you, O inhabitants of the abodes among believers and Muslims. If Allah wills, we will join you...'",
      explanationAr = "كان النبي ﷺ يعلم أصحابه إذا خرجوا إلى المقابر: {السلام عليكم أهل الديار من المؤمنين والمسلمين، وإنا إن شاء الله بكم للاحقون، نسأل الله لنا ولكم العافية}.",
      category = QuizCategory.ADHKAR,
      difficulty = QuestionDifficulty.MEDIUM,
      source = "Sahih Muslim (975)",
      hintEn = "Greeting them with Salam and praying for their and our safety.",
      hintAr = "إلقاء السلام على المؤمنين والمؤمنات وسؤال العافية للجميع."
    ),
    Question(
      id = "q_adh_16",
      questionEn = "What is the virtue of reciting Ayat al-Kursi (2:255) before going to sleep at night?",
      questionAr = "ما هو الفضل الثابت لمن قرأ آية الكرسي حين يأوي إلى فراشه ليلاً كما ورد في حديث أبي هريرة؟",
      optionsEn = listOf(
        "He will not have to pray Fajr",
        "He will receive immediate gold",
        "A protector from Allah remains with him and no devil can approach him until morning",
        "It replaces reciting Surah Al-Fatihah"
      ),
      optionsAr = listOf(
        "تسقط عنه صلاة الفجر",
        "تُضاعف له أمواله الدنيوية فوراً",
        "لا يزال عليه من الله حافظ ولا يقربه شيطان حتى يُصبح",
        "تجزئ عن قراءة الفاتحة"
      ),
      correctAnswerIndex = 2,
      explanationEn = "In Sahih Al-Bukhari, Abu Hurairah reported that whoever recites Ayat al-Kursi before sleeping: 'A guardian from Allah will remain over you, and no devil will come near you until morning.'",
      explanationAr = "ورد في صحيح البخاري عن النبي ﷺ تصديقاً لما جاء لأبي هريرة: {إذا أويت إلى فراشك فاقرأ آية الكرسي، فإنه لن يزال عليك من الله حافظ ولا يقربك شيطان حتى تصبح}.",
      category = QuizCategory.ADHKAR,
      difficulty = QuestionDifficulty.EASY,
      source = "Sahih Al-Bukhari (2311)",
      hintEn = "A divine guardian protects until morning.",
      hintAr = "حفظ إلهي شامل من وساوس وأذى الشياطين حتى طلوع الفجر."
    ),
    Question(
      id = "q_adh_17",
      questionEn = "What did the Prophet ﷺ teach Mu'adh ibn Jabal (may Allah be pleased with him) never to omit at the end of every prayer?",
      questionAr = "ما هي الوصية الجامعة التي أوصى بها النبي ﷺ معاذ بن جبل ألا يدعها دبر كل صلاة لحبه له؟",
      optionsEn = listOf(
        "Allahumma ajirni minan-nar",
        "Allahumma a'inni 'ala dhikrika wa shukrika wa husni 'ibadatik",
        "Rabbi hab li hukma",
        "Allahumma arzuqni shahadah"
      ),
      optionsAr = listOf(
        "اللهم أجرني من النار سبع مرات",
        "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ",
        "رب هب لي حكماً وألحقني بالصالحين",
        "اللهم ارزقني الشهادة في سبيلك"
      ),
      correctAnswerIndex = 1,
      explanationEn = "The Prophet ﷺ took Mu'adh's hand and said: 'By Allah I love you, so do not forget to say after every prayer: O Allah, help me to remember You, give thanks to You, and worship You properly.'",
      explanationAr = "أخذ النبي ﷺ بيد معاذ وقال: {يا معاذ والله إني لأحبك، أوصيك يا معاذ لا تدعن في دبر كل صلاة تقول: اللهم أعني على ذكرك وشكرك وحسن عبادتك}.",
      category = QuizCategory.ADHKAR,
      difficulty = QuestionDifficulty.EASY,
      source = "Sunan Abi Dawud (1522) & Sunan An-Nasa'i",
      hintEn = "Asking help for remembrance, gratitude, and good worship.",
      hintAr = "طلب العون الرباني على إدامة الذكر وشكر النعم وإتقان العبادة."
    ),
    Question(
      id = "q_adh_18",
      questionEn = "What is the comprehensive Dua that the Prophet ﷺ made most frequently, asking for goodness in this world and the hereafter?",
      questionAr = "ما هو أكثر دعاء كان النبي ﷺ يلهج به في طوافه ودبر صلواته وهو أجمع دعاء في القرآن؟",
      optionsEn = listOf(
        "Rabbana atina fid-dunya hasanatan wa fil-akhirati hasanatan wa qina 'adhaban-nar",
        "Rabbi inni lima anzalta ilayya min khayrin faqeer",
        "Rabbij'alni muqeemas-salati wa min dhurriyyati",
        "Allahumma inni as'aluka huda wat-tuqa wal-'afafa wal-ghina"
      ),
      optionsAr = listOf(
        "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
        "رب إني لما أنزلت إلي من خير فقير",
        "رب اجعلني مقيم الصلاة ومن ذريتي ربنا وتقبل دعاء",
        "اللهم إني أسألك الهدى والتقى والعفاف والغنى"
      ),
      correctAnswerIndex = 0,
      explanationEn = "Anas (may Allah be pleased with him) reported: 'The supplication the Prophet ﷺ made most frequently was: Rabbana atina fid-dunya hasanah wa fil-akhirati hasanah wa qina 'adhaban-nar' (2:201).",
      explanationAr = "عن أنس رضي الله عنه قال: كان أكثر دعاء النبي ﷺ: {رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ} لجمعه لخيري الدنيا والآخرة والوقاية من النار.",
      category = QuizCategory.ADHKAR,
      difficulty = QuestionDifficulty.EASY,
      source = "Surah Al-Baqarah (2:201) & Sahih Al-Bukhari (6389)",
      hintEn = "Surah Al-Baqarah verse 201.",
      hintAr = "الآية ٢٠١ من سورة البقرة وتجمع سعادة الدارين."
    )
  )
}
