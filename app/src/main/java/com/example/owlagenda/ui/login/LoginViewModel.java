package com.example.owlagenda.ui.login;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.User;
import com.example.owlagenda.util.SyncData;
import com.facebook.AccessToken;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class LoginViewModel extends ViewModel {
    private final FirebaseAuth firebaseAuth;
    private User user;
    private MutableLiveData<Boolean> isSuccessfully;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LoginViewModel() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public LiveData<Boolean> authUserWithEmailAndPassword(String email, String password) {
        isSuccessfully = new MutableLiveData<>();
        isLoading.postValue(true);
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                isSuccessfully.setValue(true);
                isLoading.postValue(false);
            } else {
                Exception exception = task.getException();
                if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                    errorMessage.postValue("Email ou senha incorretos.");
                } else if (exception instanceof FirebaseNetworkException) {
                    errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                } else {
                    isSuccessfully.postValue(false);
                }
                isLoading.postValue(false);
            }
        });
        return isSuccessfully;
    }

    public LiveData<Boolean> authUserWithGoogle(GoogleIdTokenCredential account) {
        isLoading.postValue(true);
        user = new User();
        isSuccessfully = new MutableLiveData<>();
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                SyncData.databaseUserReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
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
                        if (error.getCode() == DatabaseError.DISCONNECTED ||
                                error.getCode() == DatabaseError.NETWORK_ERROR) {
                            errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                        } else {
                            isSuccessfully.postValue(false);
                        }
                        isLoading.postValue(false);
                    }
                });
            } else {
                isSuccessfully.postValue(false);
                isLoading.postValue(false);
            }
        });
        return isSuccessfully;
    }

    public LiveData<Boolean> authUserWithFacebook(AccessToken token) {
        isLoading.postValue(true);
        isSuccessfully = new MutableLiveData<>();
        user = new User();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                SyncData.databaseUserReference.child(firebaseUser.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
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
                                if (error.getCode() == DatabaseError.DISCONNECTED ||
                                        error.getCode() == DatabaseError.NETWORK_ERROR) {
                                    errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                                    return;
                                }
                                isSuccessfully.postValue(false);
                                isLoading.postValue(false);
                            }
                        });
            } else {
                isSuccessfully.postValue(false);
                isLoading.postValue(false);
            }
        });
        return isSuccessfully;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }
}