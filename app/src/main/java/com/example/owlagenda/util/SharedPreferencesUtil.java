package com.example.owlagenda.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class SharedPreferencesUtil {
    private static final String PREF_NAME = "UserPrefs";
    public static final String KEY_USER_REMEMBER_ME = "userRememberMe";
    public static final String KEY_USER_TIMESTAMP = "timeCredentials";
    public static final String KEY_USER_THEME = "userTheme";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    public static void init(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
    }

    public static void saveBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public static void removeBoolean(String key) {
        editor.remove(key);
        editor.apply();
    }

    public static void saveLong(String key, long value) {
        editor.putLong(key, value);
        editor.apply();
    }

    public static long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    public static void saveInt(String key, int theme) {
        editor.putInt(key, theme);
        editor.apply();
    }

    public static int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public static void clearAll() {
        editor.clear();
        editor.apply();
    }

}
