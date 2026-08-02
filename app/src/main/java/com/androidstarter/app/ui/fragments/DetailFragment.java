package com.androidstarter.app.ui.fragments;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.androidstarter.app.R;

public class DetailFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String title = getArguments() != null ? getArguments().getString("EXTRA_TITLE") : null;
        
        TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
        if (title != null) {
            tvTitle.setText(title);
        }
        
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
        
        // AdMob
    }
}
