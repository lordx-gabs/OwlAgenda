package com.example.owlagenda.ui.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.database.IniciarOuFecharDB;
import com.example.owlagenda.data.database.dao.UsuarioDao;
import com.example.owlagenda.data.models.Usuario;
import com.example.owlagenda.util.SincronizaDbeFirebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginViewModel extends ViewModel {
    private DatabaseReference databaseReference;
    private UsuarioDao usuarioDao;
    private MutableLiveData<Usuario> userLiveData;
    private final ExecutorService executorService;

    public LoginViewModel() {
        usuarioDao = IniciarOuFecharDB.appDatabase.userDao();
        databaseReference = FirebaseDatabase.getInstance().getReference("Usuario");
        userLiveData = new MutableLiveData<>();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<Usuario> buscaPorEmailSenha(String email, String senha) {
        Query query = databaseReference.orderByChild("email").equalTo(email);
        // Adicionar um listener para escutar por mudanças nos dados
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // O método onDataChange é chamado uma unica vez
                // dataSnapshot contém os dados do usuário "José"
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Usuario usuario = ds.getValue(Usuario.class);
                        if (usuario.getSenha().equalsIgnoreCase(senha)) {
                            SincronizaDbeFirebase.syncDBWithRoom(usuario);
                            userLiveData.setValue(usuario);
                        } else {
                           userLiveData.setValue(null);
                        }
                        return;
                    }
                } else {
                    executorService.execute(() -> {
                        Usuario user = usuarioDao.buscarPorEmailESenha(email, senha);
                        if (user != null) {
                            SincronizaDbeFirebase.syncRoomWithDB(user);
                            userLiveData.postValue(user);
                        } else {
                            userLiveData.postValue(null);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

        return userLiveData;
    }

}
