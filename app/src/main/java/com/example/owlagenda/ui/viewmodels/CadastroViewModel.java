package com.example.owlagenda.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.database.IniciarOuFecharDB;
import com.example.owlagenda.data.database.dao.UsuarioDao;
import com.example.owlagenda.data.models.Usuario;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CadastroViewModel extends ViewModel {
    private UsuarioDao usuarioDao;
    private final ExecutorService executorService;

    public CadastroViewModel() {
        usuarioDao = IniciarOuFecharDB.appDatabase.userDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<Boolean> cadastraBD(Usuario user) {
        MutableLiveData<Boolean> resultLiveData = new MutableLiveData<>();

        executorService.execute(() -> {
            try {
                usuarioDao.insert(user);
                SincronizaBDViewModel.syncRoomWithDB(user);
                resultLiveData.postValue(true); // Cadastro bem-sucedido
            } catch (Exception ex) {
                resultLiveData.postValue(false); // Cadastro falhou
            }
        });

        return resultLiveData;
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

}
