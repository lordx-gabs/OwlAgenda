package com.example.owlagenda.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.Usuario;
import com.example.owlagenda.util.SincronizaDbeFirebase;

public class UsuarioViewModel extends ViewModel {
    private MutableLiveData<Usuario> usuarioLiveData;

    public UsuarioViewModel(LiveData<Usuario> userLiveData) {
        usuarioLiveData = new MutableLiveData<>();
        userLiveData.observeForever(usuario -> {
            if (usuario != null) {
                SincronizaDbeFirebase.syncDBWithRoom(usuario);
                SincronizaDbeFirebase.syncRoomWithDB(usuario);
                usuarioLiveData.setValue(usuario);
            }
        });
    }

    public LiveData<Usuario> getUsuarioLiveData() {
        return usuarioLiveData;
    }
}

