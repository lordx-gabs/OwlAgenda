package com.example.owlagenda.ui.cadastro;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.Usuario;
import com.example.owlagenda.ui.viewmodels.SincronizaBDViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class CadastroViewModel extends ViewModel {
    private FirebaseAuth mAuth;
    private StorageReference reference;
    private FirebaseStorage storage;
    private StorageReference imagemReference;
    private MutableLiveData<Boolean> carregandoOuNao;
    private MutableLiveData<String> mensagemErroLiveData;

    public CadastroViewModel() {
        mAuth = FirebaseAuth.getInstance();
        // Captura o caminho da imagem selecionada e referencia esse caminho no Storage
        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();
        carregandoOuNao = new MutableLiveData<>();
        mensagemErroLiveData= new MutableLiveData<>();
        carregandoOuNao.setValue(false);
    }

    public LiveData<Boolean> cadastraBD(Usuario user) {
        carregandoOuNao.setValue(true);
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
                // Trate a falha de criação do usuário
                Exception exception = task.getException();
                if (exception instanceof FirebaseAuthUserCollisionException) {
                    // Coloque aqui o código para lidar com a colisão de usuários (e-mail já cadastrado)
                   mensagemErroLiveData.setValue("Este e-mail já está cadastrado. Por favor, tente outro e-mail.");

                }
            }
        });
        carregandoOuNao.setValue(false);
        return resultLiveData;
    }

    // Guarda a imagem no Storage e captura sua URL, caso dê sucesso,
    // retorna a url, caso dê erro, retorna null
    public LiveData<String> guardaImagemStorage(Uri caminhoImagem) {
        carregandoOuNao.setValue(true);
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
        carregandoOuNao.setValue(false);
        return caminhoFotoUrlStorage;
    }

    public LiveData<Boolean> getcarregandoOuNao() {
        return carregandoOuNao;
    }

    public LiveData<String> getmensagemErroLiveData() {
        return mensagemErroLiveData;
    }

}
