package com.example.owlagenda.util;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.UserViewModel;
import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {
    private UserViewModel viewModel;
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        SharedPreferencesUtil.init(getApplicationContext());
        AppCompatDelegate.setDefaultNightMode(
                SharedPreferencesUtil.getInt(SharedPreferencesUtil.KEY_USER_THEME,
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        );

        NotificationUtil.createNotificationChannel(getApplicationContext());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!isExactAlarmPermissionGranted()) {
                // Direcione o usuário para as configurações do aplicativo
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                Log.e("taaa", "aqui foi");
            } else {
                Log.e("taaa", "aqui foi4444");
            }
        }
    }

    private boolean isExactAlarmPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return getPackageManager().checkPermission(
                    "android.permission.SCHEDULE_EXACT_ALARM", getPackageName()
            ) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Para versões anteriores, não é necessário
    }
}
