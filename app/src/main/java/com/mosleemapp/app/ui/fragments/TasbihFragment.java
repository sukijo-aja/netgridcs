package com.mosleemapp.app.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.mosleemapp.app.R;
import com.mosleemapp.app.ui.adapters.TasbihAdapter;
import com.mosleemapp.app.ui.adapters.TasbihAdapter.TasbihItem;

import com.mosleemapp.app.utils.AdMobUtil;

import java.util.ArrayList;
import java.util.List;

public class TasbihFragment extends Fragment {

    private static final String PREFS_NAME = "TasbihPrefs";
    private TasbihAdapter adapter;
    private List<TasbihItem> tasbihList;
    private com.mosleemapp.app.utils.AppPreference prefs;
    
    // Main UI Elements
    private TextView tvActiveName;
    private com.mosleemapp.app.ui.custom.OdometerCounterView tvActiveCount;
    private CardView btnTap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tasbih, container, false);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvActiveName = view.findViewById(R.id.tvActiveName);
        tvActiveCount = view.findViewById(R.id.tvActiveCount);
        btnTap = view.findViewById(R.id.btnTap);
        RecyclerView rvTasbihList = view.findViewById(R.id.rvTasbihList);
        MaterialButton btnResetAll = view.findViewById(R.id.btnResetAll);
        
        com.google.android.material.appbar.MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Digital Tasbih");
            toolbar.setNavigationOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().popBackStack();
            });
            toolbar.inflateMenu(R.menu.menu_tasbih);
        }

        // OdometerCounterView handles its own view factory/animations internally

        prefs = new com.mosleemapp.app.utils.AppPreference(requireContext(), PREFS_NAME);

        // Initialize Data
        initializeData();
        
        // Sound Menu Item
        if (toolbar != null) {
            android.view.MenuItem soundItem = toolbar.getMenu().findItem(R.id.action_sound);
            if (soundItem != null) {
                updateSoundMenuIcon(soundItem);
                toolbar.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_sound) {
                        boolean isSoundEnabled = prefs.getBoolean("sound_enabled", true);
                        prefs.saveBoolean("sound_enabled", !isSoundEnabled);
                        updateSoundMenuIcon(item);
                        return true;
                    }
                    return false;
                });
            }
        }

        // Setup Adapter
        adapter = new TasbihAdapter(tasbihList, new TasbihAdapter.OnTasbihInteractionListener() {
            @Override
            public void onItemSelect(TasbihItem item, int position) {
                adapter.setSelectedPosition(position);
                 // Reset animation for selection change to avoid weird transitions or just set current text
                tvActiveCount.setValue(item.getCount(), false); // No animation on select
                tvActiveName.setText(item.getName());
            }

            @Override
            public void onReset(TasbihItem item, int position) {
                item.setCount(0);
                adapter.notifyItemChanged(position);
                saveCount(item);
                
                // If the reset item is the currently selected one, update the main display
                if (position == adapter.getSelectedPosition()) {
                    updateMainDisplay(item);
                }
            }
        });

        rvTasbihList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTasbihList.setAdapter(adapter);
        
        // Initialize Main Display with first item
        if (!tasbihList.isEmpty()) {
            tvActiveCount.setValue(tasbihList.get(0).getCount(), false);
            tvActiveName.setText(tasbihList.get(0).getName());
        }

    // Tap Button Logic
        btnTap.setOnClickListener(v -> {
            stopPulseAnimation(); // Stop animation on interaction
            TasbihItem activeItem = adapter.getSelectedItem();
            if (activeItem != null) {
                activeItem.setCount(activeItem.getCount() + 1);
                updateMainDisplay(activeItem);
                saveCount(activeItem);
                adapter.updateList(adapter.getSelectedPosition()); // Update list count UI
                playSound();
            }
        });

        // Global Reset
        btnResetAll.setOnClickListener(v -> {
            for (TasbihItem item : tasbihList) {
                item.setCount(0);
                saveCount(item);
            }
            adapter.notifyDataSetChanged();
            
            // Update main display to 0
            TasbihItem activeItem = adapter.getSelectedItem();
            if (activeItem != null) {
                updateMainDisplay(activeItem);
                startPulseAnimation(); // Restart animation if reset to 0
            }
        });

        // Start animation if initial count is 0
        if (tasbihList.isEmpty() || tasbihList.get(0).getCount() == 0) {
            startPulseAnimation();
        }


        // AdMob

//        btnTap.setOnLongClickListener(v -> {
//            startSimulation();
//            return true;
//        });
    }
    
    // Simulation Logic
