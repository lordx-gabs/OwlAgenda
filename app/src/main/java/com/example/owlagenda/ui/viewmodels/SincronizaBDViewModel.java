package com.example.owlagenda.ui.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.database.IniciarOuFecharDB;
import com.example.owlagenda.data.database.dao.UsuarioDao;
import com.example.owlagenda.data.models.User;
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
    private static final MutableLiveData<Boolean> syncStatus = new MutableLiveData<>();
    private static final MutableLiveData<Boolean> isLoadingSync = new MutableLiveData<>();
    private static DatabaseReference databaseReference;
    private static UsuarioDao userDao;
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public static void synchronizeUserWithRoom(User user) {
        isLoadingSync.setValue(true);
        userDao = IniciarOuFecharDB.appDatabase.userDao();
        try {
            if (user != null) {
                insertOrUpdateUser(user);
                syncStatus.setValue(true);
            }
        } catch (Exception e) {
            syncStatus.setValue(false);
            scheduledExecutorService.schedule(() -> {
                // O código dentro deste bloco será executado após 5 segundos
                synchronizeUserWithRoom(user);
            }, 5, TimeUnit.SECONDS);
            scheduledExecutorService.shutdown();

            try {
                if (!scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduledExecutorService.shutdownNow();
                    scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e1) {
                scheduledExecutorService.shutdownNow();
                Thread.currentThread().interrupt();
            }

        } finally {
            IniciarOuFecharDB.fecharDB();
            isLoadingSync.setValue(false);
        }

    }

    public static void synchronizeUserWithFirebase(User user) {
        isLoadingSync.setValue(true);
        databaseReference = FirebaseDatabase.getInstance().getReference("Usuario");
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user.setSenha(null);
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        databaseReference.child(snapshot.getKey()).setValue(user);
                    }
                } else {
                    databaseReference.child(user.getId()).setValue(user);
                }
                syncStatus.setValue(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                syncStatus.setValue(false);
                scheduledExecutorService.schedule(() -> {
                    // O código dentro deste bloco será executado após 5 segundos
                    synchronizeUserWithFirebase(user);
                }, 5, TimeUnit.SECONDS);
                scheduledExecutorService.shutdown();

                try {
                    if (!scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        scheduledExecutorService.shutdownNow();
                        scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS);
                    }
                } catch (InterruptedException e1) {
                    scheduledExecutorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }

            }
        });

    }

    private static void insertOrUpdateUser(User user) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            User existingUser = userDao.usuarioPorId(user.getId());
            if (existingUser != null) {
                userDao.update(user);
            } else {
                userDao.insert(user);
            }
        });
    }

    public LiveData<Boolean> getSyncStatus() {
        return syncStatus;
    }
}
