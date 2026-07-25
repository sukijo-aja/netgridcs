package com.mosleemapp.app.utils.app;

public class SurahTranslationHelper {

    private static final String[] INDONESIAN_TRANSLATIONS = {
        "Pembukaan",            // 1. Al-Fatiha
        "Sapi Betina",          // 2. Al-Baqarah
        "Keluarga 'Imran",      // 3. Ali 'Imran
        "Wanita",               // 4. An-Nisa'
        "Hidangan",             // 5. Al-Ma'idah
        "Binatang Ternak",      // 6. Al-An'am
        "Tempat Tertinggi",     // 7. Al-A'raf
        "Rampasan Perang",      // 8. Al-Anfal
        "Pengampunan",          // 9. At-Tawbah
        "Yunus",                // 10. Yunus
        "Hud",                  // 11. Hud
        "Yusuf",                // 12. Yusuf
        "Guruh",                // 13. Ar-Ra'd
        "Ibrahim",              // 14. Ibrahim
        "Bukit Berbatu",        // 15. Al-Hijr
        "Lebah",                // 16. An-Nahl
        "Perjalanan Malam",     // 17. Al-Isra'
        "Goa",                  // 18. Al-Kahf
        "Maryam",               // 19. Maryam
        "Taha",                 // 20. Taha
        "Para Nabi",            // 21. Al-Anbiya'
        "Haji",                 // 22. Al-Hajj
        "Orang-Orang Mukmin",   // 23. Al-Mu'minun
        "Cahaya",               // 24. An-Nur
        "Pembeda",              // 25. Al-Furqan
        "Penyair",              // 26. Asy-Syu'ara'
        "Semut",                // 27. An-Naml
        "Kisahan",              // 28. Al-Qasas
        "Laba-laba",            // 29. Al-'Ankabut
        "Bangsa Romawi",        // 30. Ar-Rum
        "Luqman",               // 31. Luqman
        "Sujud",                // 32. As-Sajdah
        "Golongan Bersekutu",   // 33. Al-Ahzab
        "Saba'",                // 34. Saba'
        "Maha Pencipta",        // 35. Fatir
        "Yasin",                // 36. Yasin
        "Barisan",              // 37. As-Saffat
        "Sad",                  // 38. Sad
        "Rombongan",            // 39. Az-Zumar
        "Maha Pengampun",       // 40. Ghafir
        "Dijelaskan",           // 41. Fussilat
        "Musyawarah",           // 42. Asy-Syura
        "Perhiasan",            // 43. Az-Zukhruf
        "Kabut",                // 44. Ad-Dukhan
        "Berlutut",             // 45. Al-Jasiyah
        "Bukit Pasir",          // 46. Al-Ahqaf
        "Muhammad",             // 47. Muhammad
        "Kemenangan",           // 48. Al-Fath
        "Kamar-kamar",          // 49. Al-Hujurat
        "Qaf",                  // 50. Qaf
        "Angin Menerbangkan",   // 51. Az-Zariyat
        "Bukit Tursina",        // 52. At-Tur
        "Bintang",              // 53. An-Najm
        "Bulan",                // 54. Al-Qamar
        "Maha Pengasih",        // 55. Ar-Rahman
        "Hari Kiamat",          // 56. Al-Waqi'ah
        "Besi",                 // 57. Al-Hadid
        "Gugatan",              // 58. Al-Mujadilah
        "Pengusiran",           // 59. Al-Hasyr
        "Wanita Diuji",         // 60. Al-Mumtahanah
        "Barisan",              // 61. As-Saff
        "Hari Jumat",           // 62. Al-Jumu'ah
        "Orang Munafik",        // 63. Al-Munafiqun
        "Pengungkapan Kesalahan",// 64. At-Tagabun
        "Talak",                // 65. At-Talaq
        "Pengharaman",          // 66. At-Tahrim
        "Kerajaan",             // 67. Al-Mulk
        "Pena",                 // 68. Al-Qalam
        "Kebenaran Pasti",      // 69. Al-Haqqah
        "Tempat Naik",          // 70. Al-Ma'arij
        "Nuh",                  // 71. Nuh
        "Jin",                  // 72. Al-Jinn
        "Orang Berselimut",     // 73. Al-Muzzammil
        "Orang Berkemul",       // 74. Al-Muddassir
        "Hari Kiamat",          // 75. Al-Qiyamah
        "Manusia",              // 76. Al-Insan
        "Malaikat Utusan",      // 77. Al-Mursalat
        "Berita Besar",         // 78. An-Naba'
        "Malaikat Pencabut",    // 79. An-Nazi'at
        "Bermuka Masam",        // 80. 'Abasa
        "Penggulungan",         // 81. At-Takwir
        "Terbelah",             // 82. Al-Infitar
        "Orang Curang",         // 83. Al-Mutaffifin
        "Terbelah",             // 84. Al-Insyiqaq
        "Gugusan Bintang",      // 85. Al-Buruj
        "Datang Malam Hari",    // 86. At-Tariq
        "Maha Tinggi",          // 87. Al-A'la
        "Hari Kiamat",          // 88. Al-Gasyiyah
        "Fajar",                // 89. Al-Fajr
        "Negeri",               // 90. Al-Balad
        "Matahari",             // 91. Asy-Syams
        "Malam",                // 92. Al-Lail
        "Dhuha",                // 93. Ad-Duha
        "Kelapangan",           // 94. Asy-Syarh
        "Buah Tin",             // 95. At-Tin
        "Segumpal Darah",       // 96. Al-'Alaq
        "Kemuliaan",            // 97. Al-Qadr
        "Bukti Nyata",          // 98. Al-Bayyinah
        "Guncangan",            // 99. Az-Zalzalah
        "Kuda Perang",          // 100. Al-'Adiyat
        "Hari Kiamat",          // 101. Al-Qari'ah
        "Bermegah-megahan",     // 102. At-Takasur
        "Masa",                 // 103. Al-'Asr
        "Pengumpat",            // 104. Al-Humazah
        "Gajah",                // 105. Al-Fil
        "Quraisy",              // 106. Quraisy
        "Barang Berguna",       // 107. Al-Ma'un
        "Pemberian Banyak",     // 108. Al-Kausar
        "Orang Kafir",          // 109. Al-Kafirun
        "Pertolongan",          // 110. An-Nasr
        "Gejolak Api",          // 111. Al-Lahab
        "Ikhlas",               // 112. Al-Ikhlas
        "Waktu Subuh",          // 113. Al-Falaq
        "Manusia"               // 114. An-Nas
    };

    /**
     * Gets the Indonesian translation for a given Surah number (1-114).
     * @param surahNumber The 1-indexed number of the Surah.
     * @return The translation, or an empty string if out of bounds.
     */
    public static String getIndonesianTranslation(int surahNumber) {
        if (surahNumber >= 1 && surahNumber <= 114) {
            return INDONESIAN_TRANSLATIONS[surahNumber - 1];
        }
        return "";
    }
}
