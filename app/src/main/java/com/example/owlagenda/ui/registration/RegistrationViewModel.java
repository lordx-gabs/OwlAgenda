package com.example.owlagenda.ui.registration;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.User;
import com.example.owlagenda.ui.viewmodels.SincronizaBDViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class RegistrationViewModel extends ViewModel {
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private StorageReference imageStorageReference;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessageLiveData;

    public RegistrationViewModel() {
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        isLoading = new MutableLiveData<>();
        errorMessageLiveData = new MutableLiveData<>();
    }

    public LiveData<Boolean> registrationUserInDatabase(User user) {
        isLoading.setValue(true);
        MutableLiveData<Boolean> registrationResultLiveData = new MutableLiveData<>();
        firebaseAuth.createUserWithEmailAndPassword(user.getEmail(), user.getSenha()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                user.setId(firebaseUser.getUid());
                user.setSenha(null);
                firebaseUser.sendEmailVerification().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        SincronizaBDViewModel.synchronizeUserWithFirebase(user);
                        registrationResultLiveData.postValue(true);
                    } else {
                        firebaseUser.delete();
                        registrationResultLiveData.postValue(false);
                    }
                    task1.addOnCompleteListener(task2 -> isLoading.setValue(false));
                });
            } else {
                Exception exception = task.getException();
                if (exception instanceof FirebaseAuthUserCollisionException) {
                    errorMessageLiveData.setValue("Este e-mail já está cadastrado. Por favor, tente outro e-mail.");

                } else {
                    errorMessageLiveData.setValue("Erro ao cadastrar usuário: " + exception.getMessage());
                }
            }
            task.addOnCompleteListener(task1 -> isLoading.setValue(false));
        });
        return registrationResultLiveData;
    }

    public LiveData<String> storeImageInStorage(Uri imagePath) {
        isLoading.setValue(true);
        MutableLiveData<String> photoUrlStorageLiveData = new MutableLiveData<>();

        if (imagePath != null) {
            imageStorageReference = storageReference.child("fotosdeperfil/" + imagePath.getLastPathSegment());
            imageStorageReference.putFile(imagePath).addOnSuccessListener(taskSnapshot ->
                            imageStorageReference.getDownloadUrl().addOnSuccessListener(uri -> photoUrlStorageLiveData.setValue(uri.toString()))
                                    .addOnFailureListener(e -> photoUrlStorageLiveData.setValue(null))
                                    .addOnCompleteListener(e -> isLoading.setValue(false)))
                    .addOnFailureListener(e -> photoUrlStorageLiveData.setValue(null));
        } else {
            imageStorageReference = storageReference.child("fotosdeperfil/Foto_Perfil_Padrao.jpeg");
            imageStorageReference.getDownloadUrl().addOnSuccessListener(uri ->
                            photoUrlStorageLiveData.setValue(uri.toString()))
                    .addOnFailureListener(e -> photoUrlStorageLiveData.setValue(null));
        }
        return photoUrlStorageLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

}
