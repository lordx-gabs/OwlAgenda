package com.example.owlagenda.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.Usuario;
import com.example.owlagenda.ui.viewmodels.CadastroViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class CadastroView extends AppCompatActivity {
    private CadastroViewModel cadastroViewModel;
    private static final int PICK_IMAGE_REQUEST = 1;
    private StorageReference storageReference;
    private Usuario user;
    private ImageView imagemUsuario;
    private EditText etNome, etSobre, etEmail, etSenha;
    private Uri caminhoImagem;
    private StorageReference fileRef;

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

        // Inicialize o Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        user = new Usuario();

        etNome = findViewById(R.id.et_nome);
        etSobre= findViewById(R.id.et_sobrenome);
        etEmail = findViewById(R.id.et_email);
        etSenha = findViewById(R.id.et_senha);
        imagemUsuario = findViewById(R.id.foto_usuario);

    }

    public void cadastraUsuario(View view) {
        String nome = etNome.getText().toString();
        String sobrenome = etSobre.getText().toString();
        String email = etEmail.getText().toString();
        String senha = etSenha.getText().toString();

        Usuario user = new Usuario();

        if(!nome.isEmpty() && !sobrenome.isEmpty() && !email.isEmpty()  && !senha.isEmpty()) {
            user.setNome(nome);
            user.setSobrenome(sobrenome);
            user.setEmail(email);
            user.setSenha(senha);
            try {
                cadastroViewModel.verificaExisteEmail(user.getEmail()).observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean) {
                            Toast.makeText(CadastroView.this, "Email já cadastrado no sistema!!!", Toast.LENGTH_SHORT).show();
                        } else {
                            if (caminhoImagem != null) {
                                // Upload bem-sucedido
                                fileRef.putFile(caminhoImagem).addOnSuccessListener(CadastroView.this, taskSnapshot -> {
                                    // Captura a url do arquivo armazenado no Storage, guarda e cadastra o usuario no banco de dados.
                                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                        user.setUrl_foto_perfil(uri.toString());
                                    });
                                    // Falha no upload do arquivo.
                                }).addOnFailureListener(CadastroView.this, e -> Toast.makeText(CadastroView.this,
                                        "Falha no envio da foto de perfil, por favor tente novamente", Toast.LENGTH_SHORT).show());
                            }
                            cadastrarBD(user);
                        }
                    }
                });

            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Todos os campos precisam ser preenchidos.", Toast.LENGTH_SHORT).show();
        }

    }

    private void cadastrarBD(Usuario user) {
        cadastroViewModel.cadastraBD(user).observe(this, sucesso -> {
            if (sucesso) {
                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erro ao cadastrar o usuário. Por favor, tente novamente.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void escolherImagem(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uriImagemSelecionada = data.getData();
            if (uriImagemSelecionada != null) {
                // Captura o caminho da imagem selecionada e referencia esse caminho no Storage
                fileRef = storageReference.child("fotosdeperfil/" + uriImagemSelecionada.getLastPathSegment());
                imagemUsuario.setImageURI(uriImagemSelecionada);
                caminhoImagem = uriImagemSelecionada;

            }
        }
    }
}