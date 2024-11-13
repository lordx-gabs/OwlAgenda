package com.example.owlagenda.util;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        SharedPreferencesUtil.init(getApplicationContext());

        AppCompatDelegate.setDefaultNightMode(
                SharedPreferencesUtil.getInt(SharedPreferencesUtil.KEY_USER_THEME,
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));

        NotificationUtil.createNotificationChannel(getApplicationContext());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Verifica se o modo noturno mudou
        if ((newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            // Modo escuro ativado
            if (SharedPreferencesUtil.getInt(SharedPreferencesUtil.KEY_USER_THEME,
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                Log.d("teste", "" + SharedPreferencesUtil.getInt(SharedPreferencesUtil.KEY_USER_THEME,
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        } else if ((newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) {
            // Modo claro ativado
            if (SharedPreferencesUtil.getInt(SharedPreferencesUtil.KEY_USER_THEME,
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                Log.d("teste", "" + SharedPreferencesUtil.getInt(SharedPreferencesUtil.KEY_USER_THEME,
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }
    }
}
