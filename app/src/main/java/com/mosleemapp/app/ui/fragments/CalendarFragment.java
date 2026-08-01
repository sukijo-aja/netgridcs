package com.mosleemapp.app.ui.fragments;

import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.icu.util.ULocale;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.mosleemapp.app.R;
import com.mosleemapp.app.ui.adapters.CalendarEventAdapter;
import com.mosleemapp.app.ui.adapters.CalendarGridAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private TextView tvHijriMonthYear, tvMasehiMonthYear;
    private ImageButton btnPrevMonth, btnNextMonth;
    private RecyclerView rvCalendar, rvEvents;
    private MaterialToolbar toolbar;

    private Calendar currentHijriCalendar;
    private Calendar todayHijriCalendar;
    
    private String[] hijriMonths = {
            "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
            "Jumadil Awwal", "Jumadil Akhir", "Rajab", "Sya'ban",
            "Ramadhan", "Syawal", "Dzulqa'dah", "Dzulhijjah"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        initViews(view);
        setupCalendar();
        setupListeners();
        updateCalendarUI();
        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.menu_calendar);
        tvHijriMonthYear = view.findViewById(R.id.tvHijriMonthYear);
        tvMasehiMonthYear = view.findViewById(R.id.tvMasehiMonthYear);
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        rvCalendar = view.findViewById(R.id.rvCalendar);
        rvEvents = view.findViewById(R.id.rvEvents);

        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        rvCalendar.setLayoutManager(new GridLayoutManager(getContext(), 7));
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupCalendar() {
        ULocale locale = new ULocale("en@calendar=islamic");
        currentHijriCalendar = Calendar.getInstance(locale);
        todayHijriCalendar = Calendar.getInstance(locale);
    }

    private void setupListeners() {
        btnPrevMonth.setOnClickListener(v -> {
            currentHijriCalendar.add(Calendar.MONTH, -1);
            updateCalendarUI();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentHijriCalendar.add(Calendar.MONTH, 1);
            updateCalendarUI();
        });
    }

    private void updateCalendarUI() {
        int hijriMonthIndex = currentHijriCalendar.get(Calendar.MONTH);
        int hijriYear = currentHijriCalendar.get(Calendar.YEAR);

        tvHijriMonthYear.setText(hijriMonths[hijriMonthIndex] + " " + hijriYear + " H");

        // Clone to calculate start of month
        Calendar monthStart = (Calendar) currentHijriCalendar.clone();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        
        Calendar monthEnd = (Calendar) currentHijriCalendar.clone();
        monthEnd.set(Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH));

        SimpleDateFormat masehiFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String masehiStart = masehiFormat.format(monthStart.getTime());
        String masehiEnd = masehiFormat.format(monthEnd.getTime());
        
        if (masehiStart.equals(masehiEnd)) {
            tvMasehiMonthYear.setText(masehiStart);
        } else {
            tvMasehiMonthYear.setText(masehiStart + " - " + masehiEnd);
        }

        generateCalendarGrid(monthStart);
        generateEvents(hijriMonthIndex);
    }

    private void generateCalendarGrid(Calendar monthStart) {
        List<CalendarGridAdapter.CalendarDay> days = new ArrayList<>();
        int maxDays = monthStart.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Find which day of the week the 1st falls on (Sunday = 1, Monday = 2, ...)
        int startDayOfWeek = monthStart.get(Calendar.DAY_OF_WEEK);
        
        // Add empty cells for preceding days
        for (int i = 1; i < startDayOfWeek; i++) {
            days.add(new CalendarGridAdapter.CalendarDay(0, 0, false));
        }
        
        Calendar tempCal = (Calendar) monthStart.clone();
        for (int i = 1; i <= maxDays; i++) {
            boolean isToday = false;
            if (tempCal.get(Calendar.YEAR) == todayHijriCalendar.get(Calendar.YEAR) &&
                tempCal.get(Calendar.MONTH) == todayHijriCalendar.get(Calendar.MONTH) &&
                tempCal.get(Calendar.DAY_OF_MONTH) == todayHijriCalendar.get(Calendar.DAY_OF_MONTH)) {
                isToday = true;
            }
            
            // Get Masehi Date
            java.util.Calendar masehiCal = java.util.Calendar.getInstance();
            masehiCal.setTime(tempCal.getTime());
            int masehiDay = masehiCal.get(java.util.Calendar.DAY_OF_MONTH);
            
            days.add(new CalendarGridAdapter.CalendarDay(i, masehiDay, isToday));
            tempCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        CalendarGridAdapter adapter = new CalendarGridAdapter(getContext(), days);
        rvCalendar.setAdapter(adapter);
    }

    private void generateEvents(int monthIndex) {
        List<CalendarEventAdapter.CalendarEvent> events = new ArrayList<>();
        
        // Some static events as an example. In a real app, this should be comprehensive.
        if (monthIndex == 0) { // Muharram
            events.add(new CalendarEventAdapter.CalendarEvent("1 Muh", "Tahun Baru Hijriah", ""));
            events.add(new CalendarEventAdapter.CalendarEvent("10 Muh", "Puasa Asyura", ""));
        } else if (monthIndex == 2) { // Rabi' al-Awwal
            events.add(new CalendarEventAdapter.CalendarEvent("12 Rab", "Maulid Nabi Muhammad SAW", ""));
        } else if (monthIndex == 6) { // Rajab
            events.add(new CalendarEventAdapter.CalendarEvent("27 Raj", "Isra' Mi'raj", ""));
        } else if (monthIndex == 8) { // Ramadhan
            events.add(new CalendarEventAdapter.CalendarEvent("1 Ram", "Awal Puasa Ramadhan", ""));
            events.add(new CalendarEventAdapter.CalendarEvent("17 Ram", "Nuzulul Qur'an", ""));
        } else if (monthIndex == 9) { // Syawal
            events.add(new CalendarEventAdapter.CalendarEvent("1 Sya", "Idul Fitri", ""));
        } else if (monthIndex == 11) { // Dzulhijjah
            events.add(new CalendarEventAdapter.CalendarEvent("9 Dzul", "Puasa Arafah", ""));
            events.add(new CalendarEventAdapter.CalendarEvent("10 Dzul", "Idul Adha", ""));
        }

        CalendarEventAdapter adapter = new CalendarEventAdapter(getContext(), events);
        rvEvents.setAdapter(adapter);
    }
}
