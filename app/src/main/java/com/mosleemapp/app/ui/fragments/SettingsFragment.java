package com.mosleemapp.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mosleemapp.app.R;

public class SettingsFragment extends Fragment {

    private android.widget.SeekBar seekBarFontSize;
    private android.widget.TextView tvPreview;

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

        com.mosleemapp.app.utils.SettingsManager settingsManager = com.mosleemapp.app.utils.SettingsManager.getInstance(requireContext());
        
        // Base size 18, Max progress 32 -> Max size 50
        // Current saved size
        float currentSize = settingsManager.getArabicFontSize();
        int progress = (int) (currentSize - 18);
        if (progress < 0) progress = 0;
        
        seekBarFontSize.setMax(32); // 18 + 32 = 50
        seekBarFontSize.setProgress(progress);
        tvPreview.setTextSize(currentSize);

        seekBarFontSize.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                float newSize = 18 + progress;
                tvPreview.setTextSize(newSize);
                settingsManager.saveArabicFontSize(newSize);
            }

            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });
    }
}
