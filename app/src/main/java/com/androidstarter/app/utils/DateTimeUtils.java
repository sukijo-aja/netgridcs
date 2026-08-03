package com.androidstarter.app.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for common date/time formatting operations.
 */
public class DateTimeUtils {

    private static final String FORMAT_FULL = "dd MMMM yyyy, HH:mm";
    private static final String FORMAT_DATE = "dd MMM yyyy";
    private static final String FORMAT_TIME = "HH:mm";
    private static final String FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String FORMAT_API = "yyyy-MM-dd HH:mm:ss";

    /** Format a Date to full display string (e.g., "03 August 2026, 16:35") */
    public static String formatFull(Date date) {
        return new SimpleDateFormat(FORMAT_FULL, Locale.getDefault()).format(date);
    }

    /** Format a Date to short date (e.g., "03 Aug 2026") */
    public static String formatDate(Date date) {
        return new SimpleDateFormat(FORMAT_DATE, Locale.getDefault()).format(date);
    }

    /** Format a Date to time only (e.g., "16:35") */
    public static String formatTime(Date date) {
        return new SimpleDateFormat(FORMAT_TIME, Locale.getDefault()).format(date);
    }

    /** Format a timestamp to API format (e.g., "2026-08-03 16:35:00") */
    public static String formatForApi(Date date) {
        return new SimpleDateFormat(FORMAT_API, Locale.getDefault()).format(date);
    }

    /** Parse ISO 8601 string to Date */
    public static Date parseIso(String isoString) {
        try {
            return new SimpleDateFormat(FORMAT_ISO, Locale.getDefault()).parse(isoString);
        } catch (ParseException e) {
            return null;
        }
    }

    /** Parse API format string to Date */
    public static Date parseApi(String apiString) {
        try {
            return new SimpleDateFormat(FORMAT_API, Locale.getDefault()).parse(apiString);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Get relative time string (e.g., "2 hours ago", "Yesterday", "3 days ago").
     */
    public static String getRelativeTime(Date date) {
        if (date == null) return "";

        long diffMs = System.currentTimeMillis() - date.getTime();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diffMs);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs);
        long hours = TimeUnit.MILLISECONDS.toHours(diffMs);
        long days = TimeUnit.MILLISECONDS.toDays(diffMs);

        if (seconds < 60) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (hours < 24) return hours + " hours ago";
        if (days == 1) return "Yesterday";
        if (days < 7) return days + " days ago";
        if (days < 30) return (days / 7) + " weeks ago";
        if (days < 365) return (days / 30) + " months ago";
        return (days / 365) + " years ago";
    }

    /** Get relative time from a timestamp in milliseconds. */
    public static String getRelativeTime(long timestampMs) {
        return getRelativeTime(new Date(timestampMs));
    }

    /** Check if a date is today. */
    public static boolean isToday(Date date) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /** Check if a date is yesterday. */
    public static boolean isYesterday(Date date) {
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_YEAR, -1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /** Format with a custom pattern. */
    public static String format(Date date, String pattern) {
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
    }
}
