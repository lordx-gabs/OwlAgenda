package com.example.owlagenda.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

public class LoginView extends AppCompatActivity {
    private LoginViewModel loginViewModel;
    private EditText email, senha;

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
        loginViewModel.buscaPorEmailSenha(email.getText().toString(), senha.getText().toString()).observe(this, new Observer<Usuario>() {
            @Override
            public void onChanged(Usuario usuario) {
                if (usuario != null) {
                    Intent proximaTela = new Intent(LoginView.this, MainActivity.class);
                    proximaTela.putExtra("usuario", usuario);
                    startActivity(proximaTela);
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(LoginView.this);
                    alert.setMessage("Usuário não encontrado :(");
                    alert.setNeutralButton("Ok", null);
                    alert.setTitle("Mensagem");
                    alert.create().show();
                }
            }
        });
    }
}