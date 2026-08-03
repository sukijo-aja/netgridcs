package com.androidstarter.app.ui.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.androidstarter.app.utils.NetworkMonitor;
import com.google.android.material.snackbar.Snackbar;

public class BaseActivity extends AppCompatActivity implements NetworkMonitor.NetworkCallbackListener {

    private NetworkMonitor networkMonitor;
    private Snackbar networkSnackbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkMonitor = new NetworkMonitor(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkMonitor.registerNetworkCallback();
        checkInitialNetworkState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        networkMonitor.unregisterNetworkCallback();
    }

    private void checkInitialNetworkState() {
        if (!networkMonitor.isNetworkAvailable()) {
            showNetworkLostSnackbar();
        }
    }

    @Override
    public void onNetworkAvailable() {
        runOnUiThread(() -> {
            if (networkSnackbar != null && networkSnackbar.isShown()) {
                networkSnackbar.dismiss();
            }
        });
    }

    @Override
    public void onNetworkLost() {
        runOnUiThread(this::showNetworkLostSnackbar);
    }

    private void showNetworkLostSnackbar() {
        networkSnackbar = Snackbar.make(findViewById(android.R.id.content), "No Internet Connection", Snackbar.LENGTH_INDEFINITE);
        networkSnackbar.setAction("Dismiss", v -> networkSnackbar.dismiss());
        networkSnackbar.show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
//        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
//            controller.hide(WindowInsetsCompat.Type.systemBars());
//            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }
}
