package com.example.owlagenda.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.owlagenda.R;
import com.example.owlagenda.util.services.ContadorService;
import com.google.firebase.auth.FirebaseAuth;

public class EsqueciSenhaView extends AppCompatActivity {
    private EditText EtEmail;
    private TextView mensagem;
    private TextView textoContagem;
    private boolean contagemEmAndamento = false;
    private FirebaseAuth mAuth;
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_esqueci_senha_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EtEmail = findViewById(R.id.et_email_login);
        mensagem = findViewById(R.id.tv_mensagem);
        textoContagem = findViewById(R.id.contador_esqueci_senha);

        mAuth = FirebaseAuth.getInstance();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("atualizacao_contador"));
        intent = new Intent(this, ContadorService.class);
    }

    public void enviarEmailRedefinicao(View v) {
        String email = EtEmail.getText().toString();
        if (!email.isEmpty()) {
            if (!contagemEmAndamento) {
                // Se a contagem não estiver em andamento, inicia o serviço de contagem
                ContextCompat.startForegroundService(this, intent);
                // Exibe o texto de contagem
                textoContagem.setVisibility(View.VISIBLE);
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        mensagem.setVisibility(View.VISIBLE);
                        mensagem.setText("Caso o email corresponder a uma das contas no Owl, será enviado um e-mail com instruções para redefinir a senha!");
                    } else {
                        mensagem.setVisibility(View.VISIBLE);
                        mensagem.setError("Erro ao enviar e-mail, tente novamente");
                    }
                });

            } else {
                // Se a contagem estiver em andamento, informa o usuário para aguardar
                Toast.makeText(EsqueciSenhaView.this, "Aguarde o término da contagem para enviar outro email de redefinição de senha.", Toast.LENGTH_SHORT).show();
            }
        } else {
            mensagem.setVisibility(View.VISIBLE);
            mensagem.setError("Preencha o campo de e-mail");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove o registro do receptor de transmissão local ao destruir a atividade
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
}