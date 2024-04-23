package com.example.owlagenda.ui.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.database.IniciarOuFecharDB;
import com.example.owlagenda.data.database.dao.UsuarioDao;
import com.example.owlagenda.data.models.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CadastroViewModel extends ViewModel {
    private FirebaseAuth mAuth;
    private static DatabaseReference databaseReference;

    public CadastroViewModel() {
        mAuth = FirebaseAuth.getInstance();
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
                    throw new Exception("Erro ao executar tarefa, por favor tente novamente, mais tarde.");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return resultLiveData;
    }

}
