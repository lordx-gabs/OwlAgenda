package com.example.owlagenda.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.R;
import com.example.owlagenda.data.database.IniciarOuFecharDB;
import com.example.owlagenda.data.database.dao.UsuarioDao;
import com.example.owlagenda.data.models.Usuario;
import com.example.owlagenda.ui.viewmodels.LoginViewModel;
import com.example.owlagenda.util.VerificaConexao;
import com.google.firebase.database.DatabaseException;

public class LoginView extends AppCompatActivity {
    private LoginViewModel loginViewModel;
    private EditText email, senha;
    private ConnectivityManager.NetworkCallback networkCallback;

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
        email = findViewById(R.id.et_email);
        senha = findViewById(R.id.et_senha);

    }

    public void verificaUsuario(View view) {
        if (VerificaConexao.hasInternet(this)) {
            if (!email.getText().toString().isEmpty() && !senha.getText().toString().isEmpty()) {
                try {
                    loginViewModel.buscaPorEmailSenha(email.getText().toString(), senha.getText().toString())
                            .observe(LoginView.this, new Observer<Boolean>() {
                                @Override
                                public void onChanged(Boolean aBoolean) {
                                    if (aBoolean) {
                                        Intent proximaTela = new Intent(LoginView.this, MainActivity.class);
                                        proximaTela.putExtra("usuario", loginViewModel.getLiveDataUser().getValue());
                                        startActivity(proximaTela);
                                        finish();
                                    } else {
                                        AlertDialog.Builder alert = new AlertDialog.Builder(LoginView.this);
                                        alert.setMessage("Email ou senha incorreta!!!");
                                        alert.setNeutralButton("Ok", null);
                                        alert.setTitle("Mensagem");
                                        alert.create().show();
                                    }
                                }
                            });
                } catch (DatabaseException firebaseException) {
                    runOnUiThread(() -> Toast.makeText(LoginView.this, "Erro no banco de dados online!!! :(", Toast.LENGTH_SHORT).show());
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(LoginView.this, "Erro no banco de dados local!!! :(" + e.getMessage(), Toast.LENGTH_SHORT).show());
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

}