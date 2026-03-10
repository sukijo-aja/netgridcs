package com.mosleemapp.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mosleemapp.app.R;
import com.mosleemapp.app.ui.dialogs.PrayerSettingsBottomSheet;
import com.mosleemapp.app.utils.AdMobUtil;
import com.mosleemapp.app.utils.AlarmScheduler;
import com.mosleemapp.app.utils.LocaleHelper;
import com.mosleemapp.app.utils.SettingsManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.lifecycle.ViewModelProvider;
import com.mosleemapp.app.ui.viewmodel.PrayerTrackerViewModel;
import com.mosleemapp.app.data.local.entity.CustomHabitEntity;
import android.widget.LinearLayout;
import android.widget.ImageButton;

public class SettingsFragment extends Fragment {

    private SeekBar seekBarFontSize;
    private TextView tvPreview;
    private TextView tvLabelFontSize;
    private Button btnLogin;
    private TextView tvUserId;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        seekBarFontSize = view.findViewById(R.id.seekBarFontSize);
        tvPreview = view.findViewById(R.id.tvPreview);
        tvLabelFontSize = view.findViewById(R.id.labelFontSize);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbarSettings);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> {
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = 
                requireActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_home);
            } else {
                requireActivity().onBackPressed();
            }
        });

        SettingsManager settingsManager = SettingsManager.getInstance(requireContext());

        // Base size 18, Max progress 32 -> Max size 50
        // Current saved size
        float currentSize = settingsManager.getArabicFontSize();
        int progress = (int) (currentSize - 18);
        if (progress < 0) progress = 0;

        tvLabelFontSize.setText(getString(R.string.label_font_size )+ " (" + currentSize + ")");

        seekBarFontSize.setMax(32); // 18 + 32 = 50
        seekBarFontSize.setProgress(progress);
        tvPreview.setTextSize(currentSize);

        seekBarFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float newSize = 18 + progress;
                tvPreview.setTextSize(newSize);
                settingsManager.saveArabicFontSize(newSize);
                tvLabelFontSize.setText(getString(R.string.label_font_size )+ " (" + newSize+ ")");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        SwitchMaterial switchPremium = view.findViewById(R.id.switchPremium);
        switchPremium.setChecked(settingsManager.isPremium());
        switchPremium.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setPremium(isChecked);
            AdMobUtil.setPremium(isChecked);
            Toast.makeText(requireContext(), 
                isChecked ? "Premium Enabled (Ads Disabled)" : "Premium Disabled (Ads Enabled)", 
                Toast.LENGTH_SHORT).show();
        });

        // Launch Bottom Sheet for Individual Prayers
        view.findViewById(R.id.btnManagePrayers).setOnClickListener(v -> {
            PrayerSettingsBottomSheet bottomSheet = new PrayerSettingsBottomSheet();
            bottomSheet.show(getParentFragmentManager(), PrayerSettingsBottomSheet.TAG);
        });

        // Manage Custom Habits
        LinearLayout llSettingsCustomHabits = view.findViewById(R.id.llSettingsCustomHabits);
        view.findViewById(R.id.btnSettingsAddHabit).setOnClickListener(v -> showAddHabitDialog());

        PrayerTrackerViewModel prayerTrackerViewModel = new ViewModelProvider(requireActivity()).get(PrayerTrackerViewModel.class);
        prayerTrackerViewModel.getAllCustomHabits().observe(getViewLifecycleOwner(), habits -> {
            llSettingsCustomHabits.removeAllViews();
            if (habits == null || habits.isEmpty()) {
                TextView tvEmpty = new TextView(requireContext());
                tvEmpty.setText("No custom habits yet. Click + to add one.");
                tvEmpty.setPadding(32, 32, 32, 32);
                tvEmpty.setGravity(android.view.Gravity.CENTER);
                llSettingsCustomHabits.addView(tvEmpty);
                return;
            }
            for (CustomHabitEntity habit : habits) {
                View habitView = getLayoutInflater().inflate(R.layout.item_manage_custom_habit, llSettingsCustomHabits, false);
                TextView tvName = habitView.findViewById(R.id.tvHabitName);
                ImageButton btnDelete = habitView.findViewById(R.id.btnDeleteHabit);
                
                tvName.setText(habit.name);
                btnDelete.setOnClickListener(v -> {
                    new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Habit")
                        .setMessage("Are you sure you want to delete '" + habit.name + "'? This will delete all history for this habit.")
                        .setPositiveButton("Delete", (dialog, which) -> prayerTrackerViewModel.deleteCustomHabit(habit))
                        .setNegativeButton("Cancel", null)
                        .show();
                });
                llSettingsCustomHabits.addView(habitView);
            }
        });

        Button btnLanguage = view.findViewById(R.id.btnLanguage);
        updateLanguageButtonText(btnLanguage);
        btnLanguage.setOnClickListener(v -> showLanguageDialog());
        
        mAuth = FirebaseAuth.getInstance();
        tvUserId = view.findViewById(R.id.tvUserId);
        btnLogin = view.findViewById(R.id.btnLogin);
        
        updateLoginUI();
        
        btnLogin.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                // Logout
                mAuth.signOut();
                
                androidx.credentials.CredentialManager credentialManager = androidx.credentials.CredentialManager.create(requireContext());
                credentialManager.clearCredentialStateAsync(
                    new androidx.credentials.ClearCredentialStateRequest(), 
                    null, 
                    androidx.core.content.ContextCompat.getMainExecutor(requireContext()), 
                    new androidx.credentials.CredentialManagerCallback<Void, androidx.credentials.exceptions.ClearCredentialException>() {
                        @Override
                        public void onResult(Void result) {}
                        @Override
                        public void onError(androidx.credentials.exceptions.ClearCredentialException e) {}
                    }
                );

                com.mosleemapp.app.utils.AppPreference appPreference = new com.mosleemapp.app.utils.AppPreference(requireContext());
                appPreference.remove("UID");
                appPreference.remove("USER_EMAIL");
                appPreference.remove("USER_NAME");

                Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show();
                updateLoginUI();
            } else {
                // Login
                startActivity(new android.content.Intent(requireContext(), com.mosleemapp.app.ui.activities.LoginActivity.class));
            }
        });

        // Initial check for unique ID if not logged in
        String currentId = settingsManager.getUserId();
        tvUserId.setText("User ID: " + currentId);

        if ("Fetching...".equals(currentId)) {
            com.mosleemapp.app.utils.FirebaseUtil.getInstance(requireContext()).getInstallationId(id -> {
                if (isAdded()) {
                    if (id != null) {
                        tvUserId.setText("User ID: " + id);
                    } else {
                        tvUserId.setText("User ID: Error fetching ID");
                    }
                }
            });
        }

        tvUserId.setOnLongClickListener(v -> {
            String uid = settingsManager.getUserId();
            // Copy to clipboard option
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("User ID", uid);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), "User ID copied", Toast.LENGTH_SHORT).show();
            return true;
        });

        Button btnResetData = view.findViewById(R.id.btnResetData);
        btnResetData.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Reset Data")
                    .setMessage("This will delete all local Quran data and force the app to fetch from the internet again. Are you sure?")
                    .setPositiveButton("Reset", (dialog, which) -> {
                        resetData();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        Button btnDownloadAll = view.findViewById(R.id.btnDownloadAll);
        btnDownloadAll.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Download All Resources")
                    .setMessage("This will download the entire Quran (Arabic, English, Indonesian) for offline use. This may take a few minutes (approx. 10MB).")
                    .setPositiveButton("Download", (dialog, which) -> {
                        startDownloadAll();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAuth != null) {
            updateLoginUI();
        }
    }

    private void updateLoginUI() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            btnLogin.setText("Logout");
            btnLogin.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
            String displayName = user.getDisplayName();
            String email = user.getEmail();
            if (displayName != null && !displayName.isEmpty()) {
                tvUserId.setText("Logged in as: " + displayName);
            } else if (email != null && !email.isEmpty()) {
                tvUserId.setText("Logged in as: " + email);
            } else {
                tvUserId.setText("Logged in as: " + user.getUid());
            }
        } else {
            btnLogin.setText("Login");
            SettingsManager settingsManager = SettingsManager.getInstance(requireContext());
            tvUserId.setText("User ID: " + settingsManager.getUserId());
        }
    }

    private void startDownloadAll() {
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(requireContext());
        progressDialog.setMessage("Downloading Quran Data... Please wait.");
        progressDialog.setCancelable(false);
        progressDialog.show();

        com.mosleemapp.app.data.repository.QuranRepository repository = new com.mosleemapp.app.data.repository.QuranRepository(requireContext());
        repository.downloadAllData(new com.mosleemapp.app.data.repository.QuranRepository.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                 if (isAdded()) {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Download Complete! You can now use the app offline.", Toast.LENGTH_LONG).show();
                 }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Download Failed: " + message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void resetData() {
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            com.mosleemapp.app.data.local.AppDatabase db = com.mosleemapp.app.data.local.AppDatabase.getDatabase(requireContext());
            db.quranDao().deleteAllAyahs();
            db.quranDao().deleteAllSurahs();
            
            // Clear last read prefs
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("MoslemAppPrefs", android.content.Context.MODE_PRIVATE);
            prefs.edit().remove("last_read_surah_number").remove("last_read_surah_name").apply();

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Data reset. Restart app or go to Home to re-fetch.", Toast.LENGTH_LONG).show();
            });
        });
    }

    private void updateLanguageButtonText(Button button) {
        String currentLang = LocaleHelper.getLanguage(requireContext());
        String langName = "English";
        if (currentLang.equals("in")) langName = "Bahasa Indonesia";
        else if (currentLang.equals("ar")) langName = "العربية";

        button.setText(getString(R.string.language) + ": " + langName);
    }

    private void showLanguageDialog() {
        final String[] languages = {"English", "Bahasa Indonesia", "العربية"};
        final String[] codes = {"en", "in", "ar"};

        int checkedItem = 0;
        String currentLang = LocaleHelper.getLanguage(requireContext());
        if (currentLang.equals("in")) checkedItem = 1;
        else if (currentLang.equals("ar")) checkedItem = 2;

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.select_language)
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    LocaleHelper.setLocale(requireContext(), codes[which]);
                    dialog.dismiss();
                    requireActivity().recreate();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showAddHabitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add New Habit");

        final android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint("e.g. Read Quran, Fasting");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                PrayerTrackerViewModel vm = new ViewModelProvider(requireActivity()).get(PrayerTrackerViewModel.class);
                vm.addCustomHabit(name);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
