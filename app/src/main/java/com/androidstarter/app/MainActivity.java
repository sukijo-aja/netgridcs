package com.androidstarter.app;

import android.Manifest;
import android.content.Intent;
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
    
    // Permission Launcher for POST_NOTIFICATIONS (Android 13+)
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Notification permission granted");
                } else {
                    Log.d(TAG, "Notification permission denied");
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                }
            });

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

        // Handle notification click intent
        handleNotificationIntent(getIntent());

        // Request Notification Permission for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Initialize FCM Token
        AppPreference appPref = new AppPreference(MainActivity.this);
        String existingToken = appPref.getString("fcm_token", null);
        
        if (existingToken != null) {
            Log.d(TAG, "FCM_TEST: Current saved FCM Token: " + existingToken);
            System.out.println("FCM_TEST_TOKEN: " + existingToken);
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "FCM Ready", Toast.LENGTH_SHORT).show());
        } else {
            Log.d(TAG, "FCM_TEST: Requesting new FCM Token...");
            try {
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String newToken = task.getResult();
                        appPref.saveString("fcm_token", newToken);
                        Log.d(TAG, "FCM_TEST: Token successfully initialized: " + newToken);
                        System.out.println("FCM_TEST_TOKEN: " + newToken);
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "FCM Token Success", Toast.LENGTH_SHORT).show());
                    } else {
                        Log.e(TAG, "FCM_TEST: Token retrieval failed", task.getException());
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "FCM Token Failed", Toast.LENGTH_SHORT).show());
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "FCM_TEST: Error during FirebaseMessaging.getInstance().getToken()", e);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "FCM Token Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }




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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationIntent(intent);
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            Bundle extras = intent.getExtras();
            boolean openDetail = extras.getBoolean("open_notification_detail", false);
            
            // Check if it's from FCM system tray (background)
            if (!openDetail && extras.containsKey("google.message_id")) {
                openDetail = true;
            }
            
            if (openDetail) {
                String title = extras.getString("title", "Notifikasi");
                String message = extras.getString("message", extras.getString("body", ""));
                String date = extras.getString("date", new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(new java.util.Date()));
                
                com.androidstarter.app.ui.fragments.NotificationDetailFragment detailFragment = new com.androidstarter.app.ui.fragments.NotificationDetailFragment();
                Bundle args = new Bundle();
                args.putString("title", title);
                args.putString("message", message);
                args.putString("date", date);
                detailFragment.setArguments(args);
                
                loadFragmentWithBackStack(detailFragment);
            }
        }
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
