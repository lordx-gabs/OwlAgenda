package com.example.owlagenda.util;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.owlagenda.R;
import com.example.owlagenda.ui.calendar.CalendarFragment;
import com.example.owlagenda.ui.telaprincipal.TelaPrincipalView;

public class NotificationUtil extends BroadcastReceiver {
    public static final String CHANNEL_ID = "canal_notificacao_owl";
    public static final String CHANNEL_NAME = "Notificações do Owl";
    public static final int idNotificationAlertTask = 1;

    public static void createNotificationChannel(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Notificações de suas tarefas do Owl.");
        notificationManager.createNotificationChannel(channel);
    }

    public static Notification createNotification(Context context, String title, int requestCode) {
        Intent completeTaskIntent = new Intent(context, CompleteTaskReceiver.class);
        completeTaskIntent.putExtra("taskId", title);
        completeTaskIntent.putExtra("requestCode", requestCode);
        PendingIntent completeTaskPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                completeTaskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT
        );

        Intent intent = new Intent(context.getApplicationContext(), TelaPrincipalView.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), NotificationUtil.CHANNEL_ID)
                .setContentTitle(title)
                .setContentText("Sua tarefa está próxima. Clique para mais detalhes")
                .setSmallIcon(R.drawable.ic_selene)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setColor(context.getColor(R.color.selector_notification))
                .addAction(R.drawable.ic_check, "Concluir Tarefa", completeTaskPendingIntent)
                .setVibrate(new long[]{0, 500, 1000, 500});

        return builder.build();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        int requestCode = intent.getIntExtra("requestCode", 0);

        Notification notification = createNotification(context.getApplicationContext(), title, requestCode);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(requestCode, notification);
    }

    public static class scheduleNotificationApp {

        public static void scheduleNotification(Context context, long triggerAtMillis, String title, int requestCode) {
            Intent intent = new Intent(context, NotificationUtil.class);
            intent.putExtra("title", title);
            intent.putExtra("requestCode", requestCode);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                        Log.d("agendou", "horas " + triggerAtMillis);
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                    }
                } else {
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                }
            }
        }

        public static void cancelNotification(Context context, String title, int requestCode) {
            Intent intent = new Intent(context, NotificationUtil.class);
            intent.putExtra("title", title);
            intent.putExtra("requestCode", requestCode);

            // Recria o mesmo PendingIntent que foi usado para agendar o alarme
            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            } else {
                pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }

            // Obtém o AlarmManager
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                // Cancela o alarme
                alarmManager.cancel(pendingIntent);
                Log.d("AlarmManager", "Alarme cancelado");
            }
        }

        @SuppressLint("UnspecifiedImmutableFlag")
        public static boolean isAlarmSet(Context context, String title, int requestCode) {
            // Crie um Intent para o seu BroadcastReceiver
            Intent intent = new Intent(context, NotificationUtil.class);
            intent.putExtra("title", title);
            intent.putExtra("requestCode", requestCode);
            PendingIntent pendingIntent;
            // Crie o PendingIntent com o mesmo requestCode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE // Use FLAG_NO_CREATE para verificar se já existe
                );
            } else {
                pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_NO_CREATE);
            }

            // Se o PendingIntent for null, o alarme não está agendado
            return pendingIntent != null;
        }
    }
}
