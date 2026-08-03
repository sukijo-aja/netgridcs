package com.androidstarter.app.ui.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.androidstarter.app.R;
import com.androidstarter.app.utils.PinManager;

/**
 * Reusable PIN Activity supporting 3 modes: VERIFY, SET, CHANGE.
 *
 * Launch with:
 *   Intent intent = new Intent(this, PinActivity.class);
 *   intent.putExtra(PinActivity.EXTRA_MODE, PinActivity.MODE_SET);
 *   startActivityForResult(intent, REQUEST_CODE_PIN);
 *
 * Result: RESULT_OK on success, RESULT_CANCELED on cancel/fail.
 */
public class PinActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "pin_mode";
    public static final int MODE_VERIFY = 0;
    public static final int MODE_SET = 1;
    public static final int MODE_CHANGE = 2;

    private static final int PIN_LENGTH = 4;

    private int mode;
    private final StringBuilder currentInput = new StringBuilder();
    private String firstPin = null; // used for SET confirm step
    private boolean isConfirmStep = false;
    private boolean isOldPinStep = true; // used for CHANGE mode

    private View[] dots;
    private TextView tvTitle;
    private TextView tvMessage;
    private PinManager pinManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        pinManager = PinManager.getInstance(this);
        mode = getIntent().getIntExtra(EXTRA_MODE, MODE_VERIFY);

        tvTitle = findViewById(R.id.tvPinTitle);
        tvMessage = findViewById(R.id.tvPinMessage);
        dots = new View[]{
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3),
                findViewById(R.id.dot4)
        };

        updateTitle();
        checkLockout();
        setupNumPad();
    }

    private void updateTitle() {
        switch (mode) {
            case MODE_SET:
                tvTitle.setText(isConfirmStep ? "Confirm PIN" : "Create PIN");
                break;
            case MODE_CHANGE:
                tvTitle.setText(isOldPinStep ? "Enter Current PIN" : (isConfirmStep ? "Confirm New PIN" : "Enter New PIN"));
                break;
            case MODE_VERIFY:
            default:
                tvTitle.setText("Enter PIN");
                break;
        }
    }

    private void setupNumPad() {
        int[] numBtnIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };

        for (int id : numBtnIds) {
            findViewById(id).setOnClickListener(v -> {
                if (pinManager.isLockedOut()) {
                    checkLockout();
                    return;
                }
                com.google.android.material.button.MaterialButton btn = (com.google.android.material.button.MaterialButton) v;
                onDigitPressed(btn.getText().toString());
            });
        }

        findViewById(R.id.btnBackspace).setOnClickListener(v -> onBackspace());
        findViewById(R.id.btnCancel).setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void onDigitPressed(String digit) {
        if (currentInput.length() >= PIN_LENGTH) return;

        currentInput.append(digit);
        updateDots();

        if (currentInput.length() == PIN_LENGTH) {
            // Small delay for visual feedback
            dots[PIN_LENGTH - 1].postDelayed(this::onPinComplete, 200);
        }
    }

    private void onBackspace() {
        if (currentInput.length() > 0) {
            currentInput.deleteCharAt(currentInput.length() - 1);
            updateDots();
        }
    }

    private void updateDots() {
        for (int i = 0; i < PIN_LENGTH; i++) {
            dots[i].setBackgroundResource(
                    i < currentInput.length() ? R.drawable.pin_dot_filled : R.drawable.pin_dot_empty
            );
        }
    }

    private void onPinComplete() {
        String pin = currentInput.toString();
        currentInput.setLength(0);

        switch (mode) {
            case MODE_VERIFY:
                handleVerify(pin);
                break;
            case MODE_SET:
                handleSet(pin);
                break;
            case MODE_CHANGE:
                handleChange(pin);
                break;
        }
    }

    private void handleVerify(String pin) {
        if (pinManager.verifyPin(pin)) {
            setResult(RESULT_OK);
            finish();
        } else {
            showError("Wrong PIN. " + pinManager.getRemainingAttempts() + " attempts remaining.");
            updateDots();
            checkLockout();
        }
    }

    private void handleSet(String pin) {
        if (!isConfirmStep) {
            firstPin = pin;
            isConfirmStep = true;
            updateTitle();
            updateDots();
        } else {
            if (pin.equals(firstPin)) {
                pinManager.setPin(pin);
                Toast.makeText(this, "PIN set successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                showError("PINs do not match. Try again.");
                isConfirmStep = false;
                firstPin = null;
                updateTitle();
                updateDots();
            }
        }
    }

    private void handleChange(String pin) {
        if (isOldPinStep) {
            if (pinManager.verifyPin(pin)) {
                isOldPinStep = false;
                updateTitle();
                updateDots();
            } else {
                showError("Wrong PIN. " + pinManager.getRemainingAttempts() + " attempts remaining.");
                updateDots();
                checkLockout();
            }
        } else if (!isConfirmStep) {
            firstPin = pin;
            isConfirmStep = true;
            updateTitle();
            updateDots();
        } else {
            if (pin.equals(firstPin)) {
                pinManager.setPin(pin);
                Toast.makeText(this, "PIN changed successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                showError("PINs do not match. Try again.");
                isConfirmStep = false;
                firstPin = null;
                updateTitle();
                updateDots();
            }
        }
    }

    private void showError(String message) {
        tvMessage.setText(message);
        tvMessage.setVisibility(View.VISIBLE);
        tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));
    }

    private void checkLockout() {
        if (pinManager.isLockedOut()) {
            long remaining = pinManager.getLockoutRemainingMs();
            tvMessage.setVisibility(View.VISIBLE);
            new CountDownTimer(remaining, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    tvMessage.setText("Too many attempts. Try again in " + (millisUntilFinished / 1000) + "s");
                    tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));
                }

                @Override
                public void onFinish() {
                    tvMessage.setVisibility(View.GONE);
                }
            }.start();
        }
    }
}
