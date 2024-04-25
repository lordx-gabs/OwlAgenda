package com.example.owlagenda.ui.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
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
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.OAuthProvider;

public class LoginView extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private LoginViewModel loginViewModel;
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_USER_LOGIN = "user_login";
    private static final String KEY_USER_SENHA = "user_senha";
    private static final int RC_SIGN_IN = 1399; // Você pode usar qualquer número aqui
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton btnGoogle;
    private Button btnTwitter;
    private EditText email, senha;
    private CheckBox cb_lembrar;
    OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_view);
        Notificacao.criarCanalDeNotificacao(getApplicationContext());

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();

        provider.addCustomParameter("lang", "br");

        // Configure sign-in to request the user’s basic profile like name and email
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.id_google))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        // Verifica se o usuário já está autenticado
        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
            // Usuário está autenticado
            this.proximaTela();
            finish();
        } else {
            try {
                String email = sharedPreferences.getString(KEY_USER_LOGIN, ""), senha = sharedPreferences.getString(KEY_USER_SENHA, "");
                if (!email.isEmpty() && !senha.isEmpty()) {
                    loginViewModel.autenticaUserEmailSenha(email, senha).observe(this, aBoolean -> {
                        if (aBoolean) {
                            proximaTela();
                            finish();
                        }
                    });
                }
            } catch (FirebaseAuthException e) {
                Toast.makeText(this, "Erro na autenticação, por favor faça login novamente.", Toast.LENGTH_SHORT).show();
            }
        }

        cb_lembrar = findViewById(R.id.cb_lembraruser);
        email = findViewById(R.id.et_email);
        senha = findViewById(R.id.et_senha);

        btnTwitter = findViewById(R.id.btn_twitter);

        btnGoogle = findViewById(R.id.btn_google_login);

        btnGoogle.setOnClickListener(v -> logarComGoogle());

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
                            // User is signed in.
                            // IdP data available in
                            // authResult.getAdditionalUserInfo().getProfile().
                        })
                .addOnFailureListener(e -> {
                        Toast.makeText(LoginView.this, "Falha na comunicação com o X", Toast.LENGTH_SHORT).show();
                })
        );

    }

    public void verificaUsuario(View view) {
        if (VerificaConexao.hasInternet(this)) {
            String emailUser = email.getText().toString();
            String senhaUser = senha.getText().toString();

            if (!emailUser.isEmpty() && !senhaUser.isEmpty()) {
                try {
                    loginViewModel.autenticaUserEmailSenha(emailUser, senhaUser).observe(this, aBoolean -> {
                        if (aBoolean) {
                            if (mAuth.getCurrentUser().isEmailVerified()) {
                                if (isServicoRodando()) {
                                    stopService(new Intent(this, ContadorService.class));
                                }
                                if (cb_lembrar.isChecked()) {
                                    mantemLogadoUsuario(emailUser, senhaUser);
                                }
                                proximaTela();
                                finish();
                            } else {
                                Toast.makeText(LoginView.this, "Usuário ainda não verificado", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginView.this, EnviaEmailVerificacao.class));
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                //authenticating user with firebase using received token id
                // Faça algo com o ID Token, como autenticar o usuário usando Firebase Auth

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

    public void telaCadastro(View view) {
        startActivity(new Intent(this, CadastroView.class));
    }

    public void proximaTela() {
        this.startActivity(new Intent(this, MainActivity.class));
        this.finish();
    }

    private void mantemLogadoUsuario(String email, String senha) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_LOGIN, email);
        editor.putString(KEY_USER_SENHA, senha);

        editor.apply();
    }

    private void logarComGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Método para verificar se um serviço está em execução
    //rever pois pode ter mais notificações que n tem a vercom o serviço
    private boolean isServicoRodando() {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // Para versões do Android a partir do Marshmallow (API nível 23)
            return notificationManager.getActiveNotifications().length > 0;
        }
        return false;
    }
}
