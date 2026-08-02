package com.androidstarter.app.ui.fragments;

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
import com.androidstarter.app.R;
import com.androidstarter.app.ui.activities.LoginActivity;
// Removed Prayer related imports
import com.androidstarter.app.utils.AdMobUtil;
import com.androidstarter.app.utils.AppPreference;
import com.androidstarter.app.utils.FirebaseUtil;
import com.androidstarter.app.utils.LocaleHelper;
import com.androidstarter.app.utils.app.SettingsManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.lifecycle.ViewModelProvider;
import android.widget.LinearLayout;
import android.widget.ImageButton;

public class SettingsFragment extends Fragment {


    private TextView btnLogin;
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



        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
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



        SwitchMaterial switchPremium = view.findViewById(R.id.switchPremium);
        switchPremium.setChecked(settingsManager.isPremium());
        switchPremium.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setPremium(isChecked);
            AdMobUtil.setPremium(isChecked);
            Toast.makeText(requireContext(), 
                isChecked ? "Premium Enabled (Ads Disabled)" : "Premium Disabled (Ads Enabled)", 
                Toast.LENGTH_SHORT).show();
        });

        SwitchMaterial switchNotifications = view.findViewById(R.id.switchNotifications);
        switchNotifications.setChecked(settingsManager.isPushNotificationsEnabled());
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setPushNotificationsEnabled(isChecked);
            if (isChecked) {
                // Subscribe to Firebase Topic if using FCM
                Toast.makeText(requireContext(), "Notifications Enabled", Toast.LENGTH_SHORT).show();
            } else {
                // Unsubscribe from Firebase Topic
                Toast.makeText(requireContext(), "Notifications Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        SwitchMaterial switchDarkMode = view.findViewById(R.id.switchDarkMode);
        switchDarkMode.setChecked(settingsManager.isDarkModeEnabled());
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setDarkModeEnabled(isChecked);
            if (isChecked) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        TextView btnClearCache = view.findViewById(R.id.btnClearCache);
        btnClearCache.setOnClickListener(v -> {
            try {
                requireContext().getCacheDir().delete();
                Toast.makeText(requireContext(), R.string.cache_cleared, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        TextView btnRateUs = view.findViewById(R.id.btnRateUs);
        btnRateUs.setOnClickListener(v -> {
            try {
                startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=" + requireContext().getPackageName())));
            } catch (android.content.ActivityNotFoundException e) {
                startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=" + requireContext().getPackageName())));
            }
        });

        TextView btnShareApp = view.findViewById(R.id.btnShareApp);
        btnShareApp.setOnClickListener(v -> {
            android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareMessage = "\nLet me recommend you this application\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + requireContext().getPackageName() + "\n\n";
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
            startActivity(android.content.Intent.createChooser(shareIntent, "choose one"));
        });

        TextView btnContactUs = view.findViewById(R.id.btnContactUs);
        btnContactUs.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
            intent.setData(android.net.Uri.parse("mailto:support@yourdomain.com")); // only email apps should handle this
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback for App");
            try {
                startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(requireContext(), "No email client found.", Toast.LENGTH_SHORT).show();
            }
        });

        TextView btnPrivacyPolicy = view.findViewById(R.id.btnPrivacyPolicy);
        btnPrivacyPolicy.setOnClickListener(v -> {
            android.content.Intent browserIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://policies.google.com/privacy"));
            startActivity(browserIntent);
        });

        TextView tvAppVersion = view.findViewById(R.id.tvAppVersion);
        try {
            android.content.pm.PackageInfo pInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
            String version = pInfo.versionName;
            tvAppVersion.setText(getString(R.string.app_version, version));
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            tvAppVersion.setText(getString(R.string.app_version, "Unknown"));
        }

        // Removed manage prayers and custom habits UI

        TextView btnLanguage = view.findViewById(R.id.btnLanguage);
        updateLanguageButtonText(btnLanguage);
        btnLanguage.setOnClickListener(v -> showLanguageDialog());

        mAuth = FirebaseAuth.getInstance();
        tvUserId = view.findViewById(R.id.tvUserId);
        btnLogin = view.findViewById(R.id.btnLogin);
        
        updateLoginUI();
        
        // Removed calculation method initialization

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

                AppPreference appPreference = new AppPreference(requireContext());
                appPreference.remove("UID");
                appPreference.remove("USER_EMAIL");
                appPreference.remove("USER_NAME");

                Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show();
                updateLoginUI();
            } else {
                // Login
                startActivity(new android.content.Intent(requireContext(), LoginActivity.class));
            }
        });

        // Initial check for unique ID if not logged in
        String currentId = settingsManager.getUserId();
        tvUserId.setText("User ID: " + currentId);

        if ("Fetching...".equals(currentId)) {
            FirebaseUtil.getInstance(requireContext()).getInstallationId(id -> {
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

        TextView btnResetData = view.findViewById(R.id.btnResetData);
        btnResetData.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Fitur ini segera hadir", Toast.LENGTH_SHORT).show();
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
            btnLogin.setText(R.string.logout);
            btnLogin.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
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
            btnLogin.setText(R.string.login);
            btnLogin.setTextColor(getResources().getColor(R.color.text_primary));
            SettingsManager settingsManager = SettingsManager.getInstance(requireContext());
            tvUserId.setText("User ID: " + settingsManager.getUserId());
        }
    }

    // Removed download and reset methods

    private void updateLanguageButtonText(TextView button) {
        String currentLang = LocaleHelper.getLanguage(requireContext());
        String langName = "English";
        if (currentLang.equals("in")) langName = "Bahasa Indonesia";
        else if (currentLang.equals("ar")) langName = "العربية";

        button.setText(getString(R.string.language) + ": " + langName);
    }

    private void showLanguageDialog() {
        final String[] languages = {"English", "Bahasa Indonesia", "العربية", "中文"};
        final String[] codes = {"en", "in", "ar", "zh"};

        int checkedItem = 0;
        String currentLang = LocaleHelper.getLanguage(requireContext());
        if (currentLang.equals("in")) checkedItem = 1;
        else if (currentLang.equals("ar")) checkedItem = 2;
        else if (currentLang.equals("zh")) checkedItem = 3;

        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(64, 64, 64, 64);

        android.widget.TextView title = new android.widget.TextView(requireContext());
        title.setText(R.string.select_language);
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 32);
        layout.addView(title);

        android.widget.ListView listView = new android.widget.ListView(requireContext());
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_single_choice, languages);
        listView.setAdapter(adapter);
        listView.setChoiceMode(android.widget.ListView.CHOICE_MODE_SINGLE);
        listView.setItemChecked(checkedItem, true);
        listView.setDivider(null);
        
        listView.setOnItemClickListener((parent, view, position, id) -> {
            LocaleHelper.setLocale(requireContext(), codes[position]);
            dialog.dismiss();
            requireActivity().recreate();
        });
        
        layout.addView(listView);
        dialog.setContentView(layout);
        dialog.show();
    }

    // Removed showAddHabitDialog

    // Removed calculation method dialog and formatting
}

