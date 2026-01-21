package com.mosleemapp.app.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationManagerHelper {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private final LocationListener listener;

    public interface LocationListener {
        void onLocationReceived(double latitude, double longitude);
    }

    public LocationManagerHelper(Context context, LocationListener listener) {
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.listener = listener;
        createLocationCallback();
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        listener.onLocationReceived(location.getLatitude(), location.getLongitude());
                        // We only need one update for now
                        fusedLocationClient.removeLocationUpdates(locationCallback);
                        break;
                    }
                }
            }
        };
    }

    @SuppressLint("MissingPermission")
    public void getLocation() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(10000)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
}
