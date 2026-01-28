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

        setupSwitch(view, R.id.bsSwitchFajr, "Fajr", settingsManager);
        setupSwitch(view, R.id.bsSwitchDhuhr, "Dhuhr", settingsManager);
        setupSwitch(view, R.id.bsSwitchAsr, "Asr", settingsManager);
        setupSwitch(view, R.id.bsSwitchMaghrib, "Maghrib", settingsManager);
        setupSwitch(view, R.id.bsSwitchIsha, "Isha", settingsManager);

        Button btnClose = view.findViewById(R.id.btnCloseSheet);
        btnClose.setOnClickListener(v -> dismiss());

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

    private void updateSwitchText(View view, int id, String name, String time) {
        SwitchMaterial sw = view.findViewById(id);
         if (time != null) {
            String cleanTime = time.split(" ")[0];
            sw.setText(name + " (" + cleanTime + ")");
         } else {
             sw.setText(name + " 00:00");
         }
    }

    private void setupSwitch(View view, int id, String prayerName, SettingsManager sm) {
        SwitchMaterial sw = view.findViewById(id);
        sw.setChecked(sm.isPrayerAlarmEnabled(prayerName));
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sm.setPrayerAlarmEnabled(prayerName, isChecked);
            String msg = getString(isChecked ? R.string.reminder_for_prayer_enabled : R.string.reminder_for_prayer_disabled, prayerName);
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

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Remind before " + prayerName)
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
}
