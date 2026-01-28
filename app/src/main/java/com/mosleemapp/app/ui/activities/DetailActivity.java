package com.mosleemapp.app.ui.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.mosleemapp.app.R;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        String title = getIntent().getStringExtra("EXTRA_TITLE");
        
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        if (title != null) {
            tvTitle.setText(title);
        }
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        // AdMob
        com.mosleemapp.app.utils.AdMobUtil.initialize(this);
        com.mosleemapp.app.utils.AdMobUtil.loadBanner(findViewById(R.id.adView));
    }
}
