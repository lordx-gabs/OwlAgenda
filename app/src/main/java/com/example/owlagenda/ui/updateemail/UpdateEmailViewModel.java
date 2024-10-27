package com.example.owlagenda.ui.updateemail;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

public class UpdateEmailViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isLoading;
    private MutableLiveData<Boolean> isSuccessfully;
    private final FirebaseAuth firebaseAuth;
    private final MutableLiveData<String> isReauthenticationRequired;
    private final MutableLiveData<String> errorMessage;

    public UpdateEmailViewModel() {
        isLoading = new MutableLiveData<>();
        isSuccessfully = new MutableLiveData<>();
        firebaseAuth = FirebaseAuth.getInstance();
        isReauthenticationRequired = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
    }

    public LiveData<Boolean> updateEmail(String email) {
        isSuccessfully = new MutableLiveData<>();
        isLoading.postValue(true);
        firebaseAuth.getCurrentUser().verifyBeforeUpdateEmail(email).addOnCompleteListener(updateTask -> {
            if (updateTask.isSuccessful()) {
                isLoading.postValue(false);
                isSuccessfully.postValue(true);
            } else {
                isLoading.postValue(false);
                if (updateTask.getException() instanceof FirebaseAuthRecentLoginRequiredException ||
                        updateTask.getException() instanceof FirebaseAuthInvalidUserException) {
                    Log.d("teste", "Reautenticação necessária.");
                    for (UserInfo userInfo : firebaseAuth.getCurrentUser().getProviderData()) {
                        switch (userInfo.getProviderId()) {
                            case GoogleAuthProvider.PROVIDER_ID ->
                                // Usuário logado com Google
                                    isReauthenticationRequired.postValue(GoogleAuthProvider.PROVIDER_ID);
                            case FacebookAuthProvider.PROVIDER_ID ->
                                // Usuário logado com Facebook
                                    isReauthenticationRequired.postValue(FacebookAuthProvider.PROVIDER_ID);
                            case EmailAuthProvider.PROVIDER_ID ->
                                // Usuário logado com email e senha
                                    isReauthenticationRequired.postValue(EmailAuthProvider.PROVIDER_ID);
                        }
                    }
                    return;
                }

                isSuccessfully.postValue(false);
            }
        });
        return isSuccessfully;
    }

    public LiveData<Boolean> reauthenticateWithPassword(String email, String password) {
        isSuccessfully = new MutableLiveData<>();
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        // Reautentique o usuário antes de tentar a atualização
        firebaseAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                isLoading.postValue(false);
                isSuccessfully.postValue(true);
            } else {
                isLoading.postValue(false);
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    errorMessage.setValue("Email ou senha incorretos.");
                    return;
                } else if (task.getException()  instanceof FirebaseNetworkException) {
                    errorMessage.setValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                    return;
                }
                isSuccessfully.postValue(false);
            }
        });
        return isSuccessfully;
    }

    public LiveData<Boolean> reauthenticateWithFacebook(String accessToken) {
        isSuccessfully = new MutableLiveData<>();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            // Crie a credencial usando o token de acesso do Facebook
            AuthCredential credential = FacebookAuthProvider.getCredential(accessToken);

            // Reautentique o usuário
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("teste", "Reautenticação bem-sucedida com Facebook.");
                    isSuccessfully.postValue(true);
                } else {
                    Log.d("teste", "Falha na reautenticação com Facebook: " + task.getException().getMessage());
                    isSuccessfully.postValue(false);
                }
                isLoading.postValue(false);
            });
        } else {
            Log.d("teste", "Usuário não autenticado.");
            isSuccessfully.postValue(false);
        }

        return isSuccessfully;
    }

    public LiveData<Boolean> reauthenticateWithGoogle(String googleIdToken) {
        isSuccessfully = new MutableLiveData<>();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            // Crie a credencial usando o token do Google
            AuthCredential credential = GoogleAuthProvider.getCredential(googleIdToken, null);

            // Reautentique o usuário
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("teste", "Reautenticação bem-sucedida com Google.");
                    isSuccessfully.postValue(true);
                } else {
                    Log.d("teste", "Falha na reautenticação com Google: " + task.getException().getMessage());
                    isSuccessfully.postValue(false);
                }
                isLoading.postValue(false);
            });
        } else {
            Log.d("teste", "Usuário não autenticado.");
            isSuccessfully.postValue(false);
        }

        return isSuccessfully;
    }

    public LiveData<String> getIsReauthenticationRequired() {
        return isReauthenticationRequired;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}