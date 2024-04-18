package com.example.owlagenda.util;

import com.example.owlagenda.data.database.IniciarOuFecharDB;
import com.example.owlagenda.data.database.dao.UsuarioDao;
import com.example.owlagenda.data.models.Usuario;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SincronizaDbeFirebase {
    private static  DatabaseReference databaseReference;
    private static UsuarioDao userDao = IniciarOuFecharDB.appDatabase.userDao();

    public static boolean syncDBWithRoom(Usuario user) {
        boolean resul = false;
        if (user != null) {
            inserirOuAtualizarUsuario(user);
            resul = true;
        }

        return resul;
    }

    public static boolean syncRoomWithDB(Usuario user) {
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        String userId = databaseReference.push().getKey();
        return databaseReference.child(userId).setValue(user).isSuccessful();
    }

    private static void inserirOuAtualizarUsuario(Usuario usuario) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> userDao.update(usuario));
    }
}
