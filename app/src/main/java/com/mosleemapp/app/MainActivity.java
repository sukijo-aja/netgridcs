package com.mosleemapp.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mosleemapp.app.databinding.ActivityMainBinding;
import com.mosleemapp.app.ui.fragments.HadithFragment;
import com.mosleemapp.app.ui.fragments.PrayerFragment;
import com.mosleemapp.app.ui.fragments.QuranFragment;
import com.mosleemapp.app.ui.fragments.SettingsFragment;
import com.mosleemapp.app.ui.viewmodel.PrayerViewModel;
import com.mosleemapp.app.utils.LocationManagerHelper;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity implements LocationManagerHelper.LocationListener {

    private ActivityMainBinding binding;
    private PrayerViewModel viewModel;
    private LocationManagerHelper locationManagerHelper;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted || coarseLocationGranted != null && coarseLocationGranted) {
                    locationManagerHelper.getLocation();
                } else {
                    Toast.makeText(this, "Location permission required for accurate prayer times", Toast.LENGTH_SHORT).show();
                    // Fallback to default (ViewModel has default Jakarta coords)
                    viewModel.fetchPrayerTimes();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup ViewModel (Scoped to Activity so Fragments can access if needed)
        viewModel = new ViewModelProvider(this).get(PrayerViewModel.class);
        
        // Setup Location
        locationManagerHelper = new LocationManagerHelper(this, this);
        checkLocationPermissions();

        // Setup Navigation
        setupBottomNavigation();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new PrayerFragment());
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_prayer) {
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
//        Toast.makeText(this, "Location Updated", Toast.LENGTH_SHORT).show();
    }
}
