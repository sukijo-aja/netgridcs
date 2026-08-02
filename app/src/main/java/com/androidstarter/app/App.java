package com.androidstarter.app;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import com.androidstarter.app.utils.app.SettingsManager;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        SettingsManager settingsManager = SettingsManager.getInstance(this);
        if (settingsManager.isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
