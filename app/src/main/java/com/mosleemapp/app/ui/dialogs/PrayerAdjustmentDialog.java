package com.mosleemapp.app.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.mosleemapp.app.R;
import com.mosleemapp.app.utils.app.SettingsManager;

public class PrayerAdjustmentDialog extends DialogFragment {

    private String prayerName;
    private Runnable onDismissListener;

    public static PrayerAdjustmentDialog newInstance(String prayerName) {
        PrayerAdjustmentDialog fragment = new PrayerAdjustmentDialog();
        Bundle args = new Bundle();
        args.putString("prayer_name", prayerName);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnDismissListener(Runnable listener) {
        this.onDismissListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            prayerName = getArguments().getString("prayer_name");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_prayer_adjustment, null); // We need to create this layout or build programmatically

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvValue = view.findViewById(R.id.tvValue);
        Button btnMinus = view.findViewById(R.id.btnMinus);
        Button btnPlus = view.findViewById(R.id.btnPlus);

        tvTitle.setText("Adjust " + prayerName + " Time");

        SettingsManager sm = SettingsManager.getInstance(requireContext());
        final int[] correction = {sm.getPrayerTimeCorrection(prayerName)};
        
        updateValueDisplay(tvValue, correction[0]);

        btnMinus.setOnClickListener(v -> {
            correction[0]--;
            updateValueDisplay(tvValue, correction[0]);
        });

        btnPlus.setOnClickListener(v -> {
            correction[0]++;
            updateValueDisplay(tvValue, correction[0]);
        });

        builder.setView(view)
                .setPositiveButton("Save", (dialog, id) -> {
                    sm.setPrayerTimeCorrection(prayerName, correction[0]);
                    if (onDismissListener != null) {
                        onDismissListener.run();
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dismiss());

        return builder.create();
    }

    private void updateValueDisplay(TextView tv, int val) {
        String sign = val > 0 ? "+" : "";
        tv.setText(sign + val + " minutes");
    }
}
