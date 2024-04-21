package com.example.owlagenda.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.Usuario;
import com.example.owlagenda.ui.viewmodels.CadastroViewModel;

public class CadastroView extends AppCompatActivity {
    private CadastroViewModel cadastroViewModel;
    private EditText etNome, etSobre, etEmail, etSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cadastroViewModel = new ViewModelProvider(this).get(CadastroViewModel.class);

        etNome = findViewById(R.id.et_nome);
        etSobre= findViewById(R.id.et_sobrenome);
        etEmail = findViewById(R.id.et_email);
        etSenha = findViewById(R.id.et_senha);

    }

    public void cadastraUsuario(View view) {
        Usuario user = new Usuario(etNome.getText().toString(), etSobre.getText().toString(),
                etEmail.getText().toString(), etSenha.getText().toString());

        cadastroViewModel.cadastraBD(user).observe(this, sucesso -> {
            if (sucesso) {
                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erro ao cadastrar o usuário. Por favor, tente novamente.", Toast.LENGTH_SHORT).show();
            }
        });

    }
}