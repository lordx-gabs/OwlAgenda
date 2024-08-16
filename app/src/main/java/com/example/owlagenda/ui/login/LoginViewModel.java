package com.example.owlagenda.ui.login;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.repository.UserRepository;
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

    public LiveData<Boolean> authUserWithGoogle(GoogleIdTokenCredential account) {
        isLoading.postValue(true);
        isSuccessfully = new MutableLiveData<>();
        repository.authUser(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                repository.existsUserDatabase(firebaseUser.getUid(), new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            user = new User();
                            user.setEmail(firebaseUser.getEmail());
                            user.setId(firebaseUser.getUid());
                            user.setName(account.getGivenName());
                            user.setSurname(account.getFamilyName());
                            user.setUrlProfilePhoto(firebaseUser.getPhotoUrl().toString());
                            SyncData.synchronizeUserWithFirebase(user);
                        }
                        isSuccessfully.postValue(true);
                        isLoading.postValue(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        firebaseUser.delete();
                        handleDatabaseError(error);
                    }
                });

            } else {
                handleAuthException(task.getException());
            }
        }, account);

        return isSuccessfully;
    }

    public LiveData<Boolean> authUserWithFacebook(AccessToken token) {
        isLoading.postValue(true);
        isSuccessfully = new MutableLiveData<>();
        repository.authUser(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                repository.existsUserDatabase(firebaseUser.getUid(), new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            user = new User();
                            user.setEmail(firebaseUser.getEmail());
                            user.setId(firebaseUser.getUid());
                            user.setName(firebaseUser.getDisplayName().split(" ")[0]);
                            user.setSurname(firebaseUser.getDisplayName().split(" ")[1]);
                            user.setUrlProfilePhoto(firebaseUser.getPhotoUrl().toString());
                            SyncData.synchronizeUserWithFirebase(user);
                        }
                        isSuccessfully.postValue(true);
                        isLoading.postValue(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        firebaseUser.delete();
                        handleDatabaseError(error);
                    }
                });

            } else {
                handleAuthException(task.getException());
            }
        }, token);

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

    private void handleDatabaseError(DatabaseError error) {
        if (error.getCode() == DatabaseError.DISCONNECTED || error.getCode() == DatabaseError.NETWORK_ERROR) {
            errorMessage.setValue("Erro de conexão. Verifique sua conexão e tente novamente.");
            isLoading.setValue(false);
            return;
        }
        isSuccessfully.setValue(false);
        isLoading.setValue(false);
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }
}