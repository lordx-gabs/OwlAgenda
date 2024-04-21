package com.example.owlagenda.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.R;
import com.example.owlagenda.ui.viewmodels.LoginViewModel;
import com.example.owlagenda.util.VerificaConexao;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class LoginView extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private LoginViewModel loginViewModel;
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_USER_LOGIN = "user_login";
    private static final String KEY_USER_SENHA = "user_senha";
    private static final String KEY_USER_LEMBRE_ME = "user_lembre_me";
    private EditText email, senha;
    private CheckBox cb_lembrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Verifica se o usuário já está autenticado
        mAuth = FirebaseAuth.getInstance();
        boolean escolhaLembreMe = sharedPreferences.getBoolean(KEY_USER_LEMBRE_ME, false);
        if (mAuth.getCurrentUser() != null && escolhaLembreMe) {
            // Usuário está autenticado
            this.proximaTela();
        } else {
            try {
                String email = sharedPreferences.getString(KEY_USER_LOGIN, ""), senha = sharedPreferences.getString(KEY_USER_SENHA, "");
                if (!email.isEmpty() && !senha.isEmpty() && escolhaLembreMe) {
                    loginViewModel.autenticaUser(email, senha).observe(this, new Observer<Boolean>() {
                        @Override
                        public void onChanged(Boolean aBoolean) {
                            if (aBoolean) {
                                proximaTela();
                            }
                        }
                    });
                }
            } catch (FirebaseAuthException e) {
                Toast.makeText(this, "Erro na autenticação.", Toast.LENGTH_SHORT).show();
            }
        }

        cb_lembrar = findViewById(R.id.cb_lembraruser);
        email = findViewById(R.id.et_email);
        senha = findViewById(R.id.et_senha);
    }

    public void proximaTela() {
        this.startActivity(new Intent(this, MainActivity.class));
        this.finish();
    }

    public void verificaUsuario(View view) {
        if (VerificaConexao.hasInternet(this)) {
            String emailUser = email.getText().toString();
            String senhaUser = senha.getText().toString();

            if (!emailUser.isEmpty() && !senhaUser.isEmpty()) {
                try {
                    loginViewModel.autenticaUser(email.getText().toString(), senha.getText().toString()).observe(this, new Observer<Boolean>() {
                        @Override
                        public void onChanged(Boolean aBoolean) {
                            if (aBoolean) {
                                mantemLogadoUsuario(emailUser, senhaUser);

                                proximaTela();
                            } else {
                                Toast.makeText(LoginView.this, "Email ou senha incorreta.", Toast.LENGTH_SHORT).show();
                            }
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

    public void telaCadastro(View view) {
        Intent telaCadastro = new Intent(this, CadastroView.class);
        startActivity(telaCadastro);
    }

    private void mantemLogadoUsuario(String email, String senha) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_USER_LEMBRE_ME, cb_lembrar.isChecked());
        editor.putString(KEY_USER_LOGIN, email);
        editor.putString(KEY_USER_SENHA, senha);

        editor.apply();
    }

}