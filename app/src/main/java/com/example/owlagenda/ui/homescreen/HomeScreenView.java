package com.example.owlagenda.ui.homescreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.BuildConfig;
import com.example.owlagenda.R;
import com.example.owlagenda.databinding.ActivityHomeScreenBinding;
import com.example.owlagenda.ui.login.LoginView;
import com.example.owlagenda.ui.register.RegisterView;
import com.example.owlagenda.ui.homepage.HomePageView;
import com.example.owlagenda.util.SharedPreferencesUtil;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class HomeScreenView extends AppCompatActivity {
    private ActivityHomeScreenBinding binding;
    private HomeScreenViewModel viewModel;
    private CallbackManager callbackManager;
    private GetCredentialRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityHomeScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(HomeScreenViewModel.class);

        SharedPreferencesUtil.init(this);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null && SharedPreferencesUtil
                .getBoolean(SharedPreferencesUtil.KEY_USER_REMEMBER_ME, true)) {
            this.goToMainScreen();
            finish();
        }

        binding.btnRegisterHomescreen.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterView.class)));

        binding.btnLoginHomescreen.setOnClickListener(v ->
                startActivity(new Intent(this, LoginView.class)));

        GetSignInWithGoogleOption googleIdOption =
                new GetSignInWithGoogleOption.Builder(getString(R.string.default_web_client_id))
                        .build();

        request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        binding.btnGoogle.setOnClickListener(v -> loginWithGoogle());

        callbackManager = CallbackManager.Factory.create();
        binding.btnFacebook.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(HomeScreenView.this, Arrays.asList("email", "public_profile"));
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    viewModel.authUserWithFacebook(loginResult.getAccessToken()).observe(HomeScreenView.this, aBoolean -> {
                        if (aBoolean) {
                            Toast.makeText(HomeScreenView.this, "Bem vindo ao Owl", Toast.LENGTH_SHORT).show();
                            SharedPreferencesUtil.saveBoolean(SharedPreferencesUtil.KEY_USER_REMEMBER_ME, true);
                            goToMainScreen();
                        } else {
                            Toast.makeText(HomeScreenView.this, "Erro no login.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onCancel() {
                    Toast.makeText(HomeScreenView.this, "Erro de conexão com o Facebook", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(@NonNull FacebookException error) {
                    Toast.makeText(HomeScreenView.this, "Erro ao fazer login com o Facebook.", Toast.LENGTH_SHORT).show();
                }
            });
        });
        viewModel.isLoading().observe(this, aBoolean -> {
            if (aBoolean) {
                binding.linearProgressHomeScreen.setVisibility(View.VISIBLE);
            } else {
                binding.linearProgressHomeScreen.setVisibility(View.INVISIBLE);
            }
        });

        viewModel.getErrorMessageDialog().observe(this, s ->
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Erro")
                        .setMessage(s)
                        .setPositiveButton("Ok", (dialog, which) -> {
                        })
                        .show()
        );

    }

    private void goToMainScreen() {
        this.startActivity(new Intent(this, HomePageView.class));
        this.finish();
    }

    private void loginWithGoogle() {
        CredentialManager.create(this).getCredentialAsync(
                this,
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(GetCredentialResponse getCredentialResponse) {
                        Credential credential = getCredentialResponse.getCredential();

                        if (credential instanceof CustomCredential &&
                                GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                        .equals(credential.getType())) {

                            GoogleIdTokenCredential googleIdToken =
                                    GoogleIdTokenCredential.createFrom(credential.getData());

                            runOnUiThread(() ->
                                    viewModel.authUserWithGoogle(googleIdToken)
                                            .observe(HomeScreenView.this, aBoolean -> {
                                                if (aBoolean) {
                                                    Toast.makeText(
                                                            HomeScreenView.this,
                                                            "Bem vindo ao Owl!!!",
                                                            Toast.LENGTH_SHORT
                                                    ).show();
                                                    SharedPreferencesUtil.saveBoolean(
                                                            SharedPreferencesUtil.KEY_USER_REMEMBER_ME,
                                                            true
                                                    );
                                                    goToMainScreen();
                                                } else {
                                                    Toast.makeText(
                                                            HomeScreenView.this,
                                                            "Falha no login. Tente novamente.",
                                                            Toast.LENGTH_SHORT
                                                    ).show();
                                                }
                                            })
                            );
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                Toast.makeText(
                                        HomeScreenView.this,
                                        "Login Google cancelado ou indisponível",
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
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