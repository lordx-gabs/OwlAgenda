package com.example.owlagenda.ui.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.database.IniciarOuFecharDB;
import com.example.owlagenda.data.database.dao.UsuarioDao;
import com.example.owlagenda.data.models.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LoginViewModel extends ViewModel {
    private DatabaseReference databaseReference;
    private UsuarioDao usuarioDao;
    private MutableLiveData<Usuario> userLiveData;
    private MutableLiveData<Boolean> userValidoOuNao;
    private final ExecutorService executorService;
    private Usuario user;

    public LoginViewModel() {
        usuarioDao = IniciarOuFecharDB.appDatabase.userDao();
        databaseReference = FirebaseDatabase.getInstance().getReference("Usuario");
        userLiveData = new MutableLiveData<>();
        userValidoOuNao = new MutableLiveData<>();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<Boolean> buscaPorEmailSenha(String email, String senha) {
        Query query = databaseReference.orderByChild("email").equalTo(email);
        // Adicionar um listener para escutar por mudanças nos dados
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // O método onDataChange é chamado uma unica vez
                // dataSnapshot contém os dados do usuário
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Usuario usuario = ds.getValue(Usuario.class);
                        if (usuario.getSenha().equalsIgnoreCase(senha)) {
                            SincronizaBDViewModel.syncDBWithRoom(usuario);
                            userLiveData.setValue(usuario);
                            userValidoOuNao.setValue(true);
                        }
                    }

                } else {
                    executorService.execute(() -> user = usuarioDao.buscarPorEmailESenha(email, senha));

                    if (user != null) {
                        SincronizaBDViewModel.syncRoomWithDB(user);
                        userLiveData.postValue(user);
                        userValidoOuNao.setValue(true);
                    } else {
                        userValidoOuNao.setValue(false);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
        return userValidoOuNao;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Encerrar o ExecutorService quando a ViewModel for destruída
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        IniciarOuFecharDB.fecharDB();
    }

    public LiveData<Usuario> getLiveDataUser() {
        return userLiveData;
    }

}
