package com.example.owlagenda.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class LoginViewModel extends ViewModel {
    private FirebaseAuth mAuth;

    public LoginViewModel() {
        mAuth = FirebaseAuth.getInstance();
    }

    /*public LiveData<Boolean> autenticaUser(String email, String senha) {
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
    } */

    public LiveData<Boolean> autenticaUser(String email,String senha) throws FirebaseAuthException {
        MutableLiveData<Boolean> userValidoOuNao = new MutableLiveData<>();

        mAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userValidoOuNao.setValue(true);
            } else {
                userValidoOuNao.setValue(false);
            }
        });
        return userValidoOuNao;
    }


}
