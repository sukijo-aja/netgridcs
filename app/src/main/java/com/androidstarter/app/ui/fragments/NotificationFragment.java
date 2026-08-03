package com.androidstarter.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.widget.TextView;
import com.androidstarter.app.R;

public class NotificationFragment extends Fragment {

    private RecyclerView rvNotifications;
    private TextView tvEmptyState;
    private com.androidstarter.app.ui.adapters.NotificationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Notifikasi");
            toolbar.setNavigationOnClickListener(v -> {
                com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = 
                    requireActivity().findViewById(R.id.bottom_navigation);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_home);
                } else if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }
        
        rvNotifications = view.findViewById(R.id.rvNotifications);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        
        rvNotifications.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        adapter = new com.androidstarter.app.ui.adapters.NotificationAdapter((notification, formattedDate) -> {
            
            // Mark as read in background
            if (!notification.isRead) {
                com.androidstarter.app.data.local.AppDatabase.databaseWriteExecutor.execute(() -> {
                    com.androidstarter.app.data.local.AppDatabase db = com.androidstarter.app.data.local.AppDatabase.getDatabase(requireContext());
                    db.notificationDao().markAsRead(notification.id);
                });
            }
            
            String actionType = notification.actionType != null ? notification.actionType : "info";
            
            switch (actionType) {
                case "settings":
                    com.androidstarter.app.ui.fragments.SettingsFragment settingsFragment = new com.androidstarter.app.ui.fragments.SettingsFragment();
                    if (getActivity() instanceof com.androidstarter.app.MainActivity) {
                        ((com.androidstarter.app.MainActivity) getActivity()).loadFragmentWithBackStack(settingsFragment);
                    }
                    break;
                case "info":
                default:
                    com.androidstarter.app.ui.fragments.NotificationDetailFragment detailFragment = new com.androidstarter.app.ui.fragments.NotificationDetailFragment();
                    android.os.Bundle args = new android.os.Bundle();
                    args.putString("title", notification.title);
                    args.putString("message", notification.message);
                    args.putString("date", formattedDate);
                    detailFragment.setArguments(args);
                    
                    if (getActivity() instanceof com.androidstarter.app.MainActivity) {
                        ((com.androidstarter.app.MainActivity) getActivity()).loadFragmentWithBackStack(detailFragment);
                    }
                    break;
            }
        });
        rvNotifications.setAdapter(adapter);
        
        loadNotifications();
        
        return view;
    }
    
    private void loadNotifications() {
        com.androidstarter.app.data.local.AppDatabase db = com.androidstarter.app.data.local.AppDatabase.getDatabase(requireContext());
        db.notificationDao().getAllNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if (notifications != null && !notifications.isEmpty()) {
                adapter.setNotifications(notifications);
                rvNotifications.setVisibility(View.VISIBLE);
                tvEmptyState.setVisibility(View.GONE);
            } else {
                rvNotifications.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }
}