//    private android.os.Handler simulationHandler = new android.os.Handler(android.os.Looper.getMainLooper());
//    private Runnable simulationRunnable;
//    private boolean isSimulating = false;
//
//    private void startSimulation() {
//        if (isSimulating) {
//            // Stop if already running
//            isSimulating = false;
//            if (simulationRunnable != null) simulationHandler.removeCallbacks(simulationRunnable);
//            return;
//        }
//
//        isSimulating = true;
//        TasbihItem activeItem = adapter.getSelectedItem();
//        if (activeItem == null) return;
//
//        // Start from 100
//        activeItem.setCount(1);
//        updateMainDisplay(activeItem);
//
//        simulationRunnable = new Runnable() {
//            @Override
//            public void run() {
//                if (!isSimulating || activeItem.getCount() >= 999) {
//                    isSimulating = false;
//                    saveCount(activeItem); // Save final state
//                    return;
//                }
//
//                activeItem.setCount(activeItem.getCount() + 1);
//                updateMainDisplay(activeItem);
//
//                // Speed: 50ms per number
//                simulationHandler.postDelayed(this, 1);
//            }
//        };
//
//        simulationHandler.post(simulationRunnable);
//        android.widget.Toast.makeText(this, "Simulation Started (100 -> 999)", android.widget.Toast.LENGTH_SHORT).show();
//    }
    
    private void updateSoundMenuIcon(android.view.MenuItem item) {
        boolean isSoundEnabled = prefs.getBoolean("sound_enabled", false);
        if (isSoundEnabled) {
            item.setIcon(R.drawable.ic_volume_up);
        } else {
            item.setIcon(R.drawable.ic_volume_off);
        }
    }
    
    private void playSound() {
         boolean isSoundEnabled = prefs.getBoolean("sound_enabled", false);
         if (isSoundEnabled) {
             try {
                android.media.ToneGenerator toneGen = new android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100);
                toneGen.startTone(ToneGenerator.TONE_SUP_DIAL, 150);
                // toneGen.release(); // Should ideally release but for rapid taps keeping it or managing lifecycle is better. 
                // Creating new instance every tap is okay for occasional use but maybe not optimal for spamming.
                // However, TONE_PROP_BEEP is short. Let's rely on garbage collection for now or make it a member if reused often.
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
    }
    
    private void updateMainDisplay(TasbihItem item) {
        tvActiveName.setText(item.getName());
        tvActiveCount.setValue(item.getCount(),false); // Defaults to animate=true
    }

    private void initializeData() {
        tasbihList = new ArrayList<>();
        // Default items
        tasbihList.add(new TasbihItem("subhanallah", "Subhanallah", 0));
        tasbihList.add(new TasbihItem("alhamdulillah", "Alhamdulillah", 0));
        tasbihList.add(new TasbihItem("allahuakbar", "Allahu Akbar", 0));
        tasbihList.add(new TasbihItem("lailahaillallah", "La ilaha ilallah", 0));
        tasbihList.add(new TasbihItem("astaghfirullah", "Astaghfirullah", 0));
//        tasbihList.add(new TasbihItem("custom", "Custom / Others", 0));

        // Load saved counts
        for (TasbihItem item : tasbihList) {
            item.setCount(prefs.getInt("count_" + item.getId(), 0));
        }
    }

    private void saveCount(TasbihItem item) {
        prefs.saveInt("count_" + item.getId(), item.getCount());
    }
    
    // Animation Logic
    private android.animation.ObjectAnimator scaleX, scaleY;
    
    private void startPulseAnimation() {
        if (scaleX != null && scaleX.isRunning()) return;

        scaleX = android.animation.ObjectAnimator.ofFloat(btnTap, "scaleX", 1f, 1.1f, 1f);
        scaleY = android.animation.ObjectAnimator.ofFloat(btnTap, "scaleY", 1f, 1.1f, 1f);

        scaleX.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        
        scaleX.setDuration(1000);
        scaleY.setDuration(1000);

        android.animation.AnimatorSet set = new android.animation.AnimatorSet();
        set.play(scaleX).with(scaleY);
        set.start();
    }
    
    private void stopPulseAnimation() {
        if (scaleX != null) {
            scaleX.cancel();
            scaleX = null;
        }
        if (scaleY != null) {
             scaleY.cancel();
             scaleY = null;
        }
        // Reset scale to normal
        btnTap.setScaleX(1f);
        btnTap.setScaleY(1f);
    }
}
