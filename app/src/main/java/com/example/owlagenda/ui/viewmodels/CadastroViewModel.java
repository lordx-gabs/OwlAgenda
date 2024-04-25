package com.example.owlagenda.ui.viewmodels;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class CadastroViewModel extends ViewModel {
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference reference;
    // Inicializa o Firebase Storage
    private FirebaseStorage storage;
    private StorageReference imagemReference;

    public CadastroViewModel() {
        mAuth = FirebaseAuth.getInstance();
        // Captura o caminho da imagem selecionada e referencia esse caminho no Storage
        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();
    }

    public LiveData<Boolean> cadastraBD(Usuario user) {

        MutableLiveData<Boolean> resultLiveData = new MutableLiveData<>();

        mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getSenha()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                user.setId(firebaseUser.getUid());
                user.setSenha(null);
                firebaseUser.sendEmailVerification().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        SincronizaBDViewModel.sincronizaUserComFirebase(user);
                        resultLiveData.postValue(true); // Cadastro bem-sucedido e email de verificação enviado
                    } else {
                        firebaseUser.delete();
                        resultLiveData.postValue(false); // Cadastro falhou
                    }
                });
            } else {
                resultLiveData.postValue(false); // Cadastro falhou
            }
        });

        return resultLiveData;
    }

    public LiveData<Boolean> verificaExisteEmail(String email) throws Exception {
        MutableLiveData<Boolean> resultLiveData = new MutableLiveData<>();
        // referencia o usuario no banco de dados
        databaseReference = FirebaseDatabase.getInstance().getReference("Usuario");
        // inicializa a query para pesquisar no email no banco de dados
        Query pesquisaEmail = databaseReference.orderByChild("email").equalTo(email);

        pesquisaEmail.addListenerForSingleValueEvent(new ValueEventListener() {
            // pesquisa realizada com sucesso
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // O e-mail já está cadastrado, pois a pesquisa retornou algo
                    resultLiveData.setValue(true);

                } else {
                    // O e-mail não está cadastrado, pois a pesquisa não retornou nada
                    resultLiveData.setValue(false);

                }
            }

            // pesquisa falhou
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                try {
                    new Exception("Erro ao executar tarefa, por favor tente novamente, mais tarde.");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return resultLiveData;
    }

    // Guarda a imagem no Storage e captura sua URL, caso dê sucesso,
    // retorna a url, caso dê erro, retorna null
    public LiveData<String> guardaImagemStorage(Uri caminhoImagem) {
        MutableLiveData<String> caminhoFotoUrlStorage = new MutableLiveData<>();

        if (caminhoImagem != null) {
            imagemReference = reference.child("fotosdeperfil/" + caminhoImagem.getLastPathSegment());
            imagemReference.putFile(caminhoImagem).addOnSuccessListener(taskSnapshot -> {
                // Captura a URL do arquivo no Storage
                imagemReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Falha na captura da url do arquivo no Storage
                    caminhoFotoUrlStorage.setValue(uri.toString());
                }).addOnFailureListener(e -> caminhoFotoUrlStorage.setValue(null));
                // Falha ao enviar a foto de perfil.
            }).addOnFailureListener(e -> caminhoFotoUrlStorage.setValue(null));
        } else {
            // Caso o usuario não passar uma imagem, pegamos a url da imagem padrão do app
            imagemReference = reference.child("fotosdeperfil/Foto_Perfil_Padrao.jpeg");
            imagemReference.getDownloadUrl().addOnSuccessListener(uri -> {
                caminhoFotoUrlStorage.setValue(uri.toString());
            }).addOnFailureListener(e -> {
                caminhoFotoUrlStorage.setValue(null);
            });
        }
        return caminhoFotoUrlStorage;
    }

}
