package com.example.owlagenda.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.owlagenda.R;

public class NotificationUtil extends BroadcastReceiver {
    public static final String CHANNEL_ID = "canal_notificacao_owl";
    public static final String CHANNEL_NAME = "Notificações do Owl";
    public static final int idNotificationAlertTask = 1;

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
                .setSmallIcon(R.drawable.ic_selene)
                .setPriority(priority)
                .setVibrate(new long[]{0, 500, 1000, 500});

        return builder.build();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");

        Notification notification = createNotification(context, title, description, NotificationCompat.PRIORITY_HIGH);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(idNotificationAlertTask, notification);
    }

    public static class scheduleNotificationApp {
        public static void scheduleNotification(Context context, long triggerAtMillis, String title, String description) {
            Intent intent = new Intent(context, NotificationUtil.class);
            intent.putExtra("title", title);
            intent.putExtra("description", description);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                    }
                } else {
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                }
            }
        }
    }
}
