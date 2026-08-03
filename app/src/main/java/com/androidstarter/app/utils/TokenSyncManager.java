package com.androidstarter.app.utils;

import android.content.Context;
import android.util.Log;

import com.androidstarter.app.R;
import com.androidstarter.app.data.model.DeviceTokenRequest;
import com.androidstarter.app.data.remote.services.DeviceApiService;
import com.google.firebase.auth.FirebaseAuth;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TokenSyncManager {

    private static final String TAG = "TokenSyncManager";

    /**
     * Memeriksa dan mengirimkan FCM Token ke server jika ada perubahan 
     * atau dipaksa (dengan menghapus last_synced_token).
     * @param context Context aplikasi
     */
    public static void syncTokenIfNeeded(Context context) {
        AppPreference appPref = new AppPreference(context);
        
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            // User belum login, tidak perlu memetakan token ke server
            Log.d(TAG, "User not logged in, skipping sync.");
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();
        String fcmToken = appPref.getString("fcm_token", "");
        String fid = appPref.getString("firebase_install_id", "");
        String lastSyncedToken = appPref.getString("last_synced_token", "");

        if (fcmToken.isEmpty() || fid.isEmpty()) {
            Log.d(TAG, "Token or FID is empty, skipping sync.");
            return; 
        }

        // Caching: Hanya kirim jika token berbeda dari yang terakhir sukses terkirim
        if (fcmToken.equals(lastSyncedToken)) {
            Log.d(TAG, "Token already synced, skipping API call.");
            return;
        }

        try {
            String serverUrl = appPref.getString("server_url", context.getString(R.string.default_server_url));
            if (serverUrl == null || serverUrl.trim().isEmpty()) {
                serverUrl = context.getString(R.string.default_server_url);
            } else if (!serverUrl.endsWith("/")) {
                serverUrl += "/";
            }
    
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(serverUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
    
            DeviceApiService apiService = retrofit.create(DeviceApiService.class);
            DeviceTokenRequest request = new DeviceTokenRequest(uid, fid, fcmToken, email != null ? email : "");
    
            apiService.sendDeviceTokens(request).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Sync Success: Token synced to server.");
                        // Simpan status sukses agar tidak dikirim berulang kali tanpa alasan
                        appPref.saveString("last_synced_token", fcmToken); 
                    } else {
                        Log.e(TAG, "Sync Failed: " + response.code());
                    }
                }
    
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "Sync Error", t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception during token sync request", e);
        }
    }
}
