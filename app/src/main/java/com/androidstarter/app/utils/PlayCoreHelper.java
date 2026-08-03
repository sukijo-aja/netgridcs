package com.androidstarter.app.utils;

import android.app.Activity;
import android.util.Log;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.gms.tasks.Task;

public class PlayCoreHelper {

    private static final int APP_UPDATE_REQUEST_CODE = 1001;
    private static final String TAG = "PlayCoreHelper";

    /**
     * Checks for app updates (Flexible by default, but can be configured for Immediate).
     */
    public static void checkForUpdate(Activity activity) {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(activity);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.FLEXIBLE,
                            activity,
                            APP_UPDATE_REQUEST_CODE
                    );
                } catch (Exception e) {
                    Log.e(TAG, "Error starting update flow", e);
                }
            }
        });
    }

    /**
     * Shows an in-app review dialog.
     */
    public static void requestInAppReview(Activity activity) {
        ReviewManager manager = ReviewManagerFactory.create(activity);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ReviewInfo reviewInfo = task.getResult();
                Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
                flow.addOnCompleteListener(flowTask -> {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown.
                    Log.d(TAG, "Review flow finished");
                });
            } else {
                Log.e(TAG, "Error requesting review flow", task.getException());
            }
        });
    }
}
