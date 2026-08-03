package com.androidstarter.app.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import androidx.annotation.NonNull;

public class NetworkMonitor {

    public interface NetworkCallbackListener {
        void onNetworkAvailable();
        void onNetworkLost();
    }

    private final ConnectivityManager connectivityManager;
    private final ConnectivityManager.NetworkCallback networkCallback;
    private boolean isRegistered = false;

    public NetworkMonitor(Context context, NetworkCallbackListener listener) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                if (listener != null) {
                    listener.onNetworkAvailable();
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                if (listener != null) {
                    listener.onNetworkLost();
                }
            }
        };
    }

    public void registerNetworkCallback() {
        if (!isRegistered && connectivityManager != null) {
            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
            isRegistered = true;
        }
    }

    public void unregisterNetworkCallback() {
        if (isRegistered && connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
            isRegistered = false;
        }
    }

    public boolean isNetworkAvailable() {
        if (connectivityManager == null) return false;
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }
}
