package com.mosleemapp.app.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.mosleemapp.app.data.local.AppDatabase;
import com.mosleemapp.app.data.local.dao.DuaDao;
import com.mosleemapp.app.data.local.entity.DuaEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.mosleemapp.app.data.remote.DuaApiClient;
import com.mosleemapp.app.data.remote.services.DuaApiService;
import com.mosleemapp.app.data.remote.Responses.DuaResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class DuaRepository {

    private DuaDao duaDao;
    private ExecutorService executorService;
    private AppDatabase db;

    public DuaRepository(Context context) {
        db = AppDatabase.getDatabase(context);
        duaDao = db.duaDao();
        executorService = Executors.newSingleThreadExecutor();
        
        // Seed data if empty
        checkAndSeedData();
    }

    public LiveData<List<DuaEntity>> getAllDuas() {
        return duaDao.getAllDuas();
    }

    public LiveData<List<DuaEntity>> getDuasByCategory(String category) {
        return duaDao.getDuasByCategory(category);
    }

    public LiveData<List<String>> getAllCategories() {
        return duaDao.getAllCategories();
    }

    public void fetchDuasFromApi() {
        DuaApiService apiService = DuaApiClient.getClient().create(DuaApiService.class);
        apiService.getDuas().enqueue(new Callback<List<DuaResponse>>() {
            @Override
            public void onResponse(Call<List<DuaResponse>> call, Response<List<DuaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executorService.execute(() -> {
                         List<DuaEntity> entities = new ArrayList<>();
                         for (DuaResponse item : response.body()) {
                             // Simple duplicate check could be by title, but for now we just insert
                             // Or we can use OnConflictStrategy.IGNORE in DAO if we had unique keys other than ID
                             // Since our IDs are autogen, we might duplicate if we just insert.
                             // For this feature, let's just append or "Online" category.
                             
                             DuaEntity entity = new DuaEntity();
                             entity.titleEn = item.title; // Fallback
                             entity.titleId = item.title;
                             entity.arabic = item.arabic;
                             entity.latin = item.latin;
                             entity.translationEn = item.translation; // Fallback
                             entity.translationId = item.translation;
                             entity.category = "Online"; // Categorize imported ones
                             
                             entities.add(entity);
                         }
                         if (!entities.isEmpty()) {
                             duaDao.insertAll(entities);
                         }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<DuaResponse>> call, Throwable t) {
                // Handle failure (log or callback)
                t.printStackTrace();
            }
        });
    }

    private void checkAndSeedData() {
        executorService.execute(() -> {
            // Check if we need to update/re-seed to get the new extended list
            // For simplicity in this dev phase, if count is small (old data), we nuke and re-seed
            // or just checking if count < 10 (arbitrary since we added way more)
            if (duaDao.getDuaCount() < 10) { 
                List<DuaEntity> initialDuas = getInitialDuas();
                duaDao.insertAll(initialDuas);
            }
        });
    }

    private List<DuaEntity> getInitialDuas() {
        List<DuaEntity> duaList = new ArrayList<>();
        
        // --- DAILY ---
        duaList.add(createDua("Before Sleeping", "Sebelum Tidur", "بِسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا", "Bismika Allahumma amutu wa ahya", "In Your name, O Allah, I die and I live.", "Dengan nama-Mu, Ya Allah, aku mati dan aku hidup.", "Daily"));
        duaList.add(createDua("Waking Up", "Bangun Tidur", "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ", "Alhamdu lillahil-ladhi ahyana ba'da ma amatana wa ilaihin-nushur", "All praise is due to Allah who gave us life after He had caused us to die, and unto Him is the resurrection.", "Segala puji bagi Allah yang menghidupkan kami sesudah mati (tidur) kami dan kepada-Nya kami kembali.", "Daily"));
        duaList.add(createDua("Entering the Toilet", "Masuk Toilet", "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْخُبُثِ وَالْخَبَائِثِ", "Allahumma inni a'udhu bika minal-khubuthi wal-khaba'ith", "O Allah, I seek refuge with You from the male and female noxious beings.", "Ya Allah, aku berlindung pada-Mu dari godaan syetan laki-laki dan syetan perempuan.", "Daily"));
        duaList.add(createDua("Leaving the Toilet", "Keluar Toilet", "غُفْرَانَكَ", "Gufranak", "(I seek) Your forgiveness.", "Aku memohon ampunan-Mu.", "Daily"));
        duaList.add(createDua("Before Eating", "Sebelum Makan", "بِسْمِ اللهِ", "Bismillah", "In the name of Allah.", "Dengan menyebut nama Allah.", "Daily"));
        duaList.add(createDua("After Eating", "Sesudah Makan", "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ", "Alhamdu lillahil-ladhi at'amana wa saqana wa ja'alana Muslimin", "All praise is due to Allah who fed us, gave us drink, and made us Muslims.", "Segala puji bagi Allah yang telah memberi makan kami dan minuman kami, serta menjadikan kami sebagai orang-orang islam.", "Daily"));
        duaList.add(createDua("Leaving Home", "Keluar Rumah", "بِسْمِ اللهِ تَوَكَّلْتُ عَلَى اللهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللهِ", "Bismillahi tawakkaltu 'alallahi, wa la hawla wa la quwwata illa billah", "In the name of Allah, I place my trust in Allah, and there is no might nor power except with Allah.", "Dengan nama Allah, aku bertawakkal kepada Allah. Tiada daya dan kekuatan kecuali dengan Allah.", "Daily"));
        duaList.add(createDua("Entering Home", "Masuk Rumah", "بِسْمِ اللهِ وَلَجْنَا، وَبِسْمِ اللهِ خَرَجْنَا، وَعَلَى رَبِّنَا تَوَكَّلْنَا", "Bismillahi walajna, wa bismillahi kharajna, wa 'ala Rabbina tawakkalna", "In the name of Allah we enter, and in the name of Allah we leave, and upon our Lord we place our trust.", "Dengan nama Allah kami masuk rumah, dengan nama Allah kami keluar rumah, serta kepada Tuhan kami, kami bertawakkal.", "Daily"));
        duaList.add(createDua("Putting on Clothes", "Memakai Pakaian", "الْحَمْدُ لِلَّهِ الَّذِي كَسَانِي هَذَا (الثَّوْبَ) وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ", "Alhamdu lillahil-ladhi kasani hadha (ath-thawba) wa razaqanihi min ghairi hawlin minni wa la quwwah", "Praise be to Allah Who has clothed me with this (garment) and provided it for me, though I was powerless myself and incapable.", "Segala puji bagi Allah yang telah memakaikan pakaian ini kepadaku dan mengaruniakannya kepadaku tanpa daya dan kekuatanku.", "Daily"));
        duaList.add(createDua("Undressing", "Melepas Pakaian", "بِسْمِ اللهِ", "Bismillah", "In the name of Allah.", "Dengan nama Allah.", "Daily"));
        
        // --- PRAYER (SALAH) ---
        duaList.add(createDua("After Adhan", "Setelah Adzan", "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ، وَالصَّلَاةِ الْقَائِمَةِ، آتِ مُحَمَّدًانِ الْوَسِيلَةَ وَالْفَضِيلَةَ، وَابْعَثْهُ مَقَامًا مَحْمُودًانِ الَّذِي وَعَدْتَهُ", "Allahumma Rabba hadhihid-da'watit-tammah, was-salatil-qa'imah, ati Muhammadan al-wasilata wal-fadilah, wab'athhu maqaman mahmudan alladhi wa'adtah", "O Allah, Lord of this perfect call and established prayer. Grant Muhammad the intercession and favor, and raise him to the Maqam Mahmud which You have promised him.", "Ya Allah, Tuhan pemilik panggilan yang sempurna ini dan shalat yang didirikan. Berilah al-wasilah dan al-fadhilah kepada Nabi Muhammad. Dan bangkitkanlah beliau di kedudukan yang terpuji yang telah Engkau janjikan.", "Prayer"));
        duaList.add(createDua("Entering Mosque", "Masuk Masjid", "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ", "Allahumm-aftah li abwaba rahmatik", "O Allah, open the gates of Your mercy for me.", "Ya Allah, bukalah pintu-pintu rahmat-Mu untukku.", "Prayer"));
        duaList.add(createDua("Leaving Mosque", "Keluar Masjid", "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ", "Allahumma inni as'aluka min fadlik", "O Allah, I ask You from Your favour.", "Ya Allah, aku memohon kepada-Mu karunia-Mu.", "Prayer"));
        duaList.add(createDua("Start of Prayer (Iftitah)", "Iftitah", "اللَّهُمَّ بَاعِدْ بَيْنِي وَبَيْنَ خَطَايَايَ كَمَا بَاعَدْتَ بَيْنَ الْمَشْرِقِ وَالْمَغْرِبِ...", "Allahumma ba'id baini wa baina khatayaya kama ba'adta bainal-mashriqi wal-maghrib...", "O Allah, distance me from my key sins as You have distanced the East from the West...", "Ya Allah, jauhkanlah antara aku dan kesalahan-kesalahanku sebagaimana Engkau menjauhkan antara timur dan barat...", "Prayer"));
        duaList.add(createDua("Ruku", "Ruku", "سُبْحَانَ رَبِّيَ الْعَظِيمِ", "Subhana Rabbiyal-Azim", "Glory to my Lord the Exalted.", "Maha Suci Tuhanku Yang Maha Agung.", "Prayer"));
        duaList.add(createDua("Sujud", "Sujud", "سُبْحَانَ رَبِّيَ الْأَعْلَى", "Subhana Rabbiyal-A'la", "Glory to my Lord the Most High.", "Maha Suci Tuhanku Yang Maha Tinggi.", "Prayer"));
        
        // --- FAMILY ---
        duaList.add(createDua("For Parents", "Untuk Orang Tua", "رَبِّ اغْفِرْ لِي وَلِوَالِدَيَّ وَارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا", "Rabbighfir li wa liwalidayya warhamhuma kama rabbayani saghira", "My Lord, forgive me and my parents and have mercy on them just as they brought me up when I was small.", "Ya Tuhanku, ampunilah dosaku dan dosa kedua orang tuaku, dan sayangilah keduanya sebagaimana mereka mendidikku di waktu kecil.", "Family"));
        duaList.add(createDua("For Spouse & Children", "Istri & Anak", "رَبَّنَا هَبْ لَنَا مِنْ أَزْوَاجِنَا وَذُرِّيَّاتِنَا قُرَّةَ أَعْيُنٍ وَاجْعَلْنَا لِلْمُتَّقِينَ إِمَامًا", "Rabbana hab lana min azwajina wa dhuriyyatina qurrata a'yunin waj'alna lil-muttaqina imama", "Our Lord, grant us from among our wives and offspring comfort to our eyes and make us an example for the righteous.", "Ya Tuhan kami, anugrahkanlah kepada kami isteri-isteri kami dan keturunan kami sebagai penyenang hati (kami), dan jadikanlah kami imam bagi orang-orang yang bertakwa.", "Family"));
        
        // --- TRAVEL ---
        duaList.add(createDua("Traveling", "Bepergian", "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ", "Subhanalladhi sakh-khara lana hadha wa ma kunna lahu muqrinin. Wa inna ila Rabbina lamunqalibun", "Glory to Him who has subjected this to us, and we could not have otherwise subdued it. And indeed we, to our Lord, will return.", "Maha Suci Tuhan yang telah menundukkan semua ini bagi kami padahal kami sebelumnya tidak mampu menguasainya, dan sesungguhnya kami akan kembali kepada Tuhan kami.", "Travel"));
        duaList.add(createDua("Entering a Market", "Masuk Pasar", "لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ...", "La ilaha illallahu wahdahu la sharika lahu, lahul-mulku wa lahul-hamdu...", "None has the right to be worshipped but Allah alone, Who has no partner, His is the dominion and His is the praise...", "Tidak ada Tuhan selain Allah semata, tidak ada sekutu bagi-Nya. Bagi-Nya kerajaan dan bagi-Nya segala puji...", "Travel"));
        
        // --- PROTECTION ---
        duaList.add(createDua("Protection from Evil", "Perlindungan", "أَعُوذُ بِكَلِمَاتِ اللهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ", "A'udhu bikalimatillahi at-tammati min sharri ma khalaq", "I seek refuge in the Perfect Words of Allah from the evil of what He has created.", "Aku berlindung dengan kalimat-kalimat Allah yang sempurna dari kejahatan makhluk yang diciptakan-Nya.", "Protection"));
        duaList.add(createDua("When Afraid", "Ketika Takut", "لَا إِلَهَ إِلَّا اللهُ", "La ilaha illallah", "There is no God but Allah.", "Tiada Tuhan selain Allah.", "Protection"));
        
        // --- HAJJ & UMRAH ---
        duaList.add(createDua("Talbiyah", "Talbiyah", "لَبَّيْكَ اللَّهُمَّ لَبَّيْكَ، لَبَّيْكَ لَا شَرِيكَ لَكَ لَبَّيْكَ...", "Labbayk Allahumma labbayk, labbayka la sharika laka labbayk...", "Here I am, O Allah, here I am. Here I am. You have no partner. Here I am...", "Aku penuhi panggilan-Mu, ya Allah, aku penuhi panggilan-Mu. Tidak ada sekutu bagi-Mu, aku penuhi panggilan-Mu...", "Hajj"));
        duaList.add(createDua("Seeing the Kaaba", "Melihat Ka'bah", "اللَّهُمَّ زِدْ هَذَا الْبَيْتَ تَشْرِيفًا وَتَعْظِيمًا وَتَكْرِيمًا وَمَهَابَةً", "Allahumma zid hadhal-baita tashrifan wa ta'ziman wa takriman wa mahabah", "O Allah, increase this House in honor, magnificence, nobility and awe.", "Ya Allah, tambahkanlah kemuliaan, keagungan, kehormatan dan wibawa pada Baitullah ini.", "Hajj"));

        return duaList;
    }

    private DuaEntity createDua(String titleEn, String titleId, String arabic, String latin, String translationEn, String translationId, String category) {
        DuaEntity entity = new DuaEntity();
        entity.titleEn = titleEn;
        entity.titleId = titleId;
        entity.arabic = arabic;
        entity.latin = latin;
        entity.translationEn = translationEn;
        entity.translationId = translationId;
        entity.category = category;
        return entity;
    }
}
