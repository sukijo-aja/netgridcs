package com.mosleemapp.app.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mosleemapp.app.R;
import com.mosleemapp.app.utils.SettingsManager;

public class PrayerSettingsBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "PrayerSettingsBottomSheet";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_prayer_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SettingsManager settingsManager = SettingsManager.getInstance(requireContext());

        com.google.android.material.switchmaterial.SwitchMaterial switchReminder = view.findViewById(R.id.bsSwitchReminder);
        switchReminder.setChecked(settingsManager.isReminderEnabled());
        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    android.app.AlarmManager alarmManager = (android.app.AlarmManager) requireContext().getSystemService(android.content.Context.ALARM_SERVICE);
                    if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                        android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        intent.setData(android.net.Uri.parse("package:" + requireContext().getPackageName()));
                        startActivity(intent);
                        switchReminder.setChecked(false);
                        android.widget.Toast.makeText(requireContext(), "Please grant exact alarm permission first", android.widget.Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }

            settingsManager.setReminderEnabled(isChecked);
            if (isChecked) {
                com.mosleemapp.app.utils.AlarmScheduler.schedulePrayerAlarms(requireContext(), null);
                android.widget.Toast.makeText(requireContext(), R.string.reminders_enabled, android.widget.Toast.LENGTH_SHORT).show();
            } else {
                com.mosleemapp.app.utils.AlarmScheduler.cancelAlarms(requireContext());
                android.widget.Toast.makeText(requireContext(), R.string.reminders_disabled, android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        android.widget.SeekBar seekBarPreReminder = view.findViewById(R.id.bsSeekBarPreReminder);
        android.widget.TextView tvPreReminderLabel = view.findViewById(R.id.bsTvPreReminderLabel);
        
        int currentPreReminder = settingsManager.getPrePrayerReminderMinutes();
        seekBarPreReminder.setProgress(currentPreReminder);
        tvPreReminderLabel.setText(getString(R.string.reminder_before_adhan_minutes, currentPreReminder));

        seekBarPreReminder.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                tvPreReminderLabel.setText(getString(R.string.reminder_before_adhan_minutes, progress));
                settingsManager.setPrePrayerReminderMinutes(progress);
            }
            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });

        setupSwitch(view, R.id.bsSwitchFajr, "Fajr", settingsManager);
        setupSwitch(view, R.id.bsSwitchDhuhr, "Dhuhr", settingsManager);
        setupSwitch(view, R.id.bsSwitchAsr, "Asr", settingsManager);
        setupSwitch(view, R.id.bsSwitchMaghrib, "Maghrib", settingsManager);
        setupSwitch(view, R.id.bsSwitchIsha, "Isha", settingsManager);

        Button btnClose = view.findViewById(R.id.btnCloseSheet);
        btnClose.setOnClickListener(v -> dismiss());

        // Auto Silent Mode
        setupAutoSilent(view, settingsManager);

        androidx.lifecycle.ViewModelProvider provider = new androidx.lifecycle.ViewModelProvider(requireActivity());
        com.mosleemapp.app.ui.viewmodel.PrayerViewModel viewModel = provider.get(com.mosleemapp.app.ui.viewmodel.PrayerViewModel.class);
        
        viewModel.getPrayerTimes().observe(getViewLifecycleOwner(), entity -> {
            if (entity != null) {
                updateSwitchText(view, R.id.bsSwitchFajr, "Fajr", entity.fajr);
                updateSwitchText(view, R.id.bsSwitchDhuhr, "Dhuhr", entity.dhuhr);
                updateSwitchText(view, R.id.bsSwitchAsr, "Asr", entity.asr);
                updateSwitchText(view, R.id.bsSwitchMaghrib, "Maghrib", entity.maghrib);
                updateSwitchText(view, R.id.bsSwitchIsha, "Isha", entity.isha);
            }
        });
    }

    private String getLocalizedPrayerName(String prayerName) {
        switch (prayerName) {
            case "Fajr": return getString(R.string.fajr);
            case "Dhuhr": return getString(R.string.dhuhr);
            case "Asr": return getString(R.string.asr);
            case "Maghrib": return getString(R.string.maghrib);
            case "Isha": return getString(R.string.isha);
            default: return prayerName;
        }
    }

    private void updateSwitchText(View view, int id, String name, String time) {
        SwitchMaterial sw = view.findViewById(id);
        String localizedName = getLocalizedPrayerName(name);
         if (time != null) {
            String cleanTime = time.split(" ")[0];
            sw.setText(localizedName + " (" + cleanTime + ")");
         } else {
             sw.setText(localizedName + " 00:00");
         }
    }

    private void setupSwitch(View view, int id, String prayerName, SettingsManager sm) {
        SwitchMaterial sw = view.findViewById(id);
        sw.setChecked(sm.isPrayerAlarmEnabled(prayerName));
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sm.setPrayerAlarmEnabled(prayerName, isChecked);
            String localizedName = getLocalizedPrayerName(prayerName);
            String msg = getString(isChecked ? R.string.reminder_for_prayer_enabled : R.string.reminder_for_prayer_disabled, localizedName);
            android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show();
        });

        // Offset Setup
        int offsetId = getOffsetId(prayerName);
        android.widget.TextView tvOffset = view.findViewById(offsetId);
        updateOffsetUI(tvOffset, sm.getPrayerAlarmOffset(prayerName));

        tvOffset.setOnClickListener(v -> showOffsetDialog(prayerName, sm, tvOffset));
    }

    private void showOffsetDialog(String prayerName, SettingsManager sm, android.widget.TextView tvOffset) {
        final String[] options = {"On Time", "5 min before", "10 min before", "15 min before", "30 min before"};
        final int[] values = {0, 5, 10, 15, 30};
        String localizedName = getLocalizedPrayerName(prayerName);

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Remind before " + localizedName)
                .setItems(options, (dialog, which) -> {
                    int selectedOffset = values[which];
                    sm.setPrayerAlarmOffset(prayerName, selectedOffset);
                    updateOffsetUI(tvOffset, selectedOffset);
                    android.widget.Toast.makeText(requireContext(), R.string.reminder_offset_updated, android.widget.Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void updateOffsetUI(android.widget.TextView tv, int offset) {
        if (offset == 0) {
            tv.setText("On Time");
        } else {
            tv.setText(offset + " min before");
        }
    }

    private int getOffsetId(String prayerName) {
        switch (prayerName) {
            case "Fajr": return R.id.tvOffsetFajr;
            case "Dhuhr": return R.id.tvOffsetDhuhr;
            case "Asr": return R.id.tvOffsetAsr;
            case "Maghrib": return R.id.tvOffsetMaghrib;
            case "Isha": return R.id.tvOffsetIsha;
            default: return 0;
        }
    }

    private void setupAutoSilent(View view, SettingsManager sm) {
        SwitchMaterial swAutoSilent = view.findViewById(R.id.bsSwitchAutoSilent);
        android.widget.TextView tvDuration = view.findViewById(R.id.tvSilentDuration);

        // Initialize state
        swAutoSilent.setChecked(sm.isAutoSilentEnabled());
        updateSilentDurationUI(tvDuration, sm.getAutoSilentDuration());

        swAutoSilent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !com.mosleemapp.app.utils.SilentModeManager.hasDoNotDisturbPermission(requireContext())) {
                // Need DND permission — show toast and open settings
                android.widget.Toast.makeText(requireContext(), R.string.dnd_permission_required, android.widget.Toast.LENGTH_LONG).show();
                com.mosleemapp.app.utils.SilentModeManager.requestDoNotDisturbPermission(requireContext());
                swAutoSilent.setChecked(false);
                return;
            }
            sm.setAutoSilentEnabled(isChecked);
            String msg = getString(isChecked ? R.string.silent_mode_enabled : R.string.silent_mode_disabled);
            android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show();
        });

        tvDuration.setOnClickListener(v -> showSilentDurationDialog(sm, tvDuration));
    }

    private void showSilentDurationDialog(SettingsManager sm, android.widget.TextView tvDuration) {
        final String[] options = {"10 min", "15 min", "20 min", "30 min", "45 min", "60 min"};
        final int[] values = {10, 15, 20, 30, 45, 60};

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.silent_duration)
                .setItems(options, (dialog, which) -> {
                    int selected = values[which];
                    sm.setAutoSilentDuration(selected);
                    updateSilentDurationUI(tvDuration, selected);
                })
                .show();
    }

    private void updateSilentDurationUI(android.widget.TextView tv, int minutes) {
        tv.setText(minutes + " min");
    }
}
