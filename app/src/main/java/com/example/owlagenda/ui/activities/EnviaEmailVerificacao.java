package com.example.owlagenda.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.owlagenda.R;
import com.example.owlagenda.ui.viewmodels.EmailVerificacaoViewModel;
import com.example.owlagenda.util.services.ContadorService;

public class EnviaEmailVerificacao extends AppCompatActivity {
    private EmailVerificacaoViewModel emailVerificacaoViewModel;
    private TextView textoContagem;
    private boolean contagemEmAndamento = false;
    private Intent intent;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Recebe as atualizações do contador
            contagemEmAndamento = intent.getBooleanExtra("estado_contagem", false);
            if (contagemEmAndamento) {
                // Se a contagem estiver em andamento, mostra o texto de contagem
                textoContagem.setVisibility(View.VISIBLE);
            } else {
                // Caso contrário, oculta o texto de contagem
                textoContagem.setVisibility(View.INVISIBLE);
            }
            // Define o texto do contador na TextView
            textoContagem.setText(intent.getStringExtra("contador_atual"));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configuração da atividade
        EdgeToEdge.enable(this); // Configura o EdgeToEdge para a atividade
        setContentView(R.layout.activity_envia_email_verificacao); // Define o layout da atividade

        // Configura o receptor de transmissão local
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("atualizacao_contador"));

        // Inicialização do ViewModel
        emailVerificacaoViewModel = new ViewModelProvider(this).get(EmailVerificacaoViewModel.class);

        // Inicialização dos elementos de UI
        textoContagem = findViewById(R.id.tv_contagem);

        // Criação do Intent para o serviço de contagem
        intent = new Intent(this, ContadorService.class);
    }

    // Método chamado quando o botão é clicado
    public void cliqueBtn(View v) {
        if (!contagemEmAndamento) {
            // Se a contagem não estiver em andamento, inicia o serviço de contagem
            ContextCompat.startForegroundService(this, intent);

            // Exibe o texto de contagem
            textoContagem.setVisibility(View.VISIBLE);

            // Inicia o envio do email de verificação e observa a resposta
            emailVerificacaoViewModel.enviarEmailVerificacao().observe(EnviaEmailVerificacao.this, aBoolean -> {
                if (aBoolean) {
                    // Se o email for enviado com sucesso, exibe um Toast
                    Toast.makeText(EnviaEmailVerificacao.this, "Email enviado com sucesso.", Toast.LENGTH_SHORT).show();
                } else {
                    // Se houver erro no envio do email, exibe um Toast informando o usuário
                    Toast.makeText(EnviaEmailVerificacao.this, "Não foi possível enviar o email de verificação. Por favor, tente novamente.", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            // Se a contagem estiver em andamento, informa o usuário para aguardar
            Toast.makeText(EnviaEmailVerificacao.this, "Aguarde o término da contagem para enviar outro email de verificação.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove o registro do receptor de transmissão local ao destruir a atividade
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
}
