package com.example.owlagenda.ui.homescreen;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.repository.UserRepository;
import com.facebook.AccessToken;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class HomeScreenViewModel extends ViewModel {
    private final FirebaseAuth firebaseAuth;
    private User user;
    private MutableLiveData<Boolean> isSuccessfully;
    private final UserRepository repository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public HomeScreenViewModel() {
        firebaseAuth = FirebaseAuth.getInstance();
        repository = new UserRepository();
    }

    public LiveData<Boolean> authUserWithGoogle(GoogleIdTokenCredential account) {
        isLoading.postValue(true);
        isSuccessfully = new MutableLiveData<>();
        repository.authUser(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                repository.getUserById(firebaseUser.getUid(), (value, error) -> {
                    if (error != null) {
                        firebaseUser.delete();
                        handleDatabaseError(error);
                        Log.e("FirestoreError", "Erro ao obter histórico de mensagens", error);
                        return;
                    }

                    if(!value.exists()) {
                        user = new User();
                        user.setEmail(firebaseUser.getEmail());
                        user.setId(firebaseUser.getUid());
                        user.setName(account.getGivenName());
                        if(account.getFamilyName() != null){
                            user.setSurname(account.getFamilyName());
                        }
                        user.setUrlProfilePhoto(firebaseUser.getPhotoUrl().toString());
                        repository.addUser(user, task1 -> {
                            if(task1.isSuccessful()) {
                                isSuccessfully.postValue(true);
                                isLoading.postValue(false);
                            } else {
                                isSuccessfully.postValue(false);
                                isLoading.postValue(false);
                                handleDatabaseError((FirebaseFirestoreException) task.getException());
                            }
                        });
                    } else {
                        isSuccessfully.postValue(true);
                        isLoading.postValue(false);
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
                repository.getUserById(firebaseUser.getUid(), (value, error) -> {
                    if (error != null) {
                        firebaseUser.delete();
                        handleDatabaseError(error);
                        Log.e("FirestoreError", "Erro ao obter histórico de mensagens", error);
                        return;
                    }

                    if(!value.exists()) {
                        user = new User();
                        user.setEmail(firebaseUser.getEmail());
                        user.setId(firebaseUser.getUid());
                        user.setName(firebaseUser.getDisplayName().split(" ")[0]);
                        if(firebaseUser.getDisplayName().split(" ").length > 1){
                            user.setSurname(firebaseUser.getDisplayName().split(" ")[1]);
                        }
                        user.setUrlProfilePhoto(firebaseUser.getPhotoUrl().toString());
                        repository.addUser(user, task1 -> {
                            if(task1.isSuccessful()) {
                                isSuccessfully.postValue(true);
                                isLoading.postValue(false);
                            } else {
                                isSuccessfully.postValue(false);
                                isLoading.postValue(false);
                                handleDatabaseError((FirebaseFirestoreException) task1.getException());
                            }
                        });
                    } else {
                        isSuccessfully.postValue(true);
                        isLoading.postValue(false);
                    }

                });

            } else {
                handleAuthException(task.getException());
            }
        }, token);

        return isSuccessfully;
    }

    private void handleAuthException(Exception exception) {
        if (exception instanceof FirebaseNetworkException) {
            errorMessage.setValue("Erro de conexão. Verifique sua conexão e tente novamente.");
            isLoading.setValue(false);
            return;
        }
        isLoading.setValue(false);
        isSuccessfully.setValue(false);
    }

    private void handleDatabaseError(FirebaseFirestoreException error) {
        if (error.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE) {
            errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
            isLoading.setValue(false);
            repository.deleteUserAuth(task -> {
                isSuccessfully.setValue(false);
                isLoading.setValue(false);
            });
            return;
        }
        repository.deleteUserAuth(task -> {
            isSuccessfully.setValue(false);
            isLoading.setValue(false);
        });
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

}
