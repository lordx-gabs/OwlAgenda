package com.example.owlagenda.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.database.IniciarOuFecharDB;
import com.example.owlagenda.data.database.dao.UsuarioDao;
import com.example.owlagenda.data.models.Usuario;

public class UsuarioViewModel extends ViewModel {
    private MutableLiveData<Usuario> usuarioLiveData;
    private UsuarioDao userDao;
    private final Observer<Usuario> observer;

    public UsuarioViewModel() {
        usuarioLiveData = new MutableLiveData<>();
        userDao = IniciarOuFecharDB.appDatabase.userDao();

        observer = usuario -> {
            if (usuario != null) {
                SincronizaBDViewModel.sincronizaUserComRoom(usuario);
                SincronizaBDViewModel.sincronizaUserComFirebase(usuario);
                usuarioLiveData.setValue(usuario);
            }
        };
        usuarioLiveData.observeForever(observer);

    }

    public LiveData<Usuario> getUsuarioLiveData() {
        return usuarioLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        usuarioLiveData.removeObserver(observer);
    }

}
