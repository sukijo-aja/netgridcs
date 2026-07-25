package com.mosleemapp.app.ui.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.mosleemapp.app.R;
import com.mosleemapp.app.utils.LocationManagerHelper;

public class QiblaActivity extends BaseActivity implements SensorEventListener, LocationManagerHelper.LocationListener {

    private ImageView ivCompassDial;
    private android.view.View ivQiblaNeedle;
    private ImageView ivKaabaIcon;
    private TextView tvBearingInfo;
    private TextView tvDistanceInfo;
    private TextView tvNotFlatWarning;
    private TextView tvCityName;
    private androidx.cardview.widget.CardView cvCalibrationOverlay;

    private SensorManager sensorManager;
    private com.mosleemapp.app.ui.viewmodel.PrayerViewModel prayerViewModel;
    private com.mosleemapp.app.utils.AppPreference appPreference;
    private Sensor rotationVectorSensor;
    private LocationManagerHelper locationManagerHelper;

    private float currentCompassDegree = 0f;
    private float currentNeedleDegree = 0f;
    private float qiblaBearing = 0f; // from current location to Kaaba

    private final double KAABA_LATITUDE = 21.422487;
    private final double KAABA_LONGITUDE = 39.826206;
    
    // Fallback coords (Jakarta) if location isn't available immediately
    private double currentLat = -6.2088;
    private double currentLng = 106.8456;
    private boolean isLocationSet = false;
    
    private boolean isFirstSensorUpdate = true;
    private final float ALPHA = 0.15f; // low-pass filter for smooth needle movement

