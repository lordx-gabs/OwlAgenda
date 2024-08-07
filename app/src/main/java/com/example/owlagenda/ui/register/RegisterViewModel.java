package com.example.owlagenda.ui.register;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.BuildConfig;
import com.example.owlagenda.R;
import com.example.owlagenda.data.models.User;
import com.example.owlagenda.util.SyncData;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class RegisterViewModel extends ViewModel {
    private final FirebaseAuth firebaseAuth;
    private final StorageReference folderPathUser;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessageLiveData;

    public RegisterViewModel() {
        firebaseAuth = FirebaseAuth.getInstance();
        folderPathUser = FirebaseStorage.getInstance().getReference().child("usuarios");
        isLoading = new MutableLiveData<>();
        errorMessageLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<Boolean> registerUser(User user, Uri imagePath) {
        isLoading.postValue(true);
        MutableLiveData<Boolean> registrationResultLiveData = new MutableLiveData<>();

        firebaseAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        user.setId(firebaseUser.getUid());
                        user.setPassword(null);
                        uploadProfileImage(imagePath, user, firebaseUser, registrationResultLiveData);
                    } else {
                        handleRegistrationFailure(task.getException(), registrationResultLiveData);
                    }
                });

        return registrationResultLiveData;
    }

    private void uploadProfileImage(Uri imagePath, User user, FirebaseUser firebaseUser, MutableLiveData<Boolean> registrationResultLiveData) {
        int imageWidthMax = 200, imageHeightMax = 200;
        Bitmap bitmapImage = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(imagePath.getPath()), imageWidthMax, imageHeightMax, false);
        byte[] imageBytes = bitmapToByteArray(bitmapImage);

        StorageReference imageStorageReference = folderPathUser.child(firebaseUser.getUid()).child("foto_perfil.jpg");
        imageStorageReference.putBytes(imageBytes).addOnCompleteListener(task3 -> {
            if (task3.isSuccessful()) {
                imageStorageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    user.setUrlProfilePhoto(uri.toString());
                    sendVerificationEmail(user, firebaseUser, registrationResultLiveData);
                }).addOnFailureListener(e ->
                        handleImageUploadFailure(e, registrationResultLiveData, firebaseUser, imageStorageReference));
            } else {
                handleImageUploadFailure(task3.getException(), registrationResultLiveData, firebaseUser, imageStorageReference);
            }
        });
    }

    private void sendVerificationEmail(User user, FirebaseUser firebaseUser, MutableLiveData<Boolean> registrationResultLiveData) {
        firebaseUser.sendEmailVerification().addOnCompleteListener(task -> {
            SyncData.synchronizeUserWithFirebase(user);
            registrationResultLiveData.postValue(true);
        });
    }

    private void handleRegistrationFailure(Exception exception, MutableLiveData<Boolean> registrationResultLiveData) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            errorMessageLiveData.postValue("Já existe um usuário cadastrado com esse email. Por favor, tente outro email.");
        } else if (exception instanceof FirebaseNetworkException) {
            errorMessageLiveData.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
        } else {
            registrationResultLiveData.postValue(false);
        }
        isLoading.postValue(false);
    }

    private void handleImageUploadFailure(Exception exception, MutableLiveData<Boolean> registrationResultLiveData, FirebaseUser firebaseUser, StorageReference imageStorageReference) {
        registrationResultLiveData.postValue(false);
        errorMessageLiveData.postValue("Erro ao guardar foto de perfil. Erro: " + exception.getMessage());
        if (imageStorageReference != null) {
            imageStorageReference.delete();
        }
        deleteUser(firebaseUser, registrationResultLiveData);
        isLoading.postValue(false);
    }

    private void deleteUser(FirebaseUser firebaseUser, MutableLiveData<Boolean> registrationResultLiveData) {
        firebaseUser.delete().addOnCompleteListener(task -> {
            registrationResultLiveData.postValue(false);
            isLoading.postValue(false);
        });
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public Uri getImageProfileDefaultUri(Context context) {
        Bitmap bitmap = ((BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.wallpaper_your_name))
                .getBitmap();
        File outputFile;

        try {
            outputFile = File.createTempFile(
                    "avatar",
                    ".jpeg",
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    );
            OutputStream outputStream = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (IOException e) {
            return null;
        }

        return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", outputFile);
    }

    public Uri getImageFile(Context context) {
        File image;
        try {
            image = File.createTempFile(
                    "avatar",
                    ".jpeg",
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            );
        } catch (IOException e) {
            return null;
        }
        return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", image);
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

}
