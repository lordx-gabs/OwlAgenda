package com.example.owlagenda.data.database;

import android.app.Application;

public class IniciarOuFecharDB extends Application {
    public static AppDatabase appDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        appDatabase = AppDatabase.getDatabase(this);
    }

    public void fecharDB() {
        if (appDatabase != null) {
            appDatabase.close();
        }
    }

}
