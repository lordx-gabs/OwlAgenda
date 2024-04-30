package com.example.owlagenda.ui.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.R;
import com.example.owlagenda.ui.viewmodels.LoginViewModel;
import com.example.owlagenda.util.Notificacao;
import com.example.owlagenda.util.VerificaConexao;
import com.example.owlagenda.util.services.ContadorService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.OAuthProvider;

public class LoginView extends AppCompatActivity {
    private FirebaseAuth mAuth; // Instância de autenticação do Firebase
    private SharedPreferences sharedPreferences; // Para armazenar dados locais
    private LoginViewModel loginViewModel; // ViewModel para gerenciar lógica de login
    private static final String PREF_NAME = "MyPrefs"; // Nome do arquivo de preferências
    private static final String KEY_USER_LOGIN = "user_login"; // Chave para o login do usuário
    private static final String KEY_USER_SENHA = "user_senha"; // Chave para a senha do usuário
    private static final int RC_SIGN_IN = 1399; // Código de solicitação para login com Google
    private GoogleSignInClient mGoogleSignInClient; // Cliente para login com Google
    private Button btnGoogle; // Botão de login com Google
    private Button btnTwitter; // Botão de login com Twitter
    private EditText email, senha; // Campos de entrada para email e senha
    private CheckBox cb_lembrar; // CheckBox para lembrar o usuário
    OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com"); // Provedor de autenticação do Twitter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Habilita layout edge-to-edge
        this.setContentView(R.layout.activity_login_view); // Define o layout da atividade
        Notificacao.criarCanalDeNotificacao(getApplicationContext()); // Cria canal de notificação

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class); // Inicializa ViewModel

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE); // Inicializa SharedPreferences

        mAuth = FirebaseAuth.getInstance(); // Inicializa instância de autenticação do Firebase

        provider.addCustomParameter("lang", "br"); // Adiciona parâmetro customizado ao provedor de autenticação do Twitter

        // Configura opções de login com Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.id_google)) // Solicita ID Token do Google
                .requestEmail() // Solicita email do usuário
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso); // Inicializa cliente de login com Google

        // Verifica se o usuário já está autenticado
        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
            // Usuário está autenticado e email está verificado
            this.proximaTela(); // Vai para a próxima tela
            finish(); // Finaliza esta atividade
        } else {
            try {
                // Tenta recuperar email e senha salvos nas SharedPreferences
                String email = sharedPreferences.getString(KEY_USER_LOGIN, "");
                String senha = sharedPreferences.getString(KEY_USER_SENHA, "");
                if (!email.isEmpty() && !senha.isEmpty()) {
                    // Se email e senha não estão vazios, tenta autenticar o usuário
                    loginViewModel.autenticaUserEmailSenha(email, senha).observe(this, aBoolean -> {
                        if (aBoolean) {
                            // Se a autenticação for bem-sucedida, vai para a próxima tela
                            proximaTela();
                            finish(); // Finaliza esta atividade
                        }
                    });
                }
            } catch (FirebaseAuthException e) {
                Toast.makeText(this, "Erro na autenticação, por favor faça login novamente.", Toast.LENGTH_SHORT).show();
            }
        }

        // Inicializa os componentes da interface
        cb_lembrar = findViewById(R.id.cb_lembraruser);
        email = findViewById(R.id.et_email);
        senha = findViewById(R.id.et_confirma_senha);

        btnTwitter = findViewById(R.id.btn_twitter);
        btnGoogle = findViewById(R.id.btn_google);


        // Define a ação ao clicar no botão de login com Google
        btnGoogle.setOnClickListener(v -> logarComGoogle());

        // Define a ação ao clicar no botão de login com Twitter
        btnTwitter.setOnClickListener(v -> mAuth
                .startActivityForSignInWithProvider(LoginView.this, provider.build())
                .addOnSuccessListener(authResult -> {
                    loginViewModel.autenticaUserTwitter(authResult).observe(LoginView.this, aBoolean -> {
                        if (aBoolean) {
                            Toast.makeText(LoginView.this, "Bem vindo ao Owl", Toast.LENGTH_SHORT).show();
                            proximaTela();
                            finish();
                        } else {
                            Toast.makeText(LoginView.this, "Erro no login.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginView.this, "Falha no login com o X", Toast.LENGTH_SHORT).show();
                })
        );
    }

    // Método chamado ao clicar no botão "Entrar"
    public void verificaUsuario(View view) {
        if (VerificaConexao.hasInternet(this)) { // Verifica se há conexão com a internet
            String emailUser = email.getText().toString(); // Obtém o email digitado
            String senhaUser = senha.getText().toString(); // Obtém a senha digitada

            if (!emailUser.isEmpty() && !senhaUser.isEmpty()) { // Verifica se os campos não estão vazios
                try {
                    // Tenta autenticar o usuário com email e senha
                    loginViewModel.autenticaUserEmailSenha(emailUser, senhaUser).observe(this, aBoolean -> {
                        if (aBoolean) {
                            // Se a autenticação for bem-sucedida
                            if (mAuth.getCurrentUser().isEmailVerified()) { // Verifica se o email está verificado
                                if (isServicoRodando()) { // Verifica se um serviço está em execução
                                    stopService(new Intent(this, ContadorService.class)); // Para o serviço
                                }
                                if (cb_lembrar.isChecked()) { // Verifica se a CheckBox está marcada
                                    mantemLogadoUsuario(emailUser, senhaUser); // Salva o email e senha nas SharedPreferences
                                }
                                proximaTela(); // Vai para a próxima tela
                                finish(); // Finaliza esta atividade
                            } else {
                                Toast.makeText(LoginView.this, "Usuário ainda não verificado", Toast.LENGTH_SHORT).show();
                                email.setText("");
                                senha.setText("");
                                startActivity(new Intent(LoginView.this, EnviaEmailVerificacao.class)); // Abre tela de envio de email de verificação
                            }
                        } else {
                            Toast.makeText(LoginView.this, "Email ou senha incorreta.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (FirebaseAuthException exception) {
                    Toast.makeText(this, "Erro na autenticação.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Todos os campos precisam ser preenchidos.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Sem conexão com a internet!", Toast.LENGTH_SHORT).show();
        }
    }

    // Método chamado ao retornar o resultado de uma atividade (ex: login com Google)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Resultado retornado lançado pela intent GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // login com o Google feito com sucesso, autentica no Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                loginViewModel.autenticaUserGoogle(account.getIdToken(), account).observe(this, aBoolean -> {
                    if (aBoolean) {
                        Toast.makeText(LoginView.this, "Bem vindo ao Owl!!!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginView.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginView.this, "Falha no login.", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (ApiException e) {
                // Ocorreu um erro ao tentar fazer login com o Google
                Toast.makeText(this, "Erro ao tentar fazer login com o Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método chamado quando o usuário clica no botão para ir para a tela de cadastro
    public void telaCadastro(View view) {
        email.setText("");
        senha.setText("");
        // Cria uma nova Intent para abrir a CadastroView
        startActivity(new Intent(this, CadastroView.class));
    }

    // Método chamado para avançar para a próxima tela após a autenticação bem-sucedida do usuário
    public void proximaTela() {
        // Cria uma nova Intent para abrir a MainActivity
        this.startActivity(new Intent(this, MainActivity.class));
        // Finaliza a LoginView atual
        this.finish();
    }

    // Método utilizado para manter o usuário logado, armazenando o email e senha no SharedPreferences
    private void mantemLogadoUsuario(String email, String senha) {
        // Obtém uma referência para o Editor do SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Insere o email e senha no SharedPreferences
        editor.putString(KEY_USER_LOGIN, email);
        editor.putString(KEY_USER_SENHA, senha);
        // Aplica as alterações no SharedPreferences
        editor.apply();
    }

    // Método chamado quando o usuário clica no botão de login com o Google
    private void logarComGoogle() {
        // Obtém uma Intent de login com o Google usando o GoogleSignInClient
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        // Inicia o processo de login com o Google utilizando o código de solicitação RC_SIGN_IN
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Método para verificar se um serviço está em execução
    private boolean isServicoRodando() {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // Obtenha todas as notificações ativas
            StatusBarNotification[] notifications = notificationManager.getActiveNotifications();

            // ID ou Tag da notificação associada ao seu serviço
            int seuServicoNotificationId = 1; // Defina o ID da sua notificação

            // Verifique se existe alguma notificação ativa associada ao seu serviço
            for (StatusBarNotification notification : notifications) {
                if (notification.getId() == seuServicoNotificationId) {
                    return true; // Notificação associada ao seu serviço encontrada
                }
            }
        }
        return false; // Nenhuma notificação associada ao seu serviço encontrada
    }
}
