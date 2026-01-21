package com.mosleemapp.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mosleemapp.app.databinding.FragmentPrayerBinding;
import com.mosleemapp.app.ui.PrayerAdapter;
import com.mosleemapp.app.ui.viewmodel.PrayerViewModel;

import java.util.HashMap;
import java.util.Map;

public class PrayerFragment extends Fragment {

    private FragmentPrayerBinding binding;
    private PrayerViewModel viewModel;
    private PrayerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPrayerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup UI
        adapter = new PrayerAdapter();
        binding.rvPrayerTimes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPrayerTimes.setAdapter(adapter);

        // Setup ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(PrayerViewModel.class);
        
        // Observe Data
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getPrayerTimes().observe(getViewLifecycleOwner(), prayerTimeEntity -> {
            if (prayerTimeEntity != null) {
                Map<String, String> timings = new HashMap<>();
                timings.put("Fajr", prayerTimeEntity.fajr);
                timings.put("Dhuhr", prayerTimeEntity.dhuhr);
                timings.put("Asr", prayerTimeEntity.asr);
                timings.put("Maghrib", prayerTimeEntity.maghrib);
                timings.put("Isha", prayerTimeEntity.isha);
                adapter.setPrayerTimes(timings);
                
                binding.tvTitle.setText("Prayer Times");
            }
        });

        viewModel.getNextPrayerName().observe(getViewLifecycleOwner(), name -> {
            binding.tvNextPrayerName.setText(name);
        });

        viewModel.getNextPrayerTimeRemaining().observe(getViewLifecycleOwner(), time -> {
            binding.tvCountdown.setText(time);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
