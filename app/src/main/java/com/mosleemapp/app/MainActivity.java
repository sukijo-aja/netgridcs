package com.mosleemapp.app;

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
import com.mosleemapp.app.databinding.ActivityMainBinding;
import com.mosleemapp.app.ui.fragments.HadithFragment;
import com.mosleemapp.app.ui.fragments.HomeFragment;
import com.mosleemapp.app.ui.fragments.QuranFragment;
import com.mosleemapp.app.ui.fragments.SettingsFragment;
import com.mosleemapp.app.ui.viewmodel.PrayerViewModel;
import com.mosleemapp.app.utils.AppPreference;
import com.mosleemapp.app.utils.LocationManagerHelper;

import com.mosleemapp.app.utils.AdMobUtil;
import com.mosleemapp.app.utils.LocaleHelper;

import com.mosleemapp.app.ui.activities.BaseActivity;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.mosleemapp.app.workers.PrayerUpdateWorker;
import com.mosleemapp.app.data.local.AppDatabase;
import com.mosleemapp.app.data.repository.HadithRepository;
import com.mosleemapp.app.data.repository.QuranRepository;

import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity implements LocationManagerHelper.LocationListener {

    private String TAG = "MainActivityy";
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private ActivityMainBinding binding;
    private PrayerViewModel prayerViewModel;
    private LocationManagerHelper locationManagerHelper;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted || coarseLocationGranted != null && coarseLocationGranted) {
                    locationManagerHelper.getLocation();
                    // Also trigger fetch immediately with default/last known to populate UI
                    prayerViewModel.fetchPrayerTimes();
                } else {
                    Toast.makeText(this, R.string.location_permission_required, Toast.LENGTH_SHORT).show();
                    // Fallback to default (ViewModel has default Jakarta coords)
                    prayerViewModel.fetchPrayerTimes();
                }

                // Check and request exact alarm permission if on Android 12+
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(android.content.Context.ALARM_SERVICE);
                    if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                        android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    }
                }
            });

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup ViewModel (Scoped to Activity so Fragments can access if needed)
        prayerViewModel = new ViewModelProvider(this).get(PrayerViewModel.class);
        
        // Initialize AdMob
        AdMobUtil.initialize(this);
        
        // Setup Location
        locationManagerHelper = new LocationManagerHelper(this, this);
        checkPermissions();

        // Setup Navigation
        setupBottomNavigation();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Schedule Background Prayer Update
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
                
        PeriodicWorkRequest updateRequest = new PeriodicWorkRequest.Builder(
                PrayerUpdateWorker.class, 24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();
                
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "PrayerUpdateWork",
                ExistingPeriodicWorkPolicy.KEEP,
                updateRequest);

        // Try to download Quran & Hadith if not already available
        tryDownloadAllResources();

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




        // Double back to exit
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finish();
                    return;
                }
                doubleBackToExitPressedOnce = true;
                Toast.makeText(MainActivity.this, R.string.double_back_to_exit, Toast.LENGTH_SHORT).show();
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
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
                selectedFragment = new QuranFragment();
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            } else if (itemId == R.id.nav_hadith) {
                selectedFragment = new HadithFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }


    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void checkPermissions() {
        // Trigger initial fetch with default/cached location so UI isn't empty
        prayerViewModel.fetchPrayerTimes();

        java.util.List<String> permissionsToRequest = new java.util.ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        } else {
            locationManagerHelper.getLocation();
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
        } else {
            // Already have permissions, directly check alarm permission
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(android.content.Context.ALARM_SERVICE);
                if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                    android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    public void onLocationReceived(double latitude, double longitude) {
        prayerViewModel.updateLocation(latitude, longitude);
        Toast.makeText(this, R.string.location_updated, Toast.LENGTH_SHORT).show();
        AppPreference appPreference = new AppPreference(this);
        appPreference.saveLong("lat", Double.doubleToRawLongBits(latitude));
        appPreference.saveLong("lon", Double.doubleToRawLongBits(longitude));
    }

    private void tryDownloadAllResources() {
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);

            // Quran complete = 114 surahs
            int surahCount = db.quranDao().getSurahCount();
            boolean quranComplete = surahCount >= 114;

            // Hadith complete = books exist AND hadiths exist
            int bookCount = db.hadithDao().getBookCount();
            int hadithCount = db.hadithDao().getTotalHadithCount();
            boolean hadithComplete = bookCount > 0 && hadithCount > 0;

            if (quranComplete && hadithComplete) {
                Log.d(TAG, "Quran (" + surahCount + " surahs) and Hadith (" + hadithCount + " hadiths) are complete. Skipping download.");
                return;
            }

            if (!quranComplete) {
                Log.d(TAG, "Quran incomplete (" + surahCount + "/114 surahs). Starting download...");
                QuranRepository quranRepo = new QuranRepository(this);
                quranRepo.downloadAllData(new QuranRepository.Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean data) {
                        Log.d(TAG, "Quran download complete.");
                    }
                    @Override
                    public void onError(String message) {
                        Log.w(TAG, "Quran download failed: " + message);
                    }
                });
            }

            if (!hadithComplete) {
                Log.d(TAG, "Hadith incomplete (books=" + bookCount + ", hadiths=" + hadithCount + "). Starting download...");
                HadithRepository hadithRepo = new HadithRepository(this);
                hadithRepo.downloadAllData(new QuranRepository.Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean data) {
                        Log.d(TAG, "Hadith download complete.");
                    }
                    @Override
                    public void onError(String message) {
                        Log.w(TAG, "Hadith download failed: " + message);
                    }
                });
            }
        });
    }
}
