package com.mosleemapp.app.utils;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.mosleemapp.app.R;
import com.mosleemapp.app.utils.app.SettingsManager;

import android.view.View;

public class AdMobUtil {

    private static InterstitialAd mInterstitialAd;
    private static RewardedAd mRewardedAd;
    private static AppOpenAd mAppOpenAd;
    private static boolean isPremiumUser = false;
    private static boolean isShowingAppOpenAd = false;

    public static void initialize(Context context) {
        isPremiumUser = SettingsManager.getInstance(context).isPremium();
        if (isPremiumUser) return;

        // Register test devices to suppress the native ad validator dialog.
        // To add a physical device: run the app, check Logcat for:
        // "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("XXXXXX"))"
        // then add that hash to the list below.
        RequestConfiguration config = new RequestConfiguration.Builder()
                .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED)
                .setTestDeviceIds(java.util.Arrays.asList(
                        AdRequest.DEVICE_ID_EMULATOR,  // Emulator
                        "fffb0c3c9eb6620d4", // Physical device
                        "F202CD6826DBC7A6787AAC0C4C479683"
                    ))
                .build();
        MobileAds.setRequestConfiguration(config);

        MobileAds.initialize(context, initializationStatus -> {});
    }
    
    public static void setPremium(boolean isPremium) {
        isPremiumUser = isPremium;
    }

    public static void loadBanner(AdView adView) {
        if (isPremiumUser) {
            if (adView != null) {
                adView.setVisibility(View.GONE);
            }
            return;
        }
        
        if (adView != null) {
            adView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }

    public static void loadInterstitial(Context context) {
        if (isPremiumUser) return;
        
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(context, context.getString(R.string.admob_interstitial_id),
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                mInterstitialAd = null;
                                loadInterstitial(context);
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                mInterstitialAd = null;
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });
    }

    public static void showInterstitial(Activity activity) {
        if (isPremiumUser) return;
        
        if (mInterstitialAd != null) {
            mInterstitialAd.show(activity);
        } else {
            loadInterstitial(activity);
        }
    }

    public static void loadRewarded(Context context) {
        if (isPremiumUser) return;
        
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(context, context.getString(R.string.admob_rewarded_id),
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                mRewardedAd = null;
                                loadRewarded(context);
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                mRewardedAd = null;
                            }
                        });
                    }
                });
    }

    public static void showRewarded(Activity activity, OnUserEarnedRewardListener listener) {
        if (isPremiumUser) return;

        if (mRewardedAd != null) {
            mRewardedAd.show(activity, listener);
        } else {
            loadRewarded(activity);
        }
    }

    public static void loadNativeAd(Context context, com.google.android.gms.ads.nativead.NativeAd.OnNativeAdLoadedListener listener) {
        if (isPremiumUser) return;

        com.google.android.gms.ads.AdLoader adLoader = new com.google.android.gms.ads.AdLoader.Builder(context,
                context.getString(R.string.admob_native_id))
                .forNativeAd(listener)
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        // Handle failure silently
                    }
                })
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    public static void loadAppOpenAd(Context context) {
        if (isPremiumUser) return;
        
        AdRequest adRequest = new AdRequest.Builder().build();
        AppOpenAd.load(context, context.getString(R.string.admob_app_open_id), adRequest,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                        mAppOpenAd = appOpenAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mAppOpenAd = null;
                    }
                });
    }

    public static void showAppOpenAd(Activity activity) {
        if (isPremiumUser) return;
        
        if (isShowingAppOpenAd) {
            return;
        }

        if (mAppOpenAd != null) {
            mAppOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    mAppOpenAd = null;
                    isShowingAppOpenAd = false;
                    loadAppOpenAd(activity);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    mAppOpenAd = null;
                    isShowingAppOpenAd = false;
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    isShowingAppOpenAd = true;
                }
            });
            mAppOpenAd.show(activity);
        } else {
            loadAppOpenAd(activity);
        }
    }
}
