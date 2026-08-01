package com.mosleemapp.app.ui.fragments;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.mosleemapp.app.R;
import com.mosleemapp.app.data.repository.KhutbahRepository;

public class KhutbahDetailFragment extends Fragment {

    private static final String ARG_KHUTBAH_ID = "khutbah_id";
    
    private int khutbahId;
    private KhutbahRepository repository;

    public static KhutbahDetailFragment newInstance(int id) {
        KhutbahDetailFragment fragment = new KhutbahDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_KHUTBAH_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            khutbahId = getArguments().getInt(ARG_KHUTBAH_ID);
        }
        repository = new KhutbahRepository(requireActivity().getApplication());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_khutbah_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Detail Khutbah");
            toolbar.setNavigationOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        }

        TextView tvKhutbahTitle = view.findViewById(R.id.tvKhutbahTitle);
        TextView tvKhotib = view.findViewById(R.id.tvKhotib);
        TextView tvDate = view.findViewById(R.id.tvDate);
        TextView tvContent = view.findViewById(R.id.tvContent);
        TextView tvSource = view.findViewById(R.id.tvSource);

        repository.getKhutbahById(khutbahId).observe(getViewLifecycleOwner(), khutbah -> {
            if (khutbah != null) {
                tvKhutbahTitle.setText(khutbah.title);
                tvKhotib.setText(khutbah.khotib != null ? khutbah.khotib : "-");
                tvDate.setText(khutbah.date != null ? khutbah.date : "");
                
                String contentText = khutbah.content != null ? khutbah.content : khutbah.description;
                if (contentText != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        tvContent.setText(Html.fromHtml(contentText, Html.FROM_HTML_MODE_LEGACY));
                    } else {
                        tvContent.setText(Html.fromHtml(contentText));
                    }
                }
                
                if (khutbah.source != null && !khutbah.source.isEmpty()) {
                    tvSource.setText("Sumber: " + khutbah.source);
                    tvSource.setVisibility(View.VISIBLE);
                } else {
                    tvSource.setVisibility(View.GONE);
                }
            }
        });
    }
}
