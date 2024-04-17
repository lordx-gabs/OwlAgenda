package com.example.owlagenda.ui.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.owlagenda.data.database.IniciarOuFecharDB;
import com.example.owlagenda.data.database.dao.UsuarioDao;
import com.example.owlagenda.data.models.Usuario;
import com.example.owlagenda.util.SincronizaDbeFirebase;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginViewModel {
    private DatabaseReference databaseReference;
    private UsuarioDao usuarioDao;
    private Query query;
    private boolean resul;
    private LiveData<Usuario> user;

    public LoginViewModel() {
        usuarioDao = IniciarOuFecharDB.appDatabase.userDao();
        databaseReference = FirebaseDatabase.getInstance().getReference("usuarios");
    }

    public LiveData<Usuario> buscaPorEmailSenha(String email, String senha) {
        resul = false;
        query = databaseReference.orderByChild("email").equalTo(email).startAt(senha).endAt(senha);
        // Adicionar um listener para escutar por mudanças nos dados
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // O método onDataChange é chamado uma unica vez
                // dataSnapshot contém os dados do usuário "José"
                if(dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Usuario usuario = ds.getValue(Usuario.class);
                        resul = SincronizaDbeFirebase.syncDBWithRoom(usuario);
                    }
                    user = usuarioDao.buscarPorEmailESenha(email, senha);
                } else {
                    user = usuarioDao.buscarPorEmailESenha(email, senha);
                    if (user != null) {
                        resul = SincronizaDbeFirebase.syncRoomWithDB(user.getValue());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

        if (resul) {
            return user;
        } else {
            return user = null;
        }
    }

}
