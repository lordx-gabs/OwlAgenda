package com.example.owlagenda.ui.forgotpassword;

import static com.example.owlagenda.ui.forgotpassword.ForgotPasswordViewModel.KEY_USER_TIMESTAMP;
import static com.example.owlagenda.ui.login.LoginView.PREF_NAME;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;

import java.time.Instant;
import java.time.temporal.ChronoField;

public class ForgotPasswordView extends AppCompatActivity {
    private EditText emailEditText;
    private TextView message;
    private SharedPreferences userTimeCredentials;
    private ForgotPasswordViewModel viewModel;
    private long timeLeft;
    private LinearProgressIndicator loadingProgress;
    private MaterialButton btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailEditText = findViewById(R.id.et_email_reset_password);
        message = findViewById(R.id.tv_mensagem);
        loadingProgress = findViewById(R.id.progress_indicator_forgot);
        btnSend = findViewById(R.id.btn_send_email);

        userTimeCredentials = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        viewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                TextInputLayout textInputLayout = findViewById(R.id.et_email_layout_reset_password);
                int boxStrokeColor;
                if (Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches() || s.toString().isEmpty()) {
                    boxStrokeColor = getColor(R.color.botao_cor);
                } else {
                    boxStrokeColor = getColor(R.color.cor_primaria);
                }
                textInputLayout.setBoxStrokeColor(boxStrokeColor);
            }
        });

        viewModel.isLoading().observe(this, aBoolean -> {
            if (aBoolean) {
                btnSend.setEnabled(false);
                loadingProgress.setVisibility(View.VISIBLE);
            } else {
                loadingProgress.setVisibility(View.GONE);
                btnSend.setEnabled(true);
            }
        });
    }

    public void sendEmailResetPassword(View v) {
        String email = emailEditText.getText().toString();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

        if (!email.isEmpty()) {
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                viewModel.fetchCurrentTimeFirebase().observe(this, instantTimestampServer -> {
                    if (instantTimestampServer != null) {
                        if (isInstantTimestampValid(instantTimestampServer)) {
                            viewModel.sendResetPasswordEmail(email).observe(this, aBoolean -> {
                                message.setVisibility(View.VISIBLE);
                                if (aBoolean) {
                                    message.setText("Caso o email corresponder a uma das contas no Owl, será enviado um e-mail com instruções para redefinir a senha!");
                                } else {
                                    message.setError("Erro ao enviar e-mail, tente novamente");
                                }
                                emailEditText.setText("");
                                viewModel.saveTimestampUserShared(userTimeCredentials, instantTimestampServer.getLong(ChronoField.INSTANT_SECONDS));
                            });

                        } else {
                            new MaterialAlertDialogBuilder(this)
                                    .setTitle("Owl Agenda")
                                    .setMessage("Aguarde o término da contagem para enviar outro email de redefinição de senha. Tempo restante: " + timeLeft + "segundos")
                                    .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                                    .show();
                        }
                    } else {
                        Toast.makeText(this, "Erro no envio do email de redefinição de senha, tente novamente mais tarde", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                message.setVisibility(View.VISIBLE);
                message.setError("Digite um email válido");
            }
        } else {
            message.setVisibility(View.VISIBLE);
            message.setError("Preencha o campo de e-mail");
        }
    }

    private boolean isInstantTimestampValid(Instant instantTimestampServer) {
        long oneMinuteThirtySeconds = 90;
        Instant instantTimestampUser = Instant.ofEpochSecond(userTimeCredentials.getLong(KEY_USER_TIMESTAMP,
                instantTimestampServer.plusSeconds(oneMinuteThirtySeconds).getLong(ChronoField.INSTANT_SECONDS)));
        timeLeft = 90 - (instantTimestampServer.getEpochSecond() - instantTimestampUser.getEpochSecond());
        return timeLeft <= 0;
    }
}