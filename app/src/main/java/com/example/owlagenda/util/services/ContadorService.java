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

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ContadorService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final long DURACAO_CONTAGEM = 5 * 60 * 1000; // 5 minutos em milissegundos
    private static final long INTERVALO_ATUALIZACAO = 1000; // Intervalo de atualização em milissegundos
    private long tempoRestanteMillis;
    private Handler handler;
    private Runnable runnable;
    private static boolean contagemAtiva = false;

    @Override
    public void onCreate() {
        super.onCreate();
        // Inicialização do serviço
        this.tempoRestanteMillis = DURACAO_CONTAGEM; // Define o tempo total da contagem regressiva
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
        return null; // Não precisa de binder, pois não será utilizado
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = Notificacao.criarNotificacao(this, "Owl Agenda",
                "Está em execução...", NotificationCompat.PRIORITY_MIN); // Cria a notificação de serviço
        startForeground(NOTIFICATION_ID, notification); // Inicia o serviço em primeiro plano com a notificação
        comecarContagem(); // Inicia a contagem regressiva
        return START_STICKY; // O serviço será reiniciado se for encerrado pelo sistema
    }

    public void atualizaContador() {
        // Calcula os minutos e segundos restantes
        long minutos = TimeUnit.MILLISECONDS.toMinutes(tempoRestanteMillis);
        long segundos = TimeUnit.MILLISECONDS.toSeconds(tempoRestanteMillis) -
                TimeUnit.MINUTES.toSeconds(minutos);
        // Formata o tempo restante em minutos e segundos
        String tempoFormatado = String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);
        enviarAtualizacaoContador("Tempo restante: " + tempoFormatado); // Envia a atualização do contador
    }

    public void comecarContagem() {
        // Inicia a contagem.
        contagemAtiva = true; // Define a contagem como ativa
        handler.postDelayed(runnable, INTERVALO_ATUALIZACAO); // Inicia o Runnable para atualizar o contador
    }

    private void onFinish() {
        // Lógica para quando a contagem regressiva terminar
        contagemAtiva = false; // Define a contagem como inativa
        enviarAtualizacaoContador(null); // Envia uma notificação de que a contagem terminou

        stopSelf(); // Encerra o serviço após a contagem regressiva
    }

    private void enviarAtualizacaoContador(String contador) {
        // Envia uma mensagem de broadcast com o contador atual e o estado da contagem
        Intent intent = new Intent("atualizacao_contador");
        intent.putExtra("contador_atual", contador);
        intent.putExtra("estado_contagem", contagemAtiva);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}
