package com.example.owlagenda.ui.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.Usuario;
import com.example.owlagenda.ui.viewmodels.CadastroViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CadastroView extends AppCompatActivity {
    private CadastroViewModel cadastroViewModel;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Usuario user;
    private ImageView imagemUsuario;
    private EditText etNome, etSobre, etEmail, etSenha, etDataNascimento, etNumero;
    private Uri caminhoImagem;
    private Calendar calendario;
    private DatePickerDialog date;
    private AutoCompleteTextView escolhaSexo;
    private String[] opcoesSexo = {"Masculino", "Feminino", "Outros"};
    private String sexoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Habilita a borda a borda na tela (EdgeToEdge)
        EdgeToEdge.enable(this);

        // Define o layout da atividade
        setContentView(R.layout.activity_cadastro_view);

        // Aplica o recuo necessário para as barras do sistema (status bar e navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //Iniciliza o calendario.
        calendario = Calendar.getInstance();

        // Inicializa o ViewModel para o cadastro
        cadastroViewModel = new ViewModelProvider(this).get(CadastroViewModel.class);

        // Inicializa os elementos de UI
        user = new Usuario();
        etNome = findViewById(R.id.et_nome);
        etSobre = findViewById(R.id.et_sobrenome);
        etEmail = findViewById(R.id.et_email);
        etSenha = findViewById(R.id.et_confirma_senha);
        escolhaSexo = findViewById(R.id.auto_complete_text_view);
        etDataNascimento = findViewById(R.id.et_data_nascimento);
        etNumero = findViewById(R.id.et_telefone);
        imagemUsuario = findViewById(R.id.foto_usuario);

        // Inicializa o adapter que sera utlizado no Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,  android.R.layout.simple_dropdown_item_1line, opcoesSexo);
        escolhaSexo.setAdapter(adapter);

        escolhaSexo.setOnItemClickListener((parent, view, position, id) -> sexoText = opcoesSexo[position]);

        date = new DatePickerDialog(CadastroView.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar dataSelecionado =  Calendar.getInstance();
                dataSelecionado.set(year, month, dayOfMonth);
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                etDataNascimento.setText(format.format(dataSelecionado.getTime()));

            }
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH));

        etDataNascimento.setOnClickListener(v -> date.show());

    }

    // Método chamado quando o botão de cadastro é clicado
    public void cadastraUsuario(View view) {
        String nome = etNome.getText().toString();
        String sobrenome = etSobre.getText().toString();
        String email = etEmail.getText().toString();
        String senha = etSenha.getText().toString();
        String dataNascimento = null;
        String sexo = null;
        int numero = 0;
        if (!etDataNascimento.getText().toString().isEmpty()) {
            dataNascimento = etDataNascimento.getText().toString();
        }
        if (sexoText != null) {
            sexo = sexoText;

        }
        if (!etNumero.getText().toString().isEmpty()) {
            numero = Integer.parseInt(etNumero.getText().toString());
        }

        // Verifica se os campos obrigatorios foram preenchidos
        if (!nome.isEmpty() && !sobrenome.isEmpty() && !email.isEmpty() && !senha.isEmpty()) {
            user.setNome(nome);
            user.setSobrenome(sobrenome);
            user.setEmail(email);
            user.setSenha(senha);
            user.setData_aniversario(dataNascimento);
            user.setSexo(sexo);
            user.setNumeroTelefone(numero);

            // Verifica se o email já está cadastrado
            try {
                cadastroViewModel.verificaExisteEmail(user.getEmail()).observe(this, aBoolean -> {
                    if (aBoolean) {
                        Toast.makeText(CadastroView.this, "Email já cadastrado no sistema!!!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Se a imagem foi selecionada, faz o upload para o Firebase Storage
                        cadastroViewModel.guardaImagemStorage(caminhoImagem).observe(this, s -> {
                            if (s != null) {
                                user.setUrl_foto_perfil(s);
                                cadastrarBD(user);
                            } else {
                                Toast.makeText(CadastroView.this, "Erro ao cadastra a foto de perfil, tente novamente.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Todos os campos obrigatorios precisam ser preenchidos.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para cadastrar o usuário no banco de dados
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

    // Método chamado quando o usuário clica para escolher a imagem
    public void escolherImagem(View v) {
        // Verifica se a permissão para ler o armazenamento externo já foi concedida
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Se a permissão ainda não foi concedida, solicita a permissão ao usuário
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
        } else {
            // Se a permissão foi concedida, cria uma Intent para selecionar a imagem da galeria
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        }
    }

    public void clipsClick(View v) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_login, null);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        MaterialButton btnAlterarImagem = view.findViewById(R.id.alterar_imagem),
                btnExcluirImagem = view.findViewById(R.id.excluir_imagem);

        btnAlterarImagem.setOnClickListener(v1 -> this.escolherImagem(getCurrentFocus()));

        btnExcluirImagem.setOnClickListener(v12 -> {
            imagemUsuario.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.avatar_1));
            caminhoImagem = null;
        });
    }

    // Método para cortar a imagem em formato redondo
    private void abrirRecorteImagem(Uri imagemUri) {
        CropImage.activity(imagemUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1) // Define a proporção de corte para um círculo
                .start(this);
    }

    // Método chamado após o usuário escolher a imagem na galeria
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uriImagemSelecionada = data.getData();
            if (uriImagemSelecionada != null) {
                // Captura o caminho da imagem selecionada e recorta em formato redono
                Uri imagemUri = data.getData();
                abrirRecorteImagem(imagemUri);
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri imagemRecortadaUri = result.getUri();
                caminhoImagem = imagemRecortadaUri;
                imagemUsuario.setImageURI(imagemRecortadaUri); // Define a imagem recortada no ImageView
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                // Lidar com erro durante o recorte
                Toast.makeText(this, "Erro ao recortar a imagem, tente novamente.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método chamado quando o sistema solicita permissões
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PICK_IMAGE_REQUEST) {
            // Verifica se a permissão foi concedida
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão concedida, chama o método para escolher a imagem
                escolherImagem(getCurrentFocus());
            } else {
                // Permissão negada, informa ao usuário sobre a necessidade da permissão
                Toast.makeText(this, "Permissão necessária para acessar o armazenamento.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
