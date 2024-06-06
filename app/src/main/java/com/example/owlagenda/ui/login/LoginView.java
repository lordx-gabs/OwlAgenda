package com.example.owlagenda.ui.login;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.BuildConfig;
import com.example.owlagenda.R;
import com.example.owlagenda.ui.activities.EsqueciSenhaView;
import com.example.owlagenda.ui.telaprincipal.TelaPrincipalView;
import com.example.owlagenda.ui.registration.RegistrationView;
import com.example.owlagenda.ui.emailverificacao.EmailVerificationView;
import com.example.owlagenda.util.Notificacao;
import com.example.owlagenda.util.VerificationWifi;
import com.example.owlagenda.util.services.ContadorService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.OAuthProvider;

public class LoginView extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private SharedPreferences userCredentialsPreferences;
    private LoginViewModel loginViewModel; 
    private static final String PREF_NAME = "MyPrefs"; 
    private static final String KEY_USER_LOGIN = "user_login"; 
    private static final String KEY_USER_SENHA = "user_senha"; 
    private static final int REQUEST_CODE_SIGN_IN_GOOGLE = 1399;
    private GoogleSignInClient mGoogleSignInClient; 
    private Button btnGoogle; 
    private Button btnTwitter; 
    private EditText emailEditText, passwordEditText;
    private CheckBox rememberMeCheckBox;
    private TextView forgotPasswordTextView;
    OAuthProvider.Builder providerAuthTwitter = OAuthProvider.newBuilder("twitter.com");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        this.setContentView(R.layout.activity_login_view);
        Notificacao.criarCanalDeNotificacao(getApplicationContext());

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        userCredentialsPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        firebaseAuth = FirebaseAuth.getInstance();

        providerAuthTwitter.addCustomParameter("lang", "br");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.tokenGoogle)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()) {
            this.nextView();
            finish();
        } else {
            try {
                String email = userCredentialsPreferences.getString(KEY_USER_LOGIN, "");
                String senha = userCredentialsPreferences.getString(KEY_USER_SENHA, "");
                if (!email.isEmpty() && !senha.isEmpty()) {
                    loginViewModel.authUserWithEmailAndPassoword(email, senha).observe(this, aBoolean -> {
                        if (aBoolean) {
                            nextView();
                            finish();
                        }
                    });
                }
            } catch (FirebaseAuthException e) {
                Toast.makeText(this, "Erro na autenticação, por favor faça login novamente.", Toast.LENGTH_SHORT).show();
            }
        }

        rememberMeCheckBox = findViewById(R.id.cb_lembraruser);
        emailEditText = findViewById(R.id.et_email_login);
        passwordEditText = findViewById(R.id.et_senha_login);
        forgotPasswordTextView = findViewById(R.id.tv_esqueci_senha);

        btnTwitter = findViewById(R.id.btn_twitter);
        btnGoogle = findViewById(R.id.btn_google);

        btnGoogle.setOnClickListener(v -> loginWithGoogle());

        btnTwitter.setOnClickListener(v -> firebaseAuth
                .startActivityForSignInWithProvider(LoginView.this, providerAuthTwitter.build())
                .addOnSuccessListener(authResult -> {
                    loginViewModel.authUserWithTwitter(authResult).observe(LoginView.this, aBoolean -> {
                        if (aBoolean) {
                            Toast.makeText(LoginView.this, "Bem vindo ao Owl", Toast.LENGTH_SHORT).show();
                            nextView();
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

        forgotPasswordTextView.setOnClickListener(v ->
                startActivity(new Intent(LoginView.this, EsqueciSenhaView.class)));

        // fix me: mudando a cor do email mesmo sem estar focado
        emailEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                TextInputLayout textInputLayout = findViewById(R.id.et_email_layout_login);
                if (Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString()).matches()) {
                    int cor = getColor(R.color.botao_cor);
                    textInputLayout.setBoxStrokeColor(cor);
                } else {
                    int cor = getColor(R.color.cor_primaria);
                    textInputLayout.setBoxStrokeColor(cor);
                }
            }
        });

    }

    public void loginUser(View view) {
        if (VerificationWifi.hasInternet(this)) {
            String emailUser = emailEditText.getText().toString();
            String passwordUser = passwordEditText.getText().toString();

            if (!emailUser.isEmpty() && !passwordUser.isEmpty()) {
                try {
                    loginViewModel.authUserWithEmailAndPassoword(emailUser, passwordUser).observe(this, aBoolean -> {
                        if (aBoolean) {
                            if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                                if (isServiceCounterRunning()) { 
                                    stopService(new Intent(this, ContadorService.class));
                                }
                                if (rememberMeCheckBox.isChecked()) {
                                    keepsUserLogged(emailUser, passwordUser);
                                }
                                nextView();
                                finish();
                            } else {
                                Toast.makeText(LoginView.this, "Usuário ainda não verificado", Toast.LENGTH_SHORT).show();
                                emailEditText.setText("");
                                passwordEditText.setText("");
                                startActivity(new Intent(LoginView.this, EmailVerificationView.class));
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

        if (requestCode == REQUEST_CODE_SIGN_IN_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // login com o Google feito com sucesso, autentica no Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                loginViewModel.authUserWithGoogle(account.getIdToken(), account).observe(this, aBoolean -> {
                    if (aBoolean) {
                        Toast.makeText(LoginView.this, "Bem vindo ao Owl!!!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginView.this, TelaPrincipalView.class));
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
    
    public void goToViewRegister(View view) {
        emailEditText.setText("");
        passwordEditText.setText("");
        startActivity(new Intent(this, RegistrationView.class));
    }

    public void nextView() {
        this.startActivity(new Intent(this, TelaPrincipalView.class));
        this.finish();
    }

    private void keepsUserLogged(String email, String senha) {
        SharedPreferences.Editor editor = userCredentialsPreferences.edit();
        editor.putString(KEY_USER_LOGIN, email);
        editor.putString(KEY_USER_SENHA, senha);
        editor.apply();
    }

    private void loginWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN_GOOGLE);
    }

    private boolean isServiceCounterRunning() {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            StatusBarNotification[] notifications = notificationManager.getActiveNotifications();

            int seuServicoNotificationId = 1;

            for (StatusBarNotification notification : notifications) {
                if (notification.getId() == seuServicoNotificationId) {
                    return true;
                }
            }
        }
        return false;
    }
}