package com.example.owlagenda.ui.login;

import static com.example.owlagenda.util.SharedPreferencesUtil.KEY_USER_REMEMBER_ME;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.R;
import com.example.owlagenda.ui.forgotpassword.ForgotPasswordView;
import com.example.owlagenda.ui.telaprincipal.TelaPrincipalView;
import com.example.owlagenda.ui.register.RegisterView;
import com.example.owlagenda.util.SharedPreferencesUtil;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginView extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private LoginViewModel loginViewModel;
    private MaterialButton btnLogin;
    private SignInButton btnGoogle;
    private EditText emailEditText, passwordEditText;
    private CheckBox rememberMeCheckBox;
    private TextView forgotPasswordTextView;
    private CallbackManager callbackManager;
    private LoginButton btnFacebook;
    private LinearProgressIndicator loadingProgress;
    private GetCredentialRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        this.setContentView(R.layout.activity_login_view);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        SharedPreferencesUtil.init(this);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified() && SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.KEY_USER_REMEMBER_ME, true)) {
            this.nextView();
            finish();
        }

        rememberMeCheckBox = findViewById(R.id.cb_lembraruser);
        loadingProgress = findViewById(R.id.loadingBarLogin);
        emailEditText = findViewById(R.id.et_email_login);
        passwordEditText = findViewById(R.id.et_senha_login);
        forgotPasswordTextView = findViewById(R.id.tv_esqueci_senha);
        btnLogin = findViewById(R.id.btn_login);

        btnGoogle = findViewById(R.id.btn_google);
        btnGoogle.setOnClickListener(v -> loginWithGoogle());
        request = new GetCredentialRequest.Builder().addCredentialOption(new GetSignInWithGoogleOption
                .Builder(getString(R.string.default_web_client_id)).build()).build();

        btnFacebook = findViewById(R.id.login_button);
        btnFacebook.setPermissions("email", "public_profile");
        callbackManager = CallbackManager.Factory.create();
        btnFacebook.registerCallback(callbackManager, new FacebookCallback<>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                loginViewModel.authUserWithFacebook(loginResult.getAccessToken()).observe(LoginView.this, aBoolean -> {
                    if (aBoolean) {
                        Toast.makeText(LoginView.this, "Bem vindo ao Owl", Toast.LENGTH_SHORT).show();
                        keepsUserLogged(true);
                        nextView();
                    } else {
                        Toast.makeText(LoginView.this, "Erro no login.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginView.this, "Erro de conexão com o Facebook", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull FacebookException error) {
                Toast.makeText(LoginView.this, "Erro ao fazer login com o Facebook.", Toast.LENGTH_SHORT).show();
            }
        });

        forgotPasswordTextView.setOnClickListener(v ->
                startActivity(new Intent(LoginView.this, ForgotPasswordView.class)));

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                TextInputLayout textInputLayout = findViewById(R.id.et_email_layout_login);
                int boxStrokeColor;
                if (Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString()).matches()) {
                    boxStrokeColor = getColor(R.color.botao_cor);
                } else {
                    boxStrokeColor = getColor(R.color.cor_primaria);
                }
                textInputLayout.setBoxStrokeColor(boxStrokeColor);
            }
        });

        loginViewModel.getErrorMessage().observe(this, s ->
                Toast.makeText(this, s, Toast.LENGTH_SHORT).show());

        loginViewModel.isLoading().observe(this, aBoolean -> {
            if (aBoolean) {
                btnLogin.setEnabled(false);
                loadingProgress.setVisibility(View.VISIBLE);
            } else {
                loadingProgress.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
            }
        });
    }

    public void loginUser(View view) {
        String emailUser = emailEditText.getText().toString();
        String passwordUser = passwordEditText.getText().toString();
        if (!emailUser.isEmpty() && !passwordUser.isEmpty()) {
            loginViewModel.authUserWithEmailAndPassword(emailUser, passwordUser).observe(this, aBoolean -> {
                if (aBoolean) {
                    if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                        keepsUserLogged(rememberMeCheckBox.isChecked());
                        Toast.makeText(LoginView.this, "Bem vindo ao Owl", Toast.LENGTH_SHORT).show();
                        nextView();
                    } else {
                        new MaterialAlertDialogBuilder(this)
                                .setTitle("Verificação de email não realizada.")
                                .setMessage("Seu email ainda não foi verificado, por favor verifique sua caixa de entrada ou de spam do seu email, caso não tenha recebido o email de verificação, clique em Enviar novamente.")
                                .setNeutralButton("Ok", (dialog, which) -> dialog.dismiss())
                                .setPositiveButton("Enviar novamente", (dialog, which) ->
                                        firebaseAuth.getCurrentUser().sendEmailVerification())
                                .show();
                    }
                } else {
                    Toast.makeText(LoginView.this, "Erro na autenticação, tente novamente.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Todos os campos precisam ser preenchidos.", Toast.LENGTH_SHORT).show();
        }
    }

    public void goToViewRegister(View view) {
        emailEditText.setText("");
        passwordEditText.setText("");
        startActivity(new Intent(this, RegisterView.class));
    }

    private void nextView() {
        this.startActivity(new Intent(this, TelaPrincipalView.class));
        this.finish();
    }

    private void keepsUserLogged(boolean value) {
        SharedPreferencesUtil.saveBoolean(KEY_USER_REMEMBER_ME, value);
    }

    private void loginWithGoogle() {
        Executor executor = Executors.newSingleThreadExecutor();
        CredentialManager.create(this).getCredentialAsync(
                this,
                request,
                new CancellationSignal(),
                executor,
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(GetCredentialResponse getCredentialResponse) {
                        GoogleIdTokenCredential googleIdToken = GoogleIdTokenCredential.createFrom(getCredentialResponse.getCredential().getData());
                        runOnUiThread(() ->
                                loginViewModel.authUserWithGoogle(googleIdToken).observe(LoginView.this, aBoolean -> {
                                    if (aBoolean) {
                                        Toast.makeText(LoginView.this, "Bem vindo ao Owl!!!", Toast.LENGTH_SHORT).show();
                                        nextView();
                                        keepsUserLogged(true);
                                    } else {
                                        Toast.makeText(LoginView.this, "Falha no login. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                        );
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        if (e.getCause() instanceof IOException) {
                            runOnUiThread(() ->
                                    Toast.makeText(LoginView.this, "Falha de conexão. Verifique sua internet e tente novamente.", Toast.LENGTH_SHORT).show()
                            );
                        } else {
                            runOnUiThread(() ->
                                    Toast.makeText(LoginView.this, "Erro ao fazer login com o Google. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}