package com.example.owlagenda.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.R;
import com.example.owlagenda.ui.forgotpassword.ForgotPasswordView;
import com.example.owlagenda.ui.telaprincipal.TelaPrincipalView;
import com.example.owlagenda.ui.registration.RegistrationView;
import com.example.owlagenda.util.Notification;
import com.example.owlagenda.util.VerificationWifi;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class LoginView extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private SharedPreferences userCredentialsPreferences;
    private LoginViewModel loginViewModel;
    public static final String PREF_NAME = "MyPrefs";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PASSWORD = "user_password";
    private GoogleSignInClient mGoogleSignInClient;
    private Button btnGoogle;
    private Button btnTwitter;
    private EditText emailEditText, passwordEditText;
    private CheckBox rememberMeCheckBox;
    private TextView forgotPasswordTextView;
    private ActivityResultLauncher<Intent> loginGoogleLauncher;
    OAuthProvider.Builder providerAuthTwitter = OAuthProvider.newBuilder("twitter.com");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        this.setContentView(R.layout.activity_login_view);

        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance());

        Notification.getNotificationChannel(getApplicationContext());

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        userCredentialsPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        firebaseAuth = FirebaseAuth.getInstance();

        providerAuthTwitter.addCustomParameter("lang", "br");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()) {
            this.nextView();
            finish();
        } else {
            try {
                String email = userCredentialsPreferences.getString(KEY_USER_EMAIL, "");
                String password = userCredentialsPreferences.getString(KEY_USER_PASSWORD, "");
                if (!email.isEmpty() && !password.isEmpty()) {
                    loginViewModel.authUserWithEmailAndPassword(email, password).observe(this, aBoolean -> {
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
        emailEditText = findViewById(R.id.et_email_reset_password);
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
                startActivity(new Intent(LoginView.this, ForgotPasswordView.class)));

        emailEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                TextInputLayout textInputLayout = findViewById(R.id.et_email_layout_reset_password);
                if (Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString()).matches()) {
                    int cor = getColor(R.color.botao_cor);
                    textInputLayout.setBoxStrokeColor(cor);
                } else {
                    int cor = getColor(R.color.cor_primaria);
                    textInputLayout.setBoxStrokeColor(cor);
                }
            }
        });

        loginGoogleLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);

                            loginViewModel.authUserWithGoogle(account).observe(this, aBoolean -> {
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
                });

    }

    public void loginUser(View view) {
        if (VerificationWifi.hasInternet(this)) {
            String emailUser = emailEditText.getText().toString();
            String passwordUser = passwordEditText.getText().toString();

            if (!emailUser.isEmpty() && !passwordUser.isEmpty()) {
                try {
                    loginViewModel.authUserWithEmailAndPassword(emailUser, passwordUser).observe(this, aBoolean -> {
                        if (aBoolean) {
                            if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                                if (rememberMeCheckBox.isChecked()) {
                                    keepsUserLogged(emailUser, passwordUser);
                                }
                                nextView();
                                finish();
                            } else {
                                new MaterialAlertDialogBuilder(this)
                                        .setTitle("Verificação de email não realizada.")
                                        .setMessage("Seu email ainda não foi verificado, por favor verifique sua caixa de entrada ou de spam do seu email, caso não tenha recebido, clique em Enviar novamente.")
                                        .setNeutralButton("Ok", (dialog, which) -> dialog.dismiss())
                                        .setPositiveButton("Enviar novamente", (dialog, which) ->
                                                firebaseAuth.getCurrentUser().sendEmailVerification())
                                        .show();
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

    public void goToViewRegister(View view) {
        emailEditText.setText("");
        passwordEditText.setText("");
        startActivity(new Intent(this, RegistrationView.class));
    }

    public void nextView() {
        this.startActivity(new Intent(this, TelaPrincipalView.class));
        this.finish();
    }

    private void keepsUserLogged(String email, String password) {
        SharedPreferences.Editor editor = userCredentialsPreferences.edit();
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_PASSWORD, password);
        editor.apply();
    }

    private void loginWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        loginGoogleLauncher.launch(signInIntent);
    }

}