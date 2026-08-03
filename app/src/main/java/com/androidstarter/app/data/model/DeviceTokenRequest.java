package com.androidstarter.app.data.model;

import com.google.gson.annotations.SerializedName;

public class DeviceTokenRequest {
    @SerializedName("user_id")
    private String userId;

    @SerializedName("firebase_installation_id")
    private String firebaseInstallationId;

    @SerializedName("fcm_token")
    private String fcmToken;

    @SerializedName("email")
    private String email;

    public DeviceTokenRequest(String userId, String firebaseInstallationId, String fcmToken, String email) {
        this.userId = userId;
        this.firebaseInstallationId = firebaseInstallationId;
        this.fcmToken = fcmToken;
        this.email = email;
    }

    public String getUserId() { return userId; }
    public String getFirebaseInstallationId() { return firebaseInstallationId; }
    public String getFcmToken() { return fcmToken; }
    public String getEmail() { return email; }
}
