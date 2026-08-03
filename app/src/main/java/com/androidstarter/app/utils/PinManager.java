package com.androidstarter.app.utils;

import android.content.Context;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PIN management utility with secure hashed storage.
 * PIN is stored as SHA-256 hash in EncryptedSharedPreferences.
 *
 * Usage:
 *   PinManager pin = PinManager.getInstance(context);
 *   pin.setPin("1234");
 *   pin.verifyPin("1234"); // true
 *   pin.isPinEnabled();    // true
 *   pin.clearPin();
 */
public class PinManager {

    private static final String KEY_PIN_HASH = "app_pin_hash";
    private static final String KEY_PIN_ENABLED = "app_pin_enabled";
    private static final String KEY_FAILED_ATTEMPTS = "pin_failed_attempts";
    private static final String KEY_LOCK_UNTIL = "pin_lock_until";
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MS = 60_000; // 1 minute

    private static PinManager instance;
    private final AppPreference securePref;

    private PinManager(Context context) {
        securePref = AppPreference.encrypted(context);
    }

    public static synchronized PinManager getInstance(Context context) {
        if (instance == null) {
            instance = new PinManager(context.getApplicationContext());
        }
        return instance;
    }

    /** Set a new PIN (stores SHA-256 hash). */
    public void setPin(String pin) {
        securePref.saveString(KEY_PIN_HASH, hashPin(pin));
        securePref.saveBoolean(KEY_PIN_ENABLED, true);
        resetFailedAttempts();
    }

    /** Verify a PIN against the stored hash. Returns false if locked out. */
    public boolean verifyPin(String pin) {
        if (isLockedOut()) return false;

        String storedHash = securePref.getString(KEY_PIN_HASH, null);
        if (storedHash == null) return false;

        boolean match = storedHash.equals(hashPin(pin));
        if (match) {
            resetFailedAttempts();
        } else {
            incrementFailedAttempts();
        }
        return match;
    }

    /** Check if a PIN has been set and is enabled. */
    public boolean isPinEnabled() {
        return securePref.getBoolean(KEY_PIN_ENABLED, false)
                && securePref.checkKey(KEY_PIN_HASH);
    }

    /** Enable or disable PIN without clearing it. */
    public void setPinEnabled(boolean enabled) {
        securePref.saveBoolean(KEY_PIN_ENABLED, enabled);
    }

    /** Remove the PIN entirely. */
    public void clearPin() {
        securePref.remove(KEY_PIN_HASH);
        securePref.saveBoolean(KEY_PIN_ENABLED, false);
        resetFailedAttempts();
    }

    /** Change PIN: verify old, then set new. */
    public boolean changePin(String oldPin, String newPin) {
        if (verifyPin(oldPin)) {
            setPin(newPin);
            return true;
        }
        return false;
    }

    /** Get remaining failed attempts before lockout. */
    public int getRemainingAttempts() {
        return MAX_ATTEMPTS - securePref.getInt(KEY_FAILED_ATTEMPTS, 0);
    }

    /** Check if the user is currently locked out. */
    public boolean isLockedOut() {
        long lockUntil = securePref.getLong(KEY_LOCK_UNTIL, 0);
        if (System.currentTimeMillis() < lockUntil) {
            return true;
        }
        if (lockUntil > 0) {
            // Lock expired, reset
            resetFailedAttempts();
        }
        return false;
    }

    /** Get remaining lockout time in milliseconds. */
    public long getLockoutRemainingMs() {
        long lockUntil = securePref.getLong(KEY_LOCK_UNTIL, 0);
        long remaining = lockUntil - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    private void incrementFailedAttempts() {
        int attempts = securePref.getInt(KEY_FAILED_ATTEMPTS, 0) + 1;
        securePref.saveInt(KEY_FAILED_ATTEMPTS, attempts);
        if (attempts >= MAX_ATTEMPTS) {
            securePref.saveLong(KEY_LOCK_UNTIL, System.currentTimeMillis() + LOCK_DURATION_MS);
        }
    }

    private void resetFailedAttempts() {
        securePref.saveInt(KEY_FAILED_ATTEMPTS, 0);
        securePref.remove(KEY_LOCK_UNTIL);
    }

    private String hashPin(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pin.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
