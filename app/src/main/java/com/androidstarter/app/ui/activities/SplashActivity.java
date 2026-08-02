package com.androidstarter.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;


import com.androidstarter.app.MainActivity;
import com.androidstarter.app.R;

import com.androidstarter.app.data.model.VersionResponse;
import com.androidstarter.app.data.remote.services.VersionApiService;
import com.androidstarter.app.utils.AppPreference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000;
    private static final int MAX_TIMEOUT = 10000;
    private boolean isNavigated = false;
    private Handler handler = new Handler();

    private boolean isRemoteConfigDone = false;
    private boolean isVersionSyncDone = false;
    private boolean isMinDelayDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        TextView tvAppName = findViewById(R.id.tvAppName);
        AppPreference appPreference = new AppPreference(this);
        String customAppName = appPreference.getString("app_name", getString(R.string.app_name));
        tvAppName.setText(customAppName);
        
        initRemoteConfig();
        fetchVersion();

        // Memastikan splash screen minimal tampil selama SPLASH_DELAY (2 detik)
        handler.postDelayed(() -> {
            isMinDelayDone = true;
            checkAndNavigate();
        }, SPLASH_DELAY);

        // Fallback: paksa masuk setelah 10 detik jika ada masalah jaringan
        handler.postDelayed(this::navigateToMain, MAX_TIMEOUT);
    }

    private void fetchVersion() {
        com.androidstarter.app.data.repository.VersionRepository repository = new com.androidstarter.app.data.repository.VersionRepository();
        repository.checkAndSyncVersion(this, new com.androidstarter.app.data.repository.VersionRepository.VersionCallback() {
            @Override
            public void onComplete() {
                isVersionSyncDone = true;
                checkAndNavigate();
            }
        });
    }

    private void initRemoteConfig() {
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        boolean isDebug = (getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(isDebug ? 0 : 43200)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("RemoteConfig", "Fetch successful");
                        AppPreference appPreference = new AppPreference(SplashActivity.this);
                        for (java.util.Map.Entry<String, com.google.firebase.remoteconfig.FirebaseRemoteConfigValue> entry : mFirebaseRemoteConfig.getAll().entrySet()) {
                            String key = entry.getKey();
                            String strValue = entry.getValue().asString();
                            Log.d("RemoteConfig", "Key: " + key + " | Value: " + strValue);
                            if (strValue.equalsIgnoreCase("true") || strValue.equalsIgnoreCase("false")) {
                                appPreference.saveBoolean(key, entry.getValue().asBoolean());
                            } else if (strValue.matches("^-?\\d+$")) {
                                try {
                                    appPreference.saveInt(key, Integer.parseInt(strValue));
                                } catch (NumberFormatException e) {
                                    try {
                                        appPreference.saveLong(key, Long.parseLong(strValue));
                                    } catch (NumberFormatException ex) {
                                        appPreference.saveString(key, strValue);
                                    }
                                }
                            } else if (strValue.matches("^-?\\d+\\.\\d+$")) {
                                try {
                                    appPreference.saveDouble(key, Double.parseDouble(strValue));
                                } catch (NumberFormatException e) {
                                    appPreference.saveString(key, strValue);
                                }
                            } else {
                                appPreference.saveString(key, strValue);
                            }
                        }
                    } else {
                        Log.e("RemoteConfig", "Fetch failed", task.getException());
                    }
                    isRemoteConfigDone = true;
                    checkAndNavigate();

                });
    }

    private synchronized void checkAndNavigate() {
        if (isRemoteConfigDone && isVersionSyncDone && isMinDelayDone) {
            navigateToMain();
        }
    }

    private synchronized void navigateToMain() {
        if (!isNavigated) {
            isNavigated = true;
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            if (getIntent() != null && getIntent().getExtras() != null) {
                intent.putExtras(getIntent().getExtras());
            }
            // intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            startActivity(intent);
            finish();
        }
    }
}
