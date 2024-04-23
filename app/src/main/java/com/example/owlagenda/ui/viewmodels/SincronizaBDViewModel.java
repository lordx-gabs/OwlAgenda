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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SincronizaBDViewModel extends ViewModel {
    private static MutableLiveData<Boolean> estadoDaSincronizacao = new MutableLiveData<>();
    private static DatabaseReference databaseReference;
    private static UsuarioDao userDao;


    public static void sincronizaUserComRoom(Usuario user) {
        userDao = IniciarOuFecharDB.appDatabase.userDao();
        try {
            if (user != null) {
                inserirOuAtualizarUsuario(user);
                estadoDaSincronizacao.setValue(true);
            }
        } catch (Exception e) {
            estadoDaSincronizacao.setValue(false);

            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.schedule(() -> {
                // O código dentro deste bloco será executado após 5 segundos
                sincronizaUserComRoom(user);
            }, 5, TimeUnit.SECONDS);
            try {
                scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e1) {

            }
            scheduledExecutorService.shutdown();

        } finally {
            IniciarOuFecharDB.fecharDB();
        }

    }

    public static void sincronizaUserComFirebase(Usuario user) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Usuario");
        // Verifica se o usuário já existe no banco de dados
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user.setSenha(null);
                if (dataSnapshot.exists()) {
                    // O usuário já existe, então vamos atualizar os dados
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String userId = snapshot.getKey();
                        databaseReference.child(userId).setValue(user);
                    }
                } else {
                    databaseReference.child(user.getId()).setValue(user);
                }
                estadoDaSincronizacao.setValue(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                estadoDaSincronizacao.setValue(false);
                // Criar um ScheduledExecutorService
                ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                scheduledExecutorService.schedule(() -> {
                    // O código dentro deste bloco será executado após 5 segundos
                    sincronizaUserComFirebase(user);
                }, 5, TimeUnit.SECONDS);
                try {
                    scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {

                }
                scheduledExecutorService.shutdown();
            }
        });
    }

    private static void inserirOuAtualizarUsuario(Usuario usuario) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Usuario existingUser = userDao.usuarioPorId(usuario.getId());
            if (existingUser != null) {
                // O usuário já existe, então vamos atualizá-lo
                userDao.update(usuario);
            } else {
                // O usuário não existe, então vamos inseri-lo
                userDao.insert(usuario);
            }
        });
    }

    public LiveData<Boolean> getEstadoSincronizacao() {
        return estadoDaSincronizacao;
    }
}
