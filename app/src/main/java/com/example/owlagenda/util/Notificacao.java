package com.example.owlagenda.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import com.example.owlagenda.R;

public class Notificacao {
    public static final String CHANNEL_ID = "canal_notificacao_owl";
    public static final String CHANNEL_NAME = "Notificações do Owl";

    public static void criarCanalDeNotificacao(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Descrição do canal de notificação");
        notificationManager.createNotificationChannel(channel);
    }

    public static Notification criarNotificacao(Context context, String titulo, String descricao, int prioridade) {
        // Constrói a notificação do serviço
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Notificacao.CHANNEL_ID)
                .setContentTitle(titulo)
                .setContentText(descricao)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(prioridade); // Define a prioridade mínima

        return builder.build(); // Retorna a notificação construída
    }

}
