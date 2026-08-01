package com.mosleemapp.app.ui.fragments;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.ViewModelProvider;

import com.mosleemapp.app.R;
import com.mosleemapp.app.data.local.entity.CustomHabitEntity;
import com.mosleemapp.app.data.local.entity.CustomHabitLogEntity;
import com.mosleemapp.app.data.local.entity.PrayerTrackerEntity;
import com.mosleemapp.app.ui.viewmodel.PrayerTrackerViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrayerTrackerFragment extends Fragment {

    private CheckBox cbFajr, cbDhuhr, cbAsr, cbMaghrib, cbIsha;
    private CheckBox cbTilawah, cbTahajud, cbDuha, cbFast;
    private TextView tvDate, tvProgress;
    private ImageButton btnPrevDay, btnNextDay;

    private PrayerTrackerViewModel viewModel;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault());
    
    private String currentDateString;
    private PrayerTrackerEntity currentEntity;
    private boolean isUpdating = false;

    // --- Custom Habits Section ---
    private android.widget.LinearLayout llCustomHabitsContainer;
    private TextView tvNoHabits;
    private List<CustomHabitEntity> allHabits = new java.util.ArrayList<>();
    private List<CustomHabitLogEntity> currentLogs = new java.util.ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prayer_tracker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(requireActivity()).get(PrayerTrackerViewModel.class);
        
        com.google.android.material.appbar.MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Prayer Tracker");
            toolbar.setNavigationOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        }
        
        initViews(view);
        setupListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        cbFajr = view.findViewById(R.id.cbFajr);
        cbDhuhr = view.findViewById(R.id.cbDhuhr);
        cbAsr = view.findViewById(R.id.cbAsr);
        cbMaghrib = view.findViewById(R.id.cbMaghrib);
        cbIsha = view.findViewById(R.id.cbIsha);
        
        cbTilawah = view.findViewById(R.id.cbTilawah);
        cbTahajud = view.findViewById(R.id.cbTahajud);
        cbDuha = view.findViewById(R.id.cbDuha);
        cbFast = view.findViewById(R.id.cbFast);
        
        tvDate = view.findViewById(R.id.tvDate);
        tvProgress = view.findViewById(R.id.tvProgress);
        
        btnPrevDay = view.findViewById(R.id.btnPrevDay);
        btnNextDay = view.findViewById(R.id.btnNextDay);

        // Custom Habits
        llCustomHabitsContainer = view.findViewById(R.id.llCustomHabitsContainer);
        tvNoHabits = view.findViewById(R.id.tvNoHabits);
    }

    private void setupListeners() {
        btnPrevDay.setOnClickListener(v -> viewModel.changeDate(-1));
        btnNextDay.setOnClickListener(v -> viewModel.changeDate(1));

        android.widget.CompoundButton.OnCheckedChangeListener prayerListener = (buttonView, isChecked) -> {
            if (isUpdating) return;
            updateProgress();
            
            // Save state immediately
            viewModel.updatePrayer(
                    currentDateString, 
                    currentEntity, 
                    cbFajr.isChecked(), 
                    cbDhuhr.isChecked(), 
                    cbAsr.isChecked(), 
                    cbMaghrib.isChecked(), 
                    cbIsha.isChecked(),
                    cbTilawah.isChecked(),
                    cbTahajud.isChecked(),
                    cbDuha.isChecked(),
                    cbFast.isChecked()
            );
        };

        cbFajr.setOnCheckedChangeListener(prayerListener);
        cbDhuhr.setOnCheckedChangeListener(prayerListener);
        cbAsr.setOnCheckedChangeListener(prayerListener);
        cbMaghrib.setOnCheckedChangeListener(prayerListener);
        cbIsha.setOnCheckedChangeListener(prayerListener);
        cbTilawah.setOnCheckedChangeListener(prayerListener);
        cbTahajud.setOnCheckedChangeListener(prayerListener);
        cbDuha.setOnCheckedChangeListener(prayerListener);
        cbFast.setOnCheckedChangeListener(prayerListener);
    }

    private void observeViewModel() {
        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            currentDateString = dateFormat.format(date);
            tvDate.setText(displayFormat.format(date));
        });

        viewModel.getCurrentTrackerEntity().observe(getViewLifecycleOwner(), entity -> {
            isUpdating = true;
            currentEntity = entity;
            if (entity != null) {
                cbFajr.setChecked(entity.fajr);
                cbDhuhr.setChecked(entity.dhuhr);
                cbAsr.setChecked(entity.asr);
                cbMaghrib.setChecked(entity.maghrib);
                cbIsha.setChecked(entity.isha);
                
                cbTilawah.setChecked(entity.tilawah);
                cbTahajud.setChecked(entity.tahajud);
                cbDuha.setChecked(entity.duha);
                cbFast.setChecked(entity.fast);
            } else {
                cbFajr.setChecked(false);
                cbDhuhr.setChecked(false);
                cbAsr.setChecked(false);
                cbMaghrib.setChecked(false);
                cbIsha.setChecked(false);
                
                cbTilawah.setChecked(false);
                cbTahajud.setChecked(false);
                cbDuha.setChecked(false);
                cbFast.setChecked(false);
            }
            updateProgress();
            isUpdating = false;
        });

        viewModel.getAllCustomHabits().observe(getViewLifecycleOwner(), habits -> {
            allHabits = habits;
            renderCustomHabits();
        });

        viewModel.getCurrentCustomHabitLogs().observe(getViewLifecycleOwner(), logs -> {
            currentLogs = logs;
            renderCustomHabits();
        });
    }

    private void updateProgress() {
        int count = 0;
        if (cbFajr.isChecked()) count++;
        if (cbDhuhr.isChecked()) count++;
        if (cbAsr.isChecked()) count++;
        if (cbMaghrib.isChecked()) count++;
        if (cbIsha.isChecked()) count++;
        
        if (cbTilawah.isChecked()) count++;
        if (cbTahajud.isChecked()) count++;
        if (cbDuha.isChecked()) count++;
        if (cbFast.isChecked()) count++;
        
        tvProgress.setText(count + "/9 Tasks Completed");
    }

    private void renderCustomHabits() {
        llCustomHabitsContainer.removeAllViews();
        
        if (allHabits == null || allHabits.isEmpty()) {
            llCustomHabitsContainer.addView(tvNoHabits);
            tvNoHabits.setVisibility(android.view.View.VISIBLE);
            return;
        }

        for (CustomHabitEntity habit : allHabits) {
            android.view.View habitView = getLayoutInflater().inflate(R.layout.item_custom_habit_simple, llCustomHabitsContainer, false);
            CheckBox cb = habitView.findViewById(R.id.cbHabit);

            cb.setText(habit.name);
            
            // Check state
            boolean isCompleted = false;
            if (currentLogs != null) {
                for (CustomHabitLogEntity log : currentLogs) {
                    if (log.habitId == habit.id) {
                        isCompleted = log.isCompleted;
                        break;
                    }
                }
            }
            cb.setOnCheckedChangeListener(null); // Clear listeners
            cb.setChecked(isCompleted);

            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                viewModel.updateCustomHabitLog(currentDateString, habit.id, isChecked);
            });

            llCustomHabitsContainer.addView(habitView);
        }
    }

}
