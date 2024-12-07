package com.example.owlagenda.ui.login;

import static com.example.owlagenda.util.SharedPreferencesUtil.KEY_USER_REMEMBER_ME;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.R;
import com.example.owlagenda.ui.forgotpassword.ForgotPasswordView;
import com.example.owlagenda.ui.homepage.HomePageView;
import com.example.owlagenda.util.SharedPreferencesUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;

public class LoginView extends AppCompatActivity {
    private LoginViewModel loginViewModel;
    private MaterialButton btnLogin;
    private EditText emailEditText, passwordEditText;
    private CheckBox rememberMeCheckBox;
    private LinearProgressIndicator loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        this.setContentView(R.layout.activity_login);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        SharedPreferencesUtil.init(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_login);
        rememberMeCheckBox = findViewById(R.id.cb_lembraruser);
        loadingProgress = findViewById(R.id.loadingBarLogin);
        emailEditText = findViewById(R.id.et_email_login);
        passwordEditText = findViewById(R.id.et_senha_login);
        TextView forgotPasswordTextView = findViewById(R.id.tv_esqueci_senha);
        btnLogin = findViewById(R.id.btn_login);

        String email = getIntent().getStringExtra("emailUser");
        String firstName = getIntent().getStringExtra("firstNameUser");
        if (email != null && firstName != null) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle("Owl Agenda")
                    .setMessage("Olá senhor(a) " + firstName + ".\nFicamos feliz com o seu cadastro na Owl, faça seu login " +
                            "para ter acesso a todas as funcionalidades do Owl Agenda!\nEnviamos um email de " +
                            "confirmação para o seu email " + email + "\nNão esqueça de ativar ele mais tarde, aproveite " +
                            "o nosso aplicativo. :)")
                    .setPositiveButton("Ok", (dialog, which) -> {})
                    .create()
                    .show();
        }

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

        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    public void loginUser(View view) {
        if(loadingProgress.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, "Aguarde...", Toast.LENGTH_SHORT).show();
            return;
        }
        String emailUser = emailEditText.getText().toString();
        String passwordUser = passwordEditText.getText().toString();
        if (!emailUser.isEmpty() && !passwordUser.isEmpty()) {
            loginViewModel.authUserWithEmailAndPassword(emailUser, passwordUser).observe(this, aBoolean -> {
                if (aBoolean) {
                    keepsUserLogged(rememberMeCheckBox.isChecked());
                    Toast.makeText(LoginView.this, "Bem vindo ao Owl", Toast.LENGTH_SHORT).show();
                    nextView();
                } else {
                    Toast.makeText(LoginView.this, "Erro na autenticação, tente novamente.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Todos os campos precisam ser preenchidos.", Toast.LENGTH_SHORT).show();
        }
    }

    private void nextView() {
        Intent intent = new Intent(getApplicationContext(), HomePageView.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void keepsUserLogged(boolean value) {
        SharedPreferencesUtil.saveBoolean(KEY_USER_REMEMBER_ME, value);
    }
}