package com.example.owlagenda.ui.forgotpassword;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.util.SharedPreferencesUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.instacart.library.truetime.TrueTime;

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.Instant;

public class ForgotPasswordViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;
    private final FirebaseAuth mAuth;

    public ForgotPasswordViewModel() {
        this.isLoading = new MutableLiveData<>();
        this.errorMessage = new MutableLiveData<>();
        this.mAuth = FirebaseAuth.getInstance();
        new Thread(() -> {
            try {
                TrueTime.build().initialize();
            } catch (IOException e) {
                if (e instanceof UnknownHostException){
                    errorMessage.postValue("Erro de conexão com o servidor, verifique sua conexão");
                } else {
                    errorMessage.postValue("Ocorreu um erro, tente novamente mais tarde");
                }
            }
        }).start();
    }

    public LiveData<Instant> getCurrentTime() {
        MutableLiveData<Instant> currentTimeLiveData = new MutableLiveData<>();
        if (TrueTime.isInitialized()) {
            currentTimeLiveData.postValue(Instant.ofEpochMilli(TrueTime.now().getTime()));
        } else {
            errorMessage.postValue("Erro de conexão com o servidor, tente novamente mais tarde");
        }
        return currentTimeLiveData;
    }

    public LiveData<Boolean> sendResetPasswordEmail(String email) {
        isLoading.postValue(true);
        MutableLiveData<Boolean> emailResetPasswordResult = new MutableLiveData<>();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                emailResetPasswordResult.postValue(true);
            } else {
                emailResetPasswordResult.postValue(false);
            }
            isLoading.postValue(false);
        });

        return emailResetPasswordResult;
    }

    public void saveTimestampUserShared(long timestamp) {
        SharedPreferencesUtil.saveLong(SharedPreferencesUtil.KEY_USER_TIMESTAMP, timestamp);
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

}
