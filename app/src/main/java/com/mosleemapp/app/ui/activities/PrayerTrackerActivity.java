package com.mosleemapp.app.ui.activities;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

public class PrayerTrackerActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayer_tracker);
        
        viewModel = new ViewModelProvider(this).get(PrayerTrackerViewModel.class);
        
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Prayer Tracker");
            toolbar.setNavigationOnClickListener(v -> finish());
        }
        
        initViews();
        setupListeners();
        observeViewModel();
    }

    private void initViews() {
        cbFajr = findViewById(R.id.cbFajr);
        cbDhuhr = findViewById(R.id.cbDhuhr);
        cbAsr = findViewById(R.id.cbAsr);
        cbMaghrib = findViewById(R.id.cbMaghrib);
        cbIsha = findViewById(R.id.cbIsha);
        
        cbTilawah = findViewById(R.id.cbTilawah);
        cbTahajud = findViewById(R.id.cbTahajud);
        cbDuha = findViewById(R.id.cbDuha);
        cbFast = findViewById(R.id.cbFast);
        
        tvDate = findViewById(R.id.tvDate);
        tvProgress = findViewById(R.id.tvProgress);
        
        btnPrevDay = findViewById(R.id.btnPrevDay);
        btnNextDay = findViewById(R.id.btnNextDay);

        // Custom Habits
        llCustomHabitsContainer = findViewById(R.id.llCustomHabitsContainer);
        tvNoHabits = findViewById(R.id.tvNoHabits);
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
        viewModel.getSelectedDate().observe(this, date -> {
            currentDateString = dateFormat.format(date);
            tvDate.setText(displayFormat.format(date));
        });

        viewModel.getCurrentTrackerEntity().observe(this, entity -> {
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

        viewModel.getAllCustomHabits().observe(this, habits -> {
            allHabits = habits;
            renderCustomHabits();
        });

        viewModel.getCurrentCustomHabitLogs().observe(this, logs -> {
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
