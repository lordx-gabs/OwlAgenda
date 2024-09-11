package com.example.owlagenda.ui.homescreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.BuildConfig;
import com.example.owlagenda.R;
import com.example.owlagenda.databinding.ActivityHomeScreenBinding;
import com.example.owlagenda.ui.login.LoginView;
import com.example.owlagenda.ui.register.RegisterView;
import com.example.owlagenda.ui.telaprincipal.TelaPrincipalView;
import com.example.owlagenda.util.SharedPreferencesUtil;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class HomeScreenView extends AppCompatActivity {
    private ActivityHomeScreenBinding binding;
    private HomeScreenViewModel viewModel;
    private FirebaseAuth firebaseAuth;
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
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null && SharedPreferencesUtil
                .getBoolean(SharedPreferencesUtil.KEY_USER_REMEMBER_ME, true)) {
            this.goToMainScreen();
            finish();
        }

        binding.btnRegisterHomescreen.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterView.class)));

        binding.btnLoginHomescreen.setOnClickListener(v ->
                startActivity(new Intent(this, LoginView.class)));

        request = new GetCredentialRequest.Builder().addCredentialOption(new GetSignInWithGoogleOption
                .Builder(BuildConfig.tokenGoogle).build()).build();

        binding.btnTesteeee.setOnClickListener(v -> loginWithGoogle());

        callbackManager = CallbackManager.Factory.create();
        binding.btnTesteFace.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(HomeScreenView.this
            ,Arrays.asList("email", "public_profile"));
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
            if(aBoolean) {
                binding.linearProgressHomeScreen.setVisibility(View.VISIBLE);
            } else {
                binding.linearProgressHomeScreen.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void goToMainScreen() {
        this.startActivity(new Intent(this, TelaPrincipalView.class));
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
                        GoogleIdTokenCredential googleIdToken = GoogleIdTokenCredential.createFrom(getCredentialResponse.getCredential().getData());
                        runOnUiThread(() ->
                                viewModel.authUserWithGoogle(googleIdToken).observe(HomeScreenView.this, aBoolean -> {
                                    if (aBoolean) {
                                        Toast.makeText(HomeScreenView.this, "Bem vindo ao Owl!!!", Toast.LENGTH_SHORT).show();
                                        SharedPreferencesUtil.saveBoolean(SharedPreferencesUtil.KEY_USER_REMEMBER_ME, true);
                                        goToMainScreen();
                                    } else {
                                        Toast.makeText(HomeScreenView.this, "Falha no login. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                        );
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        if (e.getCause() instanceof IOException) {
                            runOnUiThread(() ->
                                    Toast.makeText(HomeScreenView.this, "Falha de conexão. Verifique sua internet e tente novamente.", Toast.LENGTH_SHORT).show()
                            );
                        } else {
                            runOnUiThread(() ->
                                    Toast.makeText(HomeScreenView.this, "Erro ao fazer login com o Google. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show()
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