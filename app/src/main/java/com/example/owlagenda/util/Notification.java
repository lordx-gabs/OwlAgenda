package com.example.owlagenda.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import com.example.owlagenda.R;

public class Notification {
    public static final String CHANNEL_ID = "canal_notificacao_owl";
    public static final String CHANNEL_NAME = "Notificações do Owl";

    public static void getNotificationChannel(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Descrição do canal de notificação");
        notificationManager.createNotificationChannel(channel);
    }

    public static android.app.Notification createNotification(Context context, String titulo, String descricao, int prioridade) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Notification.CHANNEL_ID)
                .setContentTitle(titulo)
                .setContentText(descricao)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(prioridade);

        return builder.build();
    }

}
