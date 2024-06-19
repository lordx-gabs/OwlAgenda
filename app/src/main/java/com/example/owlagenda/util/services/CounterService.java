package com.example.owlagenda.util.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.owlagenda.util.Notificacao;
import com.google.firebase.database.ServerValue;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CounterService extends Service {
    public static final int NOTIFICATION_ID_COUNTER = 65;
    private static final long DURACAO_CONTAGEM = 90000; // 1 minuto e 30 segundos em milissegundos
    private static final long INTERVALO_ATUALIZACAO = 1000; // Intervalo de atualização em milissegundos
    private long tempoRestanteMillis;
    private Handler handler;
    private Runnable runnable;
    private static boolean counterActive = false;

    @Override
    public void onCreate() {
        super.onCreate();
        this.tempoRestanteMillis = DURACAO_CONTAGEM;
        this.handler = new Handler(); // Cria um Handler para gerenciar a atualização do contador
        this.runnable = new Runnable() {
            @Override
            public void run() {
                if (tempoRestanteMillis > 0) {
                    atualizaContador(); // Método para atualizar o contador
                    // Contagem regressiva ainda rolando, agenda a próxima execução
                    handler.postDelayed(this, INTERVALO_ATUALIZACAO);
                    tempoRestanteMillis -= INTERVALO_ATUALIZACAO; // Atualiza o tempo restante
                } else {
                    // Contagem regressiva concluída, encerra a execução
                    handler.removeCallbacks(runnable);
                    onFinish(); // Lógica para quando a contagem regressiva terminar
                }
            }
        };
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = Notificacao.criarNotificacao(this, "Owl Agenda",
                "Está em execução...", NotificationCompat.PRIORITY_MIN);
        startForeground(NOTIFICATION_ID_COUNTER, notification);
        comecarContagem();
        return START_STICKY;
    }

    public void atualizaContador() {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(tempoRestanteMillis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(tempoRestanteMillis) -
                TimeUnit.MINUTES.toSeconds(minutes);
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        sendCounterUpdate("Tempo restante: " + timeFormatted);
    }

    public void comecarContagem() {
        // Inicia a contagem.
        counterActive = true; // Define a contagem como ativa
        handler.postDelayed(runnable, INTERVALO_ATUALIZACAO); // Inicia o Runnable para atualizar o contador
    }

    private void onFinish() {
        // Lógica para quando a contagem regressiva terminar
        counterActive = false; // Define a contagem como inativa
        sendCounterUpdate(null); // Envia uma notificação de que a contagem terminou

        stopSelf(); // Encerra o serviço após a contagem regressiva
    }

    private void sendCounterUpdate(String counter) {
        // Envia uma mensagem de broadcast com o counter atual e o estado da contagem
        Intent intent = new Intent("counterUpdate");
        intent.putExtra("counter", counter);
        intent.putExtra("counterStatus", counterActive);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}
