package com.androidstarter.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.androidstarter.app.R;

public class NotificationDetailFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_detail, container, false);
        
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Detail Notifikasi");
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }
        
        TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
        TextView tvDate = view.findViewById(R.id.tvDetailDate);
        TextView tvMessage = view.findViewById(R.id.tvDetailMessage);
        
        if (getArguments() != null) {
            tvTitle.setText(getArguments().getString("title", ""));
            tvDate.setText(getArguments().getString("date", ""));
            tvMessage.setText(getArguments().getString("message", ""));
        }
        
        return view;
    }
}
