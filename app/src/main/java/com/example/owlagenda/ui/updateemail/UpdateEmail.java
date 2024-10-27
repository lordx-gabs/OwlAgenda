package com.example.owlagenda.ui.updateemail;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.BuildConfig;
import com.example.owlagenda.R;
import com.example.owlagenda.databinding.ActivityUpdateEmailBinding;
import com.example.owlagenda.ui.homescreen.HomeScreenView;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class UpdateEmail extends AppCompatActivity {
    private UpdateEmailViewModel viewModel;
    private ActivityUpdateEmailBinding binding;
    private CallbackManager callbackManager;
    private GetCredentialRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_email);
        binding = ActivityUpdateEmailBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        callbackManager = CallbackManager.Factory.create();
        request = new GetCredentialRequest.Builder().addCredentialOption(new GetSignInWithGoogleOption
                .Builder(BuildConfig.tokenGoogle).build()).build();

        viewModel = new ViewModelProvider(this).get(UpdateEmailViewModel.class);

        FirebaseAuth.getInstance().getCurrentUser().reload().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                binding.etResetCurrentEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            } else {
                Toast.makeText(this, "Erro ao carregar informações do usuário.", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getErrorMessage().observe(this, s ->
                Toast.makeText(this, s, Toast.LENGTH_LONG).show());

        viewModel.getIsLoading().observe(this, aBoolean -> {
            if (aBoolean) {
                binding.loadingUpdateEmail.setVisibility(View.VISIBLE);
            } else {
                binding.loadingUpdateEmail.setVisibility(View.GONE);
            }
        });

        binding.toolbarResetEmail.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        viewModel.getIsReauthenticationRequired().observe(this, provideLogin -> {
            if (provideLogin.equals(GoogleAuthProvider.PROVIDER_ID)) {
                CredentialManager.create(this).getCredentialAsync(
                        this,
                        request,
                        new CancellationSignal(),
                        Executors.newSingleThreadExecutor(),
                        new CredentialManagerCallback<>() {
                            @Override
                            public void onResult(GetCredentialResponse getCredentialResponse) {
                                GoogleIdTokenCredential googleIdToken = GoogleIdTokenCredential.createFrom(getCredentialResponse.getCredential().getData());
                                runOnUiThread(() ->
                                        viewModel.reauthenticateWithGoogle(googleIdToken.getIdToken())
                                                .observe(UpdateEmail.this, aBoolean -> {
                                                    if (aBoolean) {
                                                        updateEmail();
                                                    } else {
                                                        Toast.makeText(UpdateEmail.this, "Erro de autenticação.", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                );
                            }

                            @Override
                            public void onError(@NonNull GetCredentialException e) {
                                if (e.getCause() instanceof IOException) {
                                    runOnUiThread(() ->
                                            Toast.makeText(UpdateEmail.this, "Falha de conexão. Verifique sua internet e tente novamente.", Toast.LENGTH_SHORT).show()
                                    );
                                } else {
                                    runOnUiThread(() ->
                                            Toast.makeText(UpdateEmail.this, "Erro ao fazer login com o Google. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show()
                                    );
                                }
                            }
                        }
                );

            } else if (provideLogin.equals(FacebookAuthProvider.PROVIDER_ID)) {
                LoginManager.getInstance().logInWithReadPermissions(UpdateEmail.this
                        , Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        viewModel.reauthenticateWithFacebook(loginResult.getAccessToken()
                                .getToken()).observe(UpdateEmail.this, aBoolean -> {
                            if (aBoolean) {
                                updateEmail();
                            } else {
                                Toast.makeText(UpdateEmail.this, "Erro de autenticação.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(UpdateEmail.this, "Erro de conexão com o Facebook", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull FacebookException error) {
                        Toast.makeText(UpdateEmail.this, "Erro ao fazer login com o Facebook.", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                LayoutInflater inflater = LayoutInflater.from(this);
                View dialogView = inflater.inflate(R.layout.dialog_password, null);

                // Campo de entrada de senha
                EditText inputPassword = dialogView.findViewById(R.id.et_password_dialog);
                inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                // Construir o diálogo
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Digite sua senha")
                        .setView(dialogView)
                        .setPositiveButton("Confirmar", (dialog, which) -> {
                            // Chamar o listener quando a senha for confirmada
                            viewModel.reauthenticateWithPassword(binding.etResetCurrentEmail
                                            .getText().toString().trim(), inputPassword.getText().toString())
                                    .observe(this, aBoolean -> {
                                        if (aBoolean) {
                                            updateEmail();
                                        } else {
                                            Toast.makeText(this, "Erro de autenticação.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });

        binding.btnResetEmail.setOnClickListener(v -> {
            if (binding.etResetNewEmail.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Insira um email novo.", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.etResetNewEmail.getText().toString().trim()).matches()) {
                Toast.makeText(this, "Insira um email válido.", Toast.LENGTH_SHORT).show();
            } else {
                updateEmail();
            }
        });
    }

    private void updateEmail() {
        viewModel.updateEmail(binding.etResetNewEmail.getText().toString().trim()).observe(this, aBoolean -> {
            if (aBoolean) {
                binding.textResetEmail.setText("Email de verificação enviado para seu novo email. Para atualizar seu email, siga as intruções enviadas para ele.");
                binding.textResetEmail.setTextSize(18);
                binding.etResetNewEmail.setText("");
            } else {
                Toast.makeText(this, "Erro ao enviar email de verificação", Toast.LENGTH_LONG).show();
            }
        });
    }

}