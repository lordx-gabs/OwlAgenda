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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.owlagenda.R;
import com.example.owlagenda.ui.viewmodels.EmailVerificacaoViewModel;
import com.example.owlagenda.util.services.EmailVerificacaoService;
import com.google.firebase.auth.FirebaseAuth;

public class EnviaEmailVerificacao extends AppCompatActivity {
    private EmailVerificacaoViewModel emailVerificacaoViewModel;
    private TextView textoContagem;
    private boolean contagemEmAndamento = false;
    private EmailVerificacaoService emailVerificacaoService;
    private Intent intent;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String contadorAtual = intent.getStringExtra("contador_atual");
            textoContagem.setText(contadorAtual);
            contagemEmAndamento = intent.getBooleanExtra("estado_contagem", false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_envia_email_verificacao);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailVerificacaoViewModel = new ViewModelProvider(this).get(EmailVerificacaoViewModel.class);

        emailVerificacaoService = new EmailVerificacaoService();

        textoContagem = findViewById(R.id.tv_contagem);

        intent = new Intent(this, EmailVerificacaoService.class);

        // Registrar o BroadcastReceiver para receber atualizações do contador
        IntentFilter intentFilter = new IntentFilter("atualizacao_contador");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    public void cliqueBtn(View v) {
        Toast.makeText(this, "boo" + contagemEmAndamento, Toast.LENGTH_SHORT).show();

        if (!contagemEmAndamento) {
            textoContagem.setVisibility(View.VISIBLE);
            ContextCompat.startForegroundService(this, intent);

            // Inicia o envio do email de verificação
            emailVerificacaoViewModel.enviarEmailVerificacao().observe(EnviaEmailVerificacao.this, aBoolean -> {
                if (aBoolean) {
                    Toast.makeText(EnviaEmailVerificacao.this, "Email enviado com sucesso.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EnviaEmailVerificacao.this, "Não foi possível enviar o email de verificação. Por favor, tente novamente.", Toast.LENGTH_SHORT).show();
                }
            });

            // Define que a contagem está em andamento, impedindo o usuário de enviar outro email
            contagemEmAndamento = true;
        } else {
            // Contagem em andamento, informe ao usuário que ele deve esperar
            Toast.makeText(EnviaEmailVerificacao.this, "Aguarde o termino da contagem, para enviar outro email de verificação.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remover o registro do BroadcastReceiver ao destruir a atividade
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
}