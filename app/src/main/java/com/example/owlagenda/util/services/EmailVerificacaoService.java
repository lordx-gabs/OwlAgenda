package com.example.owlagenda.util.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.owlagenda.R;
import com.example.owlagenda.util.Notificacao;
import com.google.api.client.googleapis.notifications.NotificationUtils;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class EmailVerificacaoService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String TAG = "EmailVerificacaoService";
    private static final long DURACAO_CONTAGEM = 5 * 60 * 1000; // 5 minutos em milissegundos
    private static final long INTERVALO_ATUALIZACAO = 1000; // Intervalo de atualização em milissegundos
    private long tempoRestanteMillis;
    private Handler handler;
    private Runnable runnable;
    private static boolean contagemAtiva = false;

    @Override
    public void onCreate() {
        super.onCreate();
        this.tempoRestanteMillis = DURACAO_CONTAGEM;
        this.handler = new Handler();
        this.runnable = new Runnable() {
            @Override
            public void run() {
                if (tempoRestanteMillis > 0) {
                    atualizaContador();
                    // Contagem regressiva ainda rolando
                    handler.postDelayed(this, INTERVALO_ATUALIZACAO);
                    tempoRestanteMillis -= INTERVALO_ATUALIZACAO;
                } else {
                    // Contagem regressiva concluída
                    handler.removeCallbacks(runnable);
                    onFinish();
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
        Notification notification = buildNotification();
        startForeground(NOTIFICATION_ID, notification);
        comecarContagem();
        return START_STICKY; // O serviço será reiniciado se for encerrado pelo sistema
    }

    public void atualizaContador() {
        long minutos = TimeUnit.MILLISECONDS.toMinutes(tempoRestanteMillis);
        long segundos = TimeUnit.MILLISECONDS.toSeconds(tempoRestanteMillis) -
                TimeUnit.MINUTES.toSeconds(minutos);
        String tempoFormatado = String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);
        enviarAtualizacaoContador("Tempo restante: " + tempoFormatado);
    }

    public void comecarContagem() {
        // Inicia a contagem.
        contagemAtiva = true;
        handler.postDelayed(runnable, INTERVALO_ATUALIZACAO);
    }

    private void onFinish() {
        // Lógica para quando a contagem regressiva terminar
        contagemAtiva = false;
        enviarAtualizacaoContador(null);

        stopSelf(); // Encerra o serviço após a contagem regressiva
    }

    private void enviarAtualizacaoContador(String contador) {
        Intent intent = new Intent("atualizacao_contador");
        intent.putExtra("contador_atual", contador);
        intent.putExtra("estado_contagem", contagemAtiva);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private Notification buildNotification() {
        // Construa sua notificação aqui
        // Exemplo: criar uma notificação simples
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Notificacao.CHANNEL_ID)
                .setContentTitle("Seu Serviço")
                .setContentText("Está em execução...")
                .setSmallIcon(R.drawable.ic_launcher_background);

        return builder.build();
    }

}

