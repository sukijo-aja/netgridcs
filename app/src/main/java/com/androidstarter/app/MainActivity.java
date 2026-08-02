package com.androidstarter.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.messaging.FirebaseMessaging;
import com.androidstarter.app.databinding.ActivityMainBinding;
import com.androidstarter.app.ui.fragments.SettingsFragment;
import com.androidstarter.app.ui.fragments.HomeFragment;
import com.androidstarter.app.utils.AppPreference;

import com.androidstarter.app.utils.AdMobUtil;
import com.androidstarter.app.utils.LocaleHelper;

import com.androidstarter.app.ui.activities.BaseActivity;

import com.androidstarter.app.data.local.AppDatabase;

import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity {

    private String TAG = "MainActivityy";
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private ActivityMainBinding binding;
    // Removed Location and Permission Launcher

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize AdMob
        AdMobUtil.initialize(this);
        AdMobUtil.loadBanner(binding.adView);

        // Setup Navigation
        setupBottomNavigation();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Initialize FCM Token in background
        new Thread(() -> {
            AppPreference appPref = new AppPreference(MainActivity.this);
            String existingToken = appPref.getString("fcm_token", null);
            Log.d(TAG, "Background Thread: Current saved FCM Token: " + (existingToken != null ? existingToken : "null"));

            if (existingToken == null) {
                Log.d(TAG, "Background Thread: Requesting new FCM Token...");
                try {
                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String newToken = task.getResult();
                            appPref.saveString("fcm_token", newToken);
                            Log.d(TAG, "FCM Token successfully initialized: " + newToken);
                        } else {
                            Log.e(TAG, "FCM Token retrieval failed", task.getException());
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error during FirebaseMessaging.getInstance().getToken()", e);
                }
            }
        }).start();




        // Double back to exit or pop backstack
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    if (doubleBackToExitPressedOnce) {
                        finish();
                        return;
                    }
                    doubleBackToExitPressedOnce = true;
                    Toast.makeText(MainActivity.this, R.string.double_back_to_exit, Toast.LENGTH_SHORT).show();
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
                }
            }
        });
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_quran) {
                Toast.makeText(MainActivity.this, "Fitur ini segera hadir", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            } else if (itemId == R.id.nav_hadith) {
                Toast.makeText(MainActivity.this, "Fitur ini segera hadir", Toast.LENGTH_SHORT).show();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                binding.bottomNavigation.setVisibility(android.view.View.GONE);
            } else {
                binding.bottomNavigation.setVisibility(android.view.View.VISIBLE);
            }
        });
    }


    private void loadFragment(Fragment fragment) {
        // Clear backstack when navigating via bottom navigation
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void loadFragmentWithBackStack(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    // Removed CheckPermissions and onLocationReceived
    // Resources download removed
}
