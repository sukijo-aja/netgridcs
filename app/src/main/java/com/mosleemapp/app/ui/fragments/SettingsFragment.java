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

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mosleemapp.app.R;
import com.mosleemapp.app.ui.dialogs.PrayerSettingsBottomSheet;
import com.mosleemapp.app.utils.AdMobUtil;
import com.mosleemapp.app.utils.AlarmScheduler;
import com.mosleemapp.app.utils.LocaleHelper;
import com.mosleemapp.app.utils.SettingsManager;

public class SettingsFragment extends Fragment {

    private SeekBar seekBarFontSize;
    private TextView tvPreview;
    private TextView tvLabelFontSize;

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

        SwitchMaterial switchReminder = view.findViewById(R.id.switchReminder);
        switchReminder.setChecked(settingsManager.isReminderEnabled());
        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setReminderEnabled(isChecked);
            if (isChecked) {
                AlarmScheduler.schedulePrayerAlarms(requireContext(), null); // Need entity ideally
                Toast.makeText(requireContext(), R.string.reminders_enabled, Toast.LENGTH_SHORT).show();
            } else {
                AlarmScheduler.cancelAlarms(requireContext());
                Toast.makeText(requireContext(), R.string.reminders_disabled, Toast.LENGTH_SHORT).show();
            }
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

        SeekBar seekBarPreReminder = view.findViewById(R.id.seekBarPreReminder);
        TextView tvPreReminderLabel = view.findViewById(R.id.tvPreReminderLabel);
        
        int currentPreReminder = settingsManager.getPrePrayerReminderMinutes();
        seekBarPreReminder.setProgress(currentPreReminder);
        tvPreReminderLabel.setText("Reminder before Adhan: " + currentPreReminder + " min");

        seekBarPreReminder.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvPreReminderLabel.setText("Reminder before Adhan: " + progress + " min");
                settingsManager.setPrePrayerReminderMinutes(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Launch Bottom Sheet for Individual Prayers
        view.findViewById(R.id.btnManagePrayers).setOnClickListener(v -> {
            PrayerSettingsBottomSheet bottomSheet = new PrayerSettingsBottomSheet();
            bottomSheet.show(getParentFragmentManager(), PrayerSettingsBottomSheet.TAG);
        });

        Button btnLanguage = view.findViewById(R.id.btnLanguage);
        updateLanguageButtonText(btnLanguage);
        btnLanguage.setOnClickListener(v -> showLanguageDialog());
        
        // Show User ID
        TextView tvUserId = view.findViewById(R.id.tvUserId);
        String userId = settingsManager.getUserId();
        // Format to make it look a bit nicer, maybe just show first segment or full
        tvUserId.setText("User ID: " + userId);
        tvUserId.setOnLongClickListener(v -> {
            // Copy to clipboard option
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("User ID", userId);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), "User ID copied", Toast.LENGTH_SHORT).show();
            return true;
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
}
