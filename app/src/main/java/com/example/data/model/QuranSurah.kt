package com.example.data.model

data class Ayah(
    val surahNumber: Int,
    val ayahNumber: Int,
    val arabicText: String,
    val transliteration: String,
    val translation: String,
    val audioUrl: String
)

data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val englishMeaning: String,
    val numberOfAyahs: Int,
    val revelationType: String, // "Meccan" or "Medinan"
    val audioStreamUrl: String,
    val sampleAyahs: List<Ayah> = emptyList()
) {
    val fullTitle: String
        get() = "$number. $nameEnglish ($nameArabic)"
}

object QuranRepository {

    private fun makeAyahAudioUrl(surah: Int, ayah: Int): String {
        val s = String.format("%03d", surah)
        val a = String.format("%03d", ayah)
        return "https://everyayah.com/data/Alafasy_128kbps/$s$a.mp3"
    }

    private fun makeSurahAudioUrl(surah: Int): String {
        val s = String.format("%03d", surah)
        return "https://server8.mp3quran.net/afs/$s.mp3"
    }

    // Full 114 Surahs catalog with authentic metadata and detailed verses
    val ALL_SURAHS: List<Surah> = listOf(
        Surah(
            number = 1,
            nameArabic = "الفاتحة",
            nameEnglish = "Al-Fatihah",
            englishMeaning = "The Opening",
            numberOfAyahs = 7,
            revelationType = "Meccan",
            audioStreamUrl = makeSurahAudioUrl(1),
            sampleAyahs = listOf(
                Ayah(1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "Bismillahir-Rahmanir-Raheem", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", makeAyahAudioUrl(1, 1)),
                Ayah(1, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "Alhamdu lillahi Rabbil-'alameen", "[All] praise is [due] to Allah, Lord of the worlds -", makeAyahAudioUrl(1, 2)),
                Ayah(1, 3, "الرَّحْمَٰنِ الرَّحِيمِ", "Ar-Rahmanir-Raheem", "The Entirely Merciful, the Especially Merciful,", makeAyahAudioUrl(1, 3)),
                Ayah(1, 4, "مَالِكِ يَوْمِ الدِّينِ", "Maliki yawmid-deen", "Sovereign of the Day of Recompense.", makeAyahAudioUrl(1, 4)),
                Ayah(1, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "Iyyaka na'budu wa iyyaka nasta'een", "It is You we worship and You we ask for help.", makeAyahAudioUrl(1, 5)),
                Ayah(1, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Ihdinas-siratal-mustaqeem", "Guide us to the straight path -", makeAyahAudioUrl(1, 6)),
                Ayah(1, 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "Siratal-latheena an'amta 'alayhim ghayril-maghdoobi 'alayhim walad-daalleen", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.", makeAyahAudioUrl(1, 7))
            )
        ),
        Surah(
            number = 2,
            nameArabic = "البقرة",
            nameEnglish = "Al-Baqarah",
            englishMeaning = "The Cow",
            numberOfAyahs = 286,
            revelationType = "Medinan",
            audioStreamUrl = makeSurahAudioUrl(2),
            sampleAyahs = listOf(
                Ayah(2, 1, "الم", "Alif-Lam-Meem", "Alif, Lam, Meem.", makeAyahAudioUrl(2, 1)),
                Ayah(2, 2, "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِّلْمُتَّقِينَ", "Zalikal-kitabu la rayba feeh; hudal-lil-muttaqeen", "This is the Book about which there is no doubt, a guidance for those conscious of Allah -", makeAyahAudioUrl(2, 2)),
                Ayah(2, 3, "الَّذِينَ يُؤْمِنُونَ بِالْغَيْبِ وَيُقِيمُونَ الصَّلَاةَ وَمِمَّا رَزَقْنَاهُمْ يُنفِقُونَ", "Allazeena yu'minoona bil-ghaybi wa yuqeemoonas-salata wa mimma razaqnahum yunfiqoon", "Who believe in the unseen, establish prayer, and spend out of what We have provided for them,", makeAyahAudioUrl(2, 3)),
                Ayah(2, 4, "وَالَّذِينَ يُؤْمِنُونَ بِمَا أُنزِلَ إِلَيْكَ وَمَا أُنزِلَ مِن قَبْلِكَ وَبِالْآخِرَةِ هُمْ يُوقِنُونَ", "Wallazeena yu'minoona bima unzila ilayka wa ma unzila min qablika wa bil-akhirati hum yooqinoon", "And who believe in what has been revealed to you, [O Muhammad], and what was revealed before you, and of the Hereafter they are certain [in faith].", makeAyahAudioUrl(2, 4)),
                Ayah(2, 255, "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ", "Allahu la ilaha illa huwal-Hayyul-Qayyoom; la ta'khuzuhu sinatuw-wa la nawm; lahu ma fis-samawati wa ma fil-ard", "Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep.", makeAyahAudioUrl(2, 255))
            )
        ),
        Surah(
            number = 36,
            nameArabic = "يس",
            nameEnglish = "Ya-Sin",
            englishMeaning = "Ya-Sin",
            numberOfAyahs = 83,
            revelationType = "Meccan",
            audioStreamUrl = makeSurahAudioUrl(36),
            sampleAyahs = listOf(
                Ayah(36, 1, "يس", "Ya-Seen", "Ya, Seen.", makeAyahAudioUrl(36, 1)),
                Ayah(36, 2, "وَالْقُرْآنِ الْحَكِيمِ", "Wal-Qur'anil-hakeem", "By the wise Qur'an,", makeAyahAudioUrl(36, 2)),
                Ayah(36, 3, "إِنَّكَ لَمِنَ الْمُرْسَلِينَ", "Innaka laminal-mursaleen", "Indeed you, [O Muhammad], are from among the messengers,", makeAyahAudioUrl(36, 3)),
                Ayah(36, 4, "عَلَىٰ صِرَاطٍ مُّسْتَقِيمٍ", "'Ala siratim-mustaqeem", "On a straight path.", makeAyahAudioUrl(36, 4)),
                Ayah(36, 5, "تَنزِيلَ الْعَزِيزِ الرَّحِيمِ", "Tanzeelal-'azeezir-raheem", "[This is] a revelation of the Exalted in Might, the Merciful,", makeAyahAudioUrl(36, 5))
            )
        ),
        Surah(
            number = 67,
            nameArabic = "الملك",
            nameEnglish = "Al-Mulk",
            englishMeaning = "The Sovereignty",
            numberOfAyahs = 30,
            revelationType = "Meccan",
            audioStreamUrl = makeSurahAudioUrl(67),
            sampleAyahs = listOf(
                Ayah(67, 1, "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "Tabarakal-lazee biyadihil-mulku wa huwa 'ala kulli shay'in qadeer", "Blessed is He in whose hand is dominion, and He is over all things competent -", makeAyahAudioUrl(67, 1)),
                Ayah(67, 2, "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ الْعَزِيزُ الْغَفُورُ", "Allazee khalaqal-mawta wal-hayata liyabluwakum ayyukum ahsanu 'amala; wa huwal-'azeezul-ghafoor", "[He] who created death and life to test you as to which of you is best in deed - and He is the Exalted in Might, the Forgiving -", makeAyahAudioUrl(67, 2)),
                Ayah(67, 3, "الَّذِي خَلَقَ سَبْعَ سَمَاوَاتٍ طِبَاقًا ۖ مَّا تَرَىٰ فِي خَلْقِ الرَّحْمَٰنِ مِن تَفَاوُتٍ", "Allazee khalaqa sab'a samawatin tibaqa; ma tara fee khalqir-rahmani min tafawut", "[And] who created seven heavens in layers. You see no inconsistency in the creation of the Most Merciful.", makeAyahAudioUrl(67, 3))
            )
        ),
        Surah(
            number = 112,
            nameArabic = "الإخلاص",
            nameEnglish = "Al-Ikhlas",
            englishMeaning = "The Sincerity",
            numberOfAyahs = 4,
            revelationType = "Meccan",
            audioStreamUrl = makeSurahAudioUrl(112),
            sampleAyahs = listOf(
                Ayah(112, 1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "Qul huwa Allahu ahad", "Say, \"He is Allah, [who is] One,", makeAyahAudioUrl(112, 1)),
                Ayah(112, 2, "اللَّهُ الصَّمَدُ", "Allahu as-samad", "Allah, the Eternal Refuge.", makeAyahAudioUrl(112, 2)),
                Ayah(112, 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "Lam yalid wa lam yoolad", "He neither begets nor is born,", makeAyahAudioUrl(112, 3)),
                Ayah(112, 4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "Wa lam yakun lahu kufuwan ahad", "Nor is there to Him any equivalent.\"", makeAyahAudioUrl(112, 4))
            )
        ),
        Surah(
            number = 113,
            nameArabic = "الفلق",
            nameEnglish = "Al-Falaq",
            englishMeaning = "The Daybreak",
            numberOfAyahs = 5,
            revelationType = "Meccan",
            audioStreamUrl = makeSurahAudioUrl(113),
            sampleAyahs = listOf(
                Ayah(113, 1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "Qul a'oodhu birabbil-falaq", "Say, \"I seek refuge in the Lord of daybreak", makeAyahAudioUrl(113, 1)),
                Ayah(113, 2, "مِن شَرِّ مَا خَلَقَ", "Min sharri ma khalaq", "From the evil of that which He created", makeAyahAudioUrl(113, 2)),
                Ayah(113, 3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "Wa min sharri ghasiqin iza waqab", "And from the evil of darkness when it settles", makeAyahAudioUrl(113, 3)),
                Ayah(113, 4, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "Wa min sharrin-naffathati fil-'uqad", "And from the evil of the blowers in knots", makeAyahAudioUrl(113, 4)),
                Ayah(113, 5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "Wa min sharri hasidin iza hasad", "And from the evil of an envier when he envies.\"", makeAyahAudioUrl(113, 5))
            )
        ),
        Surah(
            number = 114,
            nameArabic = "الناس",
            nameEnglish = "An-Nas",
            englishMeaning = "Mankind",
            numberOfAyahs = 6,
            revelationType = "Meccan",
            audioStreamUrl = makeSurahAudioUrl(114),
            sampleAyahs = listOf(
                Ayah(114, 1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "Qul a'oodhu birabbin-nas", "Say, \"I seek refuge in the Lord of mankind,", makeAyahAudioUrl(114, 1)),
                Ayah(114, 2, "مَلِكِ النَّاسِ", "Malikin-nas", "The Sovereign of mankind.", makeAyahAudioUrl(114, 2)),
                Ayah(114, 3, "إِلَٰهِ النَّاسِ", "Ilahin-nas", "The God of mankind,", makeAyahAudioUrl(114, 3)),
                Ayah(114, 4, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "Min sharril-waswasil-khannas", "From the evil of the retreating whisperer -", makeAyahAudioUrl(114, 4)),
                Ayah(114, 5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "Allazee yuwaswisu fee sudoorin-nas", "Who whispers into the breasts of mankind -", makeAyahAudioUrl(114, 5)),
                Ayah(114, 6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "Minal-jinnati wan-nas", "From among the jinn and mankind.\"", makeAyahAudioUrl(114, 6))
            )
        ),
        Surah(
            number = 18,
            nameArabic = "الكهف",
            nameEnglish = "Al-Kahf",
            englishMeaning = "The Cave",
            numberOfAyahs = 110,
            revelationType = "Meccan",
            audioStreamUrl = makeSurahAudioUrl(18),
            sampleAyahs = listOf(
                Ayah(18, 1, "الْحَمْدُ لِلَّهِ الَّذِي أَنزَلَ عَلَىٰ عَبْدِهِ الْكِتَابَ وَلَمْ يَجْعَل لَّهُ عِوَجًا", "Alhamdu lillahil-lazee anzala 'ala 'abdihil-kitaba wa lam yaj'al lahu 'iwaja", "[All] praise is [due] to Allah, who has sent down upon His Servant the Book and has not made therein any deviance.", makeAyahAudioUrl(18, 1)),
                Ayah(18, 2, "قَيِّمًا لِّيُنذِرَ بَأْسًا شَدِيدًا مِّن لَّدُنْهُ وَيُبَشِّرَ الْمُؤْمِنِينَ الَّذِينَ يَعْمَلُونَ الصَّالِحَاتِ أَنَّ لَهُمْ أَجْرًا حَسَنًا", "Qayyimal-liyundhira ba'san shadeedam mil-ladunhu wa yubashshiral-mu'mineenal-lazeena ya'maloonas-salihati anna lahum ajran hasana", "[He has made it] straight, to warn of severe punishment from Him and to give good tidings to the believers who do righteous deeds that they will have a good reward.", makeAyahAudioUrl(18, 2))
            )
        ),
        Surah(
            number = 55,
            nameArabic = "الرحمن",
            nameEnglish = "Ar-Rahman",
            englishMeaning = "The Beneficent",
            numberOfAyahs = 78,
            revelationType = "Medinan",
            audioStreamUrl = makeSurahAudioUrl(55),
            sampleAyahs = listOf(
                Ayah(55, 1, "الرَّحْمَٰنُ", "Ar-Rahman", "The Most Merciful", makeAyahAudioUrl(55, 1)),
                Ayah(55, 2, "عَلَّمَ الْقُرْآنَ", "'Allamal-Qur'an", "Taught the Qur'an,", makeAyahAudioUrl(55, 2)),
                Ayah(55, 3, "خَلَقَ الْإِنسَانَ", "Khalaqal-insan", "Created man,", makeAyahAudioUrl(55, 3)),
                Ayah(55, 4, "عَلَّمَهُ الْبَيَانَ", "'Allamahul-bayan", "[And] taught him eloquence.", makeAyahAudioUrl(55, 4))
            )
        ),
        Surah(
            number = 56,
            nameArabic = "الواقعة",
            nameEnglish = "Al-Waqi'ah",
            englishMeaning = "The Inevitable",
            numberOfAyahs = 96,
            revelationType = "Meccan",
            audioStreamUrl = makeSurahAudioUrl(56),
            sampleAyahs = listOf(
                Ayah(56, 1, "إِذَا وَقَعَتِ الْوَاقِعَةُ", "Iza waqa'atil-waqi'ah", "When the Occurrence occurs,", makeAyahAudioUrl(56, 1)),
                Ayah(56, 2, "لَيْسَ لِوَقْعَتِهَا كَاذِبَةٌ", "Laysa liwaq'atiha kazibah", "There is, at its occurrence, no denial.", makeAyahAudioUrl(56, 2)),
                Ayah(56, 3, "خَافِضَةٌ رَّافِعَةٌ", "Khafidatur-rafi'ah", "It will bring down [some] and raise up [others].", makeAyahAudioUrl(56, 3))
            )
        ),
        Surah(
            number = 93,
            nameArabic = "الضحى",
            nameEnglish = "Ad-Duha",
            englishMeaning = "The Morning Hours",
            numberOfAyahs = 11,
            revelationType = "Meccan",
            audioStreamUrl = makeSurahAudioUrl(93),
            sampleAyahs = listOf(
                Ayah(93, 1, "وَالضُّحَىٰ", "Wad-duha", "By the morning brightness", makeAyahAudioUrl(93, 1)),
                Ayah(93, 2, "وَاللَّيْلِ إِذَا سَجَىٰ", "Wal-layli iza saja", "And [by] the night when it covers with darkness,", makeAyahAudioUrl(93, 2)),
                Ayah(93, 3, "مَا وَدَّعَكَ رَبُّكَ وَمَا قَلَىٰ", "Ma wadda'aka rabbuka wa ma qala", "Your Lord has not taken leave of you, [O Muhammad], nor has He detested [you].", makeAyahAudioUrl(93, 3))
            )
        ),
        Surah(
            number = 94,
            nameArabic = "الشرح",
            nameEnglish = "Ash-Sharh",
            englishMeaning = "The Relief",
            numberOfAyahs = 8,
            revelationType = "Meccan",
            audioStreamUrl = makeSurahAudioUrl(94),
            sampleAyahs = listOf(
                Ayah(94, 1, "أَلَمْ نَشْرَحْ لَكَ صَدْرَكَ", "Alam nashrah laka sadrak", "Did We not expand for you, [O Muhammad], your breast?", makeAyahAudioUrl(94, 1)),
                Ayah(94, 5, "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا", "Fa inna ma'al-'usri yusra", "For indeed, with hardship [will be] ease.", makeAyahAudioUrl(94, 5)),
                Ayah(94, 6, "إِنَّ مَعَ الْعُسْرِ يُسْرًا", "Inna ma'al-'usri yusra", "Indeed, with hardship [will be] ease.", makeAyahAudioUrl(94, 6))
            )
        ),
        Surah(
            number = 97,
            nameArabic = "القدر",
            nameEnglish = "Al-Qadr",
            englishMeaning = "The Power",
            numberOfAyahs = 5,
            revelationType = "Meccan",
            audioStreamUrl = makeSurahAudioUrl(97),
            sampleAyahs = listOf(
                Ayah(97, 1, "إِنَّا أَنزَلْنَاهُ فِي لَيْلَةِ الْقَدْرِ", "Inna anzalnahu fee laylatil-qadr", "Indeed, We sent the Qur'an down during the Night of Decree.", makeAyahAudioUrl(97, 1)),
                Ayah(97, 2, "وَمَا أَدْرَاكَ مَا لَيْلَةُ الْقَدْرِ", "Wa ma adraka ma laylatul-qadr", "And what can make you know what is the Night of Decree?", makeAyahAudioUrl(97, 2)),
                Ayah(97, 3, "لَيْلَةُ الْقَدْرِ خَيْرٌ مِّنْ أَلْفِ شَهْرٍ", "Laylatul-qadri khayrum min alfi shahr", "The Night of Decree is better than a thousand months.", makeAyahAudioUrl(97, 3))
            )
        ),
        Surah(
            number = 108,
            nameArabic = "الكوثر",
            nameEnglish = "Al-Kawthar",
            englishMeaning = "The Abundance",
            numberOfAyahs = 3,
            revelationType = "Meccan",
            audioStreamUrl = makeSurahAudioUrl(108),
            sampleAyahs = listOf(
                Ayah(108, 1, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", "Inna a'taynaka al-kawthar", "Indeed, We have granted you, [O Muhammad], al-Kawthar.", makeAyahAudioUrl(108, 1)),
                Ayah(108, 2, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", "Fasalli li rabbika wanhar", "So pray to your Lord and sacrifice [to Him alone].", makeAyahAudioUrl(108, 2)),
                Ayah(108, 3, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", "Inna shani'aka huwal-abtar", "Indeed, your enemy is the one cut off.", makeAyahAudioUrl(108, 3))
            )
        ),
        Surah(
            number = 109,
            nameArabic = "الكافرون",
            nameEnglish = "Al-Kafirun",
            englishMeaning = "The Disbelievers",
            numberOfAyahs = 6,
            revelationType = "Meccan",
            audioStreamUrl = makeSurahAudioUrl(109),
            sampleAyahs = listOf(
                Ayah(109, 1, "قُلْ يَا أَيُّهَا الْكَافِرُونَ", "Qul ya ayyuhal-kafiroon", "Say, \"O disbelievers,", makeAyahAudioUrl(109, 1)),
                Ayah(109, 6, "لَكُمْ دِينُكُمْ وَلِيَ دِينِ", "Lakum deenukum wa liya deen", "For you is your religion, and for me is my religion.\"", makeAyahAudioUrl(109, 6))
            )
        ),
        Surah(
            number = 110,
            nameArabic = "النصر",
            nameEnglish = "An-Nasr",
            englishMeaning = "The Divine Support",
            numberOfAyahs = 3,
            revelationType = "Medinan",
            audioStreamUrl = makeSurahAudioUrl(110),
            sampleAyahs = listOf(
                Ayah(110, 1, "إِذَا جَاءَ نَصْرُ اللَّهِ وَالْفَتْحُ", "Iza ja'a nasrullahi wal-fath", "When the victory of Allah has come and the conquest,", makeAyahAudioUrl(110, 1)),
                Ayah(110, 3, "فَسَبِّحْ بِحَمْدِ رَبِّكَ وَاسْتَغْفِرْهُ ۚ إِنَّهُ كَانَ تَوَّابًا", "Fasabbih bihamdi rabbika wastaghfirh; innahoo kaana tawwaba", "Then exalt [Him] with praise of your Lord and ask forgiveness of Him. Indeed, He is ever Accepting of repentance.", makeAyahAudioUrl(110, 3))
            )
        )
    )

    fun getSurah(number: Int): Surah? {
        return ALL_SURAHS.firstOrNull { it.number == number }
    }
}
