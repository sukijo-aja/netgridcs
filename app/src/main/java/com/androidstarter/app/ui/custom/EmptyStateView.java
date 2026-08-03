package com.androidstarter.app.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.androidstarter.app.R;
import com.google.android.material.button.MaterialButton;

/**
 * Custom View for Empty State screens (No internet, empty list, search not found, etc.).
 * Includes icon, title, description, and an action button.
 */
public class EmptyStateView extends LinearLayout {

    private ImageView ivIcon;
    private TextView tvTitle;
    private TextView tvDescription;
    private MaterialButton btnAction;

    public EmptyStateView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public EmptyStateView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EmptyStateView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        
        LayoutInflater.from(context).inflate(R.layout.view_empty_state, this, true);

        ivIcon = findViewById(R.id.ivEmptyIcon);
        tvTitle = findViewById(R.id.tvEmptyTitle);
        tvDescription = findViewById(R.id.tvEmptyDescription);
        btnAction = findViewById(R.id.btnEmptyAction);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EmptyStateView, 0, 0);
            try {
                Drawable icon = a.getDrawable(R.styleable.EmptyStateView_emptyIcon);
                if (icon != null) {
                    ivIcon.setImageDrawable(icon);
                } else {
                    ivIcon.setVisibility(GONE);
                }

                String title = a.getString(R.styleable.EmptyStateView_emptyTitle);
                setTitle(title);

                String description = a.getString(R.styleable.EmptyStateView_emptyDescription);
                setDescription(description);

                boolean showButton = a.getBoolean(R.styleable.EmptyStateView_showEmptyButton, false);
                String buttonText = a.getString(R.styleable.EmptyStateView_emptyButtonText);
                setButton(showButton, buttonText);
            } finally {
                a.recycle();
            }
        }
    }

    // ---- Setters ----

    public void setIcon(@DrawableRes int resId) {
        ivIcon.setImageResource(resId);
        ivIcon.setVisibility(resId != 0 ? VISIBLE : GONE);
    }

    public void setIcon(@Nullable Drawable drawable) {
        ivIcon.setImageDrawable(drawable);
        ivIcon.setVisibility(drawable != null ? VISIBLE : GONE);
    }

    public void setTitle(@Nullable String title) {
        tvTitle.setText(title);
        tvTitle.setVisibility(title != null && !title.isEmpty() ? VISIBLE : GONE);
    }

    public void setTitle(@StringRes int resId) {
        setTitle(getContext().getString(resId));
    }

    public void setDescription(@Nullable String description) {
        tvDescription.setText(description);
        tvDescription.setVisibility(description != null && !description.isEmpty() ? VISIBLE : GONE);
    }

    public void setDescription(@StringRes int resId) {
        setDescription(getContext().getString(resId));
    }

    public void setButton(boolean show, @Nullable String buttonText) {
        btnAction.setVisibility(show ? VISIBLE : GONE);
        if (show && buttonText != null) {
            btnAction.setText(buttonText);
        }
    }

    public void setOnButtonClickListener(@Nullable OnClickListener listener) {
        btnAction.setOnClickListener(listener);
    }

    /**
     * Presets: Quick-configure the state for pre-defined scenarios.
     */
    
    public void showNoInternet(OnClickListener onRetryClick) {
        setIcon(android.R.drawable.ic_dialog_alert);
        setTitle("No Internet Connection");
        setDescription("Please check your connection and try again.");
        setButton(true, "Retry");
        setOnButtonClickListener(onRetryClick);
        setVisibility(VISIBLE);
    }

    public void showEmptyList(String title, String description, @Nullable String buttonText, @Nullable OnClickListener onActionClick) {
        setIcon(android.R.drawable.ic_menu_search);
        setTitle(title);
        setDescription(description);
        if (buttonText != null && onActionClick != null) {
            setButton(true, buttonText);
            setOnButtonClickListener(onActionClick);
        } else {
            setButton(false, null);
        }
        setVisibility(VISIBLE);
    }

    public void showError(String errorMessage, OnClickListener onRetryClick) {
        setIcon(android.R.drawable.stat_notify_error);
        setTitle("An Error Occurred");
        setDescription(errorMessage);
        setButton(true, "Retry");
        setOnButtonClickListener(onRetryClick);
        setVisibility(VISIBLE);
    }
}
