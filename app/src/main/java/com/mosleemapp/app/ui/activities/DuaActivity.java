package com.mosleemapp.app.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mosleemapp.app.R;
import com.mosleemapp.app.data.models.DuaItem;
import com.mosleemapp.app.ui.adapters.DuaAdapter;
import com.mosleemapp.app.utils.AdMobUtil;
import java.util.ArrayList;
import java.util.List;
import android.widget.FrameLayout;

public class DuaActivity extends BaseActivity {

    private RecyclerView rvDuaList;
    private DuaAdapter adapter;
    private List<DuaItem> duaList;
    private FrameLayout flAdPlaceholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dua);

        ImageView btnBack = findViewById(R.id.btnBack);
        rvDuaList = findViewById(R.id.rvDuaList);
        flAdPlaceholder = findViewById(R.id.flAdPlaceholder);

        btnBack.setOnClickListener(v -> finish());

        setupDuaList();
        
        rvDuaList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DuaAdapter(duaList);
        rvDuaList.setAdapter(adapter);

        // Load Native Ad
        loadNativeAd();
    }

    private void setupDuaList() {
        duaList = new ArrayList<>();
        duaList.add(new DuaItem("Before Sleeping", "بِسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا", "Bismika Allahumma amutu wa ahya", "In Your name, O Allah, I die and I live."));
        duaList.add(new DuaItem("Waking Up", "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ", "Alhamdu lillahil-ladhi ahyana ba'da ma amatana wa ilaihin-nushur", "All praise is due to Allah who gave us life after He had caused us to die, and unto Him is the resurrection."));
        duaList.add(new DuaItem("Entering the Toilet", "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْخُبُثِ وَالْخَبَائِثِ", "Allahumma inni a'udhu bika minal-khubuthi wal-khaba'ith", "O Allah, I seek refuge with You from the male and female noxious beings."));
        duaList.add(new DuaItem("Leaving the Toilet", "غُفْرَانَكَ", "Gufranak", "(I seek) Your forgiveness."));
        duaList.add(new DuaItem("Before Eating", "بِسْمِ اللهِ", "Bismillah", "In the name of Allah."));
        duaList.add(new DuaItem("After Eating", "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ", "Alhamdu lillahil-ladhi at'amana wa saqana wa ja'alana Muslimin", "All praise is due to Allah who fed us, gave us drink, and made us Muslims."));
        duaList.add(new DuaItem("Leaving Home", "بِسْمِ اللهِ تَوَكَّلْتُ عَلَى اللهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللهِ", "Bismillahi tawakkaltu 'alallahi, wa la hawla wa la quwwata illa billah", "In the name of Allah, I place my trust in Allah, and there is no might nor power except with Allah."));
        duaList.add(new DuaItem("Entering Home", "بِسْمِ اللهِ وَلَجْنَا، وَبِسْمِ اللهِ خَرَجْنَا، وَعَلَى رَبِّنَا تَوَكَّلْنَا", "Bismillahi walajna, wa bismillahi kharajna, wa 'ala Rabbina tawakkalna", "In the name of Allah we enter, and in the name of Allah we leave, and upon our Lord we place our trust."));
    }
    
    private void loadNativeAd() {
        AdMobUtil.loadNativeAd(this, nativeAd -> {
            if (isDestroyed()) return;
            
            NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.ad_native, null);
            populateNativeAdView(nativeAd, adView);
            flAdPlaceholder.removeAllViews();
            flAdPlaceholder.addView(adView);
        });
    }

    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        adView.setMediaView(adView.findViewById(R.id.ad_media));
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_icon));

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);
    }
}
