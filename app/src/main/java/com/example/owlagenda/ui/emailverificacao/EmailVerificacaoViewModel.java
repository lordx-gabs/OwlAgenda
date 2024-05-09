package com.example.owlagenda.ui.emailverificacao;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

public class EmailVerificacaoViewModel extends ViewModel {

    private FirebaseAuth mAuth;

    public EmailVerificacaoViewModel(){
        mAuth = FirebaseAuth.getInstance();
    }

    public LiveData<Boolean> enviarEmailVerificacao() {
        MutableLiveData<Boolean> resultado = new MutableLiveData<>();

        mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                resultado.postValue(true);
            } else {
                resultado.postValue(false);
            }
        });

        return resultado;
    }

}
