package com.mosleemapp.app.ui.activities;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mosleemapp.app.R;
import com.mosleemapp.app.data.local.AppDatabase;
import com.mosleemapp.app.data.local.entity.PrayerTrackerEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PrayerTrackerActivity extends AppCompatActivity {

    private CheckBox cbFajr, cbDhuhr, cbAsr, cbMaghrib, cbIsha;
    private TextView tvDate, tvProgress;
    private ImageButton btnPrevDay, btnNextDay;
    
    private Calendar currentCalendar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault());
    
    private AppDatabase db;
    private PrayerTrackerEntity currentEntity;
    private boolean isUpdating = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayer_tracker);
        
        db = AppDatabase.getDatabase(this);
        currentCalendar = Calendar.getInstance();
        
        initViews();
//        setupListeners();
        loadDataForDate(currentCalendar.getTime());
    }

    private void updateProgress() {
        int count = 0;
        if (cbFajr.isChecked()) count++;
        if (cbDhuhr.isChecked()) count++;
        if (cbAsr.isChecked()) count++;
        if (cbMaghrib.isChecked()) count++;
        if (cbIsha.isChecked()) count++;
        
        tvProgress.setText(count + "/5 Prayers Completed");
    }

    // --- Custom Habits Section ---

    private android.widget.LinearLayout llCustomHabitsContainer;
    private TextView tvNoHabits;
    private java.util.List<com.mosleemapp.app.data.local.entity.CustomHabitEntity> allHabits = new java.util.ArrayList<>();
    private java.util.List<com.mosleemapp.app.data.local.entity.CustomHabitLogEntity> currentLogs = new java.util.ArrayList<>();

    private void initViews() {
        cbFajr = findViewById(R.id.cbFajr);
        cbDhuhr = findViewById(R.id.cbDhuhr);
        cbAsr = findViewById(R.id.cbAsr);
        cbMaghrib = findViewById(R.id.cbMaghrib);
        cbIsha = findViewById(R.id.cbIsha);
        
        tvDate = findViewById(R.id.tvDate);
        tvProgress = findViewById(R.id.tvProgress);
        
        btnPrevDay = findViewById(R.id.btnPrevDay);
        btnNextDay = findViewById(R.id.btnNextDay);

        // Custom Habits
        llCustomHabitsContainer = findViewById(R.id.llCustomHabitsContainer);
        tvNoHabits = findViewById(R.id.tvNoHabits);
        
        findViewById(R.id.btnAddHabit).setOnClickListener(v -> showAddHabitDialog());
    }

    private void loadDataForDate(Date date) {
        String dateString = dateFormat.format(date);
        tvDate.setText(displayFormat.format(date));
        
        // 1. Load Standard Prayers
        db.prayerTrackerDao().getTrackerForDate(dateString).observe(this, entity -> {
            isUpdating = true;
            if (entity != null) {
                currentEntity = entity;
                cbFajr.setChecked(entity.fajr);
                cbDhuhr.setChecked(entity.dhuhr);
                cbAsr.setChecked(entity.asr);
                cbMaghrib.setChecked(entity.maghrib);
                cbIsha.setChecked(entity.isha);
            } else {
                currentEntity = new PrayerTrackerEntity(dateString);
                cbFajr.setChecked(false);
                cbDhuhr.setChecked(false);
                cbAsr.setChecked(false);
                cbMaghrib.setChecked(false);
                cbIsha.setChecked(false);
            }
            isUpdating = false;
            updateProgress();
        });

        // 2. Load Custom Habits
        loadCustomHabits(dateString);
    }
    
    private void loadCustomHabits(String dateString) {
        // Observe definition changes (Add/Remove habits)
       db.customHabitDao().getAllHabits().observe(this, habits -> {
           allHabits = habits;
           
           // Observe log changes for this date
           db.customHabitDao().getLogsForDate(dateString).removeObservers(this); 
           db.customHabitDao().getLogsForDate(dateString).observe(this, logs -> {
               currentLogs = logs;
               renderCustomHabits(dateString);
           });
       });
    }

    private void renderCustomHabits(String dateString) {
        llCustomHabitsContainer.removeAllViews();
        
        if (allHabits.isEmpty()) {
            llCustomHabitsContainer.addView(tvNoHabits);
            tvNoHabits.setVisibility(android.view.View.VISIBLE);
        } else {
             // tvNoHabits is effectively removed by removeAllViews, but we can keep it as a referenced view if needed
            // Actually removeAllViews removes it from parent.
        }

        for (com.mosleemapp.app.data.local.entity.CustomHabitEntity habit : allHabits) {
            android.view.View habitView = getLayoutInflater().inflate(R.layout.item_custom_habit_simple, llCustomHabitsContainer, false);
            CheckBox cb = habitView.findViewById(R.id.cbHabit);
            ImageButton btnDelete = habitView.findViewById(R.id.btnDeleteHabit);

            cb.setText(habit.name);
            
            // Check state
            boolean isCompleted = false;
            if (currentLogs != null) {
                for (com.mosleemapp.app.data.local.entity.CustomHabitLogEntity log : currentLogs) {
                    if (log.habitId == habit.id) {
                        isCompleted = log.isCompleted;
                        break;
                    }
                }
            }
            cb.setOnCheckedChangeListener(null); // Clear listeners
            cb.setChecked(isCompleted);

            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveCustomHabitLog(dateString, habit.id, isChecked);
            });

            btnDelete.setOnClickListener(v -> deleteHabit(habit));

            llCustomHabitsContainer.addView(habitView);
        }
    }

    private void saveCustomHabitLog(String date, int habitId, boolean isCompleted) {
        new Thread(() -> {
            com.mosleemapp.app.data.local.entity.CustomHabitLogEntity log = 
                new com.mosleemapp.app.data.local.entity.CustomHabitLogEntity(date, habitId, isCompleted);
            db.customHabitDao().insertOrUpdateLog(log);
        }).start();
    }
    
    private void showAddHabitDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Add New Habit");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("e.g. Read Quran, Fasting");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                addHabit(name);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addHabit(String name) {
        new Thread(() -> {
            db.customHabitDao().insertHabit(new com.mosleemapp.app.data.local.entity.CustomHabitEntity(name, System.currentTimeMillis()));
        }).start();
    }
    
    private void deleteHabit(com.mosleemapp.app.data.local.entity.CustomHabitEntity habit) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '" + habit.name + "'? This will delete all history for this habit.")
            .setPositiveButton("Delete", (dialog, which) -> {
                 new Thread(() -> {
                    db.customHabitDao().deleteHabit(habit);
                }).start();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
