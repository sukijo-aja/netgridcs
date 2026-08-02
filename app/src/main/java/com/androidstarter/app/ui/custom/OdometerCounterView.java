package com.androidstarter.app.ui.custom;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.androidstarter.app.R;

import java.util.ArrayList;
import java.util.List;

public class OdometerCounterView extends LinearLayout {

    private List<TextSwitcher> digitSwitchers = new ArrayList<>();
    private String currentText = "";
    private Animation inAnim, outAnim;
    private int textColor = Color.BLACK;

    public OdometerCounterView(Context context) {
        super(context);
        init(context);
    }

    public OdometerCounterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OdometerCounterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        
        inAnim = AnimationUtils.loadAnimation(context, R.anim.slide_in_up);
        outAnim = AnimationUtils.loadAnimation(context, R.anim.slide_out_up);
        
        textColor = ContextCompat.getColor(context, R.color.primary_green);
    }

    public void setValue(int value) {
        setValue(value, true);
    }

    public void setValue(int value, boolean animate) {
        String newValueStr = String.valueOf(value);
        
        int diff = newValueStr.length() - digitSwitchers.size();
        
        if (diff > 0) {
            for (int i = 0; i < diff; i++) {
                addDigitSwitcher();
            }
        } else if (diff < 0) {
            int toRemove = Math.abs(diff);
            for (int i = 0; i < toRemove; i++) {
                removeViewAt(0);
                digitSwitchers.remove(0);
            }
        }

        // 2. Update digits
        for (int i = 0; i < newValueStr.length(); i++) {
            char newDigit = newValueStr.charAt(i);
            TextSwitcher switcher = digitSwitchers.get(i);
            
            TextView currentView = (TextView) switcher.getCurrentView();
            String currentDigitStr = currentView != null ? currentView.getText().toString() : "";
            String newDigitStr = String.valueOf(newDigit);
            
            if (!currentDigitStr.equals(newDigitStr)) {
                if (animate) {
                    switcher.setText(newDigitStr);
                } else {
                    switcher.setCurrentText(newDigitStr);
                }
            }
        }
        
        currentText = newValueStr;
    }

    private void addDigitSwitcher() {
        TextSwitcher switcher = new TextSwitcher(getContext());
        switcher.setFactory(() -> {
            TextView textView = new TextView(getContext());
            textView.setTextSize(80); // Match previous design 80sp
            textView.setTypeface(null, Typeface.BOLD);
            textView.setTextColor(textColor);
            textView.setGravity(Gravity.CENTER);
            return textView;
        });
        
//        switcher.setInAnimation(inAnim);
//        switcher.setOutAnimation(outAnim);
        
        digitSwitchers.add(switcher);
        addView(switcher);
    }
}
