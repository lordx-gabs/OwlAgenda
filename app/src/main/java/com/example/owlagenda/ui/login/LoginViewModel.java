package com.example.owlagenda.ui.login;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.repository.UserRepository;
import com.example.owlagenda.ui.selene.Message;
import com.example.owlagenda.util.SyncData;
import com.facebook.AccessToken;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LoginViewModel extends ViewModel {
    private final FirebaseAuth firebaseAuth;
    private User user;
    private MutableLiveData<Boolean> isSuccessfully;
    private final UserRepository repository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LoginViewModel() {
        firebaseAuth = FirebaseAuth.getInstance();
        repository = new UserRepository();
    }

    public LiveData<Boolean> authUserWithEmailAndPassword(String email, String password) {
        isSuccessfully = new MutableLiveData<>();
        isLoading.postValue(true);
        repository.authUser(task -> {
            if (task.isSuccessful()) {
                isSuccessfully.setValue(true);
                isLoading.postValue(false);
            } else {
                handleAuthException(task.getException());
            }
        }, email, password);

        return isSuccessfully;
    }

    private void handleAuthException(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage.setValue("Email ou senha incorretos.");
        } else if (exception instanceof FirebaseNetworkException) {
            errorMessage.setValue("Erro de conexão. Verifique sua conexão e tente novamente.");
        }
        isLoading.setValue(false);
        isSuccessfully.setValue(false);
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }
}