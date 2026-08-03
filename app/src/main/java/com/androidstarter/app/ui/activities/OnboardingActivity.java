package com.androidstarter.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;
import com.androidstarter.app.MainActivity;
import com.androidstarter.app.R;
import com.androidstarter.app.data.model.OnboardingItem;
import com.androidstarter.app.databinding.ActivityOnboardingBinding;
import com.androidstarter.app.ui.adapters.OnboardingAdapter;
import com.androidstarter.app.utils.AppPreference;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;
    private OnboardingAdapter onboardingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupOnboardingItems();
        setupViewPager();
        setupClickListeners();
    }

    private void setupOnboardingItems() {
        List<OnboardingItem> items = new ArrayList<>();
        items.add(new OnboardingItem(
                R.mipmap.ic_launcher,
                "Welcome to Android Starter",
                "A clean, robust, and modern foundation for your next Android project."
        ));
        items.add(new OnboardingItem(
                R.mipmap.ic_launcher,
                "Ready to Scale",
                "Built with MVVM, Room, Retrofit, and Firebase to handle production needs."
        ));
        items.add(new OnboardingItem(
                R.mipmap.ic_launcher,
                "Get Started",
                "Start building amazing features without worrying about boilerplate code."
        ));

        onboardingAdapter = new OnboardingAdapter(items);
    }

    private void setupViewPager() {
        binding.viewPager.setAdapter(onboardingAdapter);
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {}
        ).attach();

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == onboardingAdapter.getItemCount() - 1) {
                    binding.buttonNext.setText("Get Started");
                } else {
                    binding.buttonNext.setText("Next");
                }
            }
        });
    }

    private void setupClickListeners() {
        binding.textSkip.setOnClickListener(v -> navigateToMain());

        binding.buttonNext.setOnClickListener(v -> {
            if (binding.viewPager.getCurrentItem() + 1 < onboardingAdapter.getItemCount()) {
                binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
            } else {
                navigateToMain();
            }
        });
    }

    private void navigateToMain() {
        new AppPreference(this).saveBoolean("isFirstLaunch", false);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
