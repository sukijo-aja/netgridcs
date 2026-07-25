package com.mosleemapp.app.utils;

import android.annotation.TargetApi;
import android.content.Context;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import com.mosleemapp.app.utils.app.SettingsManager;

import java.util.Locale;

public class LocaleHelper {

    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";

    public static Context onAttach(Context context) {
        String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        return setLocale(context, lang, false); // Don't persist on attach
    }

    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, lang, false); // Don't persist on attach
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    public static Context setLocale(Context context, String language) {
        return setLocale(context, language, true); // Persist when explicitly set
    }

    public static Context setLocale(Context context, String language, boolean persist) {
        if (persist) {
            persist(context, language);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }

        return updateResourcesLegacy(context, language);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        com.mosleemapp.app.utils.AppPreference preferences = SettingsManager.getInstance(context).getAppPreference();
        // Return null if not found, to detect if user has set a preference
        if (!preferences.checkKey(SELECTED_LANGUAGE)) {
            return defaultLanguage;
        }
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    private static void persist(Context context, String language) {
        com.mosleemapp.app.utils.AppPreference preferences = SettingsManager.getInstance(context).getAppPreference();
        preferences.saveString(SELECTED_LANGUAGE, language);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);

        configuration.setLayoutDirection(Locale.ENGLISH);

        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // Force LTR even for RTL languages
            configuration.setLayoutDirection(Locale.ENGLISH);
        }
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return context;
    }
}
