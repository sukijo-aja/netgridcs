package com.mosleemapp.app.ui.activities;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mosleemapp.app.R;

public class ReminderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Show on Lock Screen logic
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        String prayerName = getIntent().getStringExtra("prayer_name");
        TextView tvPrayerName = findViewById(R.id.tvPrayerName);
        Button btnDismiss = findViewById(R.id.btnDismiss);

        if (prayerName != null) {
            tvPrayerName.setText(prayerName);
        } else {
            tvPrayerName.setText("It's Prayer Time");
        }

        btnDismiss.setOnClickListener(v -> finish());
    }
}
