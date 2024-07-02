package com.example.owlagenda.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import com.example.owlagenda.R;

public class NotificationUtil {
    public static final String CHANNEL_ID = "canal_notificacao_owl";
    public static final String CHANNEL_NAME = "Notificações do Owl";

    public static void createNotificationChannel(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Descrição do canal de notificação");
        notificationManager.createNotificationChannel(channel);
    }

    public static Notification createNotification(Context context, String title, String description, int priority) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationUtil.CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(description)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(priority);

        return builder.build();
    }

}