    // Reusable arrays for sensor calculations to avoid GC churn
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qibla);

        ivCompassDial = findViewById(R.id.iv_compass_dial);
        ivQiblaNeedle = findViewById(R.id.iv_qibla_needle);
        ivKaabaIcon = findViewById(R.id.iv_kaaba_icon);
        tvBearingInfo = findViewById(R.id.tv_bearingInfo);
        tvDistanceInfo = findViewById(R.id.tv_distanceInfo);
        tvNotFlatWarning = findViewById(R.id.tv_not_flat_warning);
        tvCityName = findViewById(R.id.tv_city_name);
        cvCalibrationOverlay = findViewById(R.id.cv_calibration_overlay);
        
        appPreference = new com.mosleemapp.app.utils.AppPreference(this);
        String savedCity = appPreference.getString("city", "");
        if (savedCity != null && !savedCity.isEmpty()) {
            tvCityName.setText(savedCity);
        }

        prayerViewModel = new androidx.lifecycle.ViewModelProvider(this).get(com.mosleemapp.app.ui.viewmodel.PrayerViewModel.class);
        
        com.google.android.gms.ads.AdView adView = findViewById(R.id.adView);
        com.mosleemapp.app.utils.AdMobUtil.loadBanner(adView);
        prayerViewModel.getCityName().observe(this, name -> {
            if (name != null && !name.isEmpty()) {
                tvCityName.setText(name);
            }
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            if (rotationVectorSensor == null) {
                // Fallback to orientation if rotation vector is unavailable (deprecated but fallback)
                rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            }
        }

        if (rotationVectorSensor == null) {
            Toast.makeText(this, "Device doesn't have required sensors for compass", Toast.LENGTH_LONG).show();
            tvBearingInfo.setText("Compass not supported");
        }

        locationManagerHelper = new LocationManagerHelper(this, this);
        locationManagerHelper.getLocation(); // Request location update

        calculateQibla(); // initial calculation with default coords
    }

    @Override
    protected void onResume() {
        super.onResume();
         if (sensorManager != null && rotationVectorSensor != null) {
             sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI);
         }
    }

    @Override
    protected void onPause() {
        super.onPause();
         if (sensorManager != null) {
             sensorManager.unregisterListener(this);
         }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float azimuthInDegrees = 0f;
        float pitchInDegrees = 0f;
        float rollInDegrees = 0f;

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.getOrientation(rotationMatrix, orientationAngles);
            
            // Convert azimuth to degrees
            azimuthInDegrees = (float) Math.toDegrees(orientationAngles[0]);
            if (azimuthInDegrees < 0) {
                azimuthInDegrees += 360;
            }
            pitchInDegrees = (float) Math.toDegrees(orientationAngles[1]);
            rollInDegrees = (float) Math.toDegrees(orientationAngles[2]);
        } else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            azimuthInDegrees = event.values[0];
            pitchInDegrees = event.values[1];
            rollInDegrees = event.values[2];
        }

        int visibility = (Math.abs(pitchInDegrees) > 20 || Math.abs(rollInDegrees) > 20) ? android.view.View.VISIBLE : android.view.View.INVISIBLE;
        if (tvNotFlatWarning.getVisibility() != visibility) {
            tvNotFlatWarning.setVisibility(visibility);
        }

        // The compass dial (North) should point 'up', so we rotate it by -azimuth
        float newCompassDegree = -azimuthInDegrees;
        
        // The Qibla needle should point to Qibla relative to North
        // It rotates with the dial, plus the Qibla bearing
        float newNeedleDegree = newCompassDegree + qiblaBearing;

        if (isFirstSensorUpdate) {
            currentCompassDegree = newCompassDegree;
            currentNeedleDegree = newNeedleDegree;
            isFirstSensorUpdate = false;
        } else {
            // Adjust angles to handle 360 degree wrap-around (so it doesn't spin backwards)
            newCompassDegree = adjustAngle(currentCompassDegree, newCompassDegree);
            newNeedleDegree = adjustAngle(currentNeedleDegree, newNeedleDegree);

            // Apply low-pass filter to smooth the jitter
            currentCompassDegree = currentCompassDegree + ALPHA * (newCompassDegree - currentCompassDegree);
            currentNeedleDegree = currentNeedleDegree + ALPHA * (newNeedleDegree - currentNeedleDegree);
        }

        // Apply rotation directly to the view instead of creating Animation objects every frame
        ivCompassDial.setRotation(currentCompassDegree);
        ivQiblaNeedle.setRotation(currentCompassDegree); // Point to North/South instead of Qibla

        // Position Kaaba icon on the edge of the compass at the Qibla direction
        float radius = ivCompassDial.getWidth() / 2f - 30; // slightly inside the compass edge
        double angleRad = Math.toRadians(currentNeedleDegree - 90); // -90 because 0° is up
        ivKaabaIcon.setTranslationX((float) (radius * Math.cos(angleRad)));
        ivKaabaIcon.setTranslationY((float) (radius * Math.sin(angleRad)));

        int displayAzimuth = (int) azimuthInDegrees;
        if(isLocationSet) {
            tvBearingInfo.setText(displayAzimuth + "° " + getDirectionString(displayAzimuth));
        } else {
            tvBearingInfo.setText("Waiting for location... (" + displayAzimuth + "°)");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR || sensor.getType() == Sensor.TYPE_ORIENTATION) {
            int visibility = (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE || accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW) 
                    ? android.view.View.VISIBLE 
                    : android.view.View.GONE;
            if (cvCalibrationOverlay != null && cvCalibrationOverlay.getVisibility() != visibility) {
                cvCalibrationOverlay.setVisibility(visibility);
            }
        }
    }

    @Override
    public void onLocationReceived(double latitude, double longitude) {
        currentLat = latitude;
        currentLng = longitude;
        isLocationSet = true;
        calculateQibla();
        
        String city = appPreference.getString("city", "");
        if (city == null || city.isEmpty()) {
            prayerViewModel.updateLocation(latitude, longitude);
        }
    }

    private void calculateQibla() {
        Location currentLocation = new Location("");
        currentLocation.setLatitude(currentLat);
        currentLocation.setLongitude(currentLng);

        Location kaabaLocation = new Location("");
        kaabaLocation.setLatitude(KAABA_LATITUDE);
        kaabaLocation.setLongitude(KAABA_LONGITUDE);

        // Calculate bearing
        qiblaBearing = currentLocation.bearingTo(kaabaLocation);
        if (qiblaBearing < 0) {
            qiblaBearing += 360;
        }

        // Calculate distance
        float distanceInMeters = currentLocation.distanceTo(kaabaLocation);
        float distanceInKm = distanceInMeters / 1000f;

        tvDistanceInfo.setText(String.format("Distance to Kaaba: %.0f km", distanceInKm));
    }

    private float adjustAngle(float current, float target) {
        float difference = target - current;
        if (difference > 180) {
            target -= 360;
        } else if (difference < -180) {
            target += 360;
        }
        return target;
    }

    private String getDirectionString(int azimuth) {
        if (azimuth >= 338 || azimuth < 23) return "N";
        if (azimuth >= 23 && azimuth < 68) return "NE";
        if (azimuth >= 68 && azimuth < 113) return "E";
        if (azimuth >= 113 && azimuth < 158) return "SE";
        if (azimuth >= 158 && azimuth < 203) return "S";
        if (azimuth >= 203 && azimuth < 248) return "SW";
        if (azimuth >= 248 && azimuth < 293) return "W";
        if (azimuth >= 293 && azimuth < 338) return "NW";
        return "";
    }
}
