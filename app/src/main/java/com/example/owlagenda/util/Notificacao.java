package com.example.owlagenda.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class Notificacao {
    public static final String CHANNEL_ID = "seu_channel_id";
    public static final String CHANNEL_NAME = "Seu Canal de Notificação";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Descrição do seu canal de notificação");
            notificationManager.createNotificationChannel(channel);
        }
    }

}
