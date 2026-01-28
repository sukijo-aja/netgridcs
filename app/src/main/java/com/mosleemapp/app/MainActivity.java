package com.mosleemapp.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mosleemapp.app.databinding.ActivityMainBinding;
import com.mosleemapp.app.ui.fragments.HadithFragment;
import com.mosleemapp.app.ui.fragments.HomeFragment;
import com.mosleemapp.app.ui.fragments.PrayerFragment;
import com.mosleemapp.app.ui.fragments.QuranFragment;
import com.mosleemapp.app.ui.fragments.SettingsFragment;
import com.mosleemapp.app.ui.viewmodel.PrayerViewModel;
import com.mosleemapp.app.utils.LocationManagerHelper;
import com.google.android.material.navigation.NavigationBarView;

import com.mosleemapp.app.utils.AdMobUtil;
import com.mosleemapp.app.utils.LocaleHelper;

import com.mosleemapp.app.ui.activities.BaseActivity;

public class MainActivity extends BaseActivity implements LocationManagerHelper.LocationListener {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private ActivityMainBinding binding;
    private PrayerViewModel viewModel;
    private LocationManagerHelper locationManagerHelper;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted || coarseLocationGranted != null && coarseLocationGranted) {
                    locationManagerHelper.getLocation();
                    // Also trigger fetch immediately with default/last known to populate UI
                    viewModel.fetchPrayerTimes();
                } else {
                    Toast.makeText(this, R.string.location_permission_required, Toast.LENGTH_SHORT).show();
                    // Fallback to default (ViewModel has default Jakarta coords)
                    viewModel.fetchPrayerTimes();
                }
            });

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup ViewModel (Scoped to Activity so Fragments can access if needed)
        viewModel = new ViewModelProvider(this).get(PrayerViewModel.class);
        
        // Initialize AdMob
        AdMobUtil.initialize(this);
        
        // Setup Location
        locationManagerHelper = new LocationManagerHelper(this, this);
        checkLocationPermissions();

        // Setup Navigation
        setupBottomNavigation();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

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
            } else if (itemId == R.id.nav_prayer) {
                selectedFragment = new PrayerFragment();
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

    private void checkLocationPermissions() {
        // Trigger initial fetch with default/cached location so UI isn't empty
        viewModel.fetchPrayerTimes();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManagerHelper.getLocation();
        } else {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @Override
    public void onLocationReceived(double latitude, double longitude) {
        viewModel.updateLocation(latitude, longitude);
        Toast.makeText(this, R.string.location_updated, Toast.LENGTH_SHORT).show();
        Log.d("Locationxx", "Latitude: " + latitude + ", Longitude: " + longitude);
    }
}
