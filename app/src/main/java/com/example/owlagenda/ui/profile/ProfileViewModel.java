package com.example.owlagenda.ui.profile;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.BuildConfig;
import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.repository.UserRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ProfileViewModel extends ViewModel {
    private MutableLiveData<Boolean> isSuccessful;
    private final UserRepository userRepository;
    private final MutableLiveData<String> errorMessageLiveData;
    public final MutableLiveData<Boolean> isLoading;

    public ProfileViewModel() {
        userRepository = new UserRepository();
        isSuccessful = new MutableLiveData<>();
        errorMessageLiveData = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
    }

    public LiveData<String> saveProfilePhoto(Bitmap imageProfileBitmap) {
        isLoading.setValue(true);
        MutableLiveData<String> urlProfilePhoto = new MutableLiveData<>();
        byte[] imageBytes = resizeImage(imageProfileBitmap);

        userRepository.uploadProfilePhoto(imageBytes, FirebaseAuth.getInstance().getCurrentUser().getUid(),
                task -> {
                    if (task.isSuccessful()) {
                        userRepository.getDownloadUrlProfileImage(FirebaseAuth.getInstance().getCurrentUser().getUid()
                                , task4 -> {
                                    if (task4.isSuccessful()) {
                                        urlProfilePhoto.setValue(task4.getResult().toString());
                                    } else {
                                        isLoading.setValue(false);
                                        handleImageUploadFailure(FirebaseAuth.getInstance().getCurrentUser(), task4.getException());
                                    }
                                });
                    } else {
                        urlProfilePhoto.setValue(null);
                        isLoading.setValue(false);
                    }
                });
        return urlProfilePhoto;
    }

    private void handleImageUploadFailure(FirebaseUser firebaseUser, Exception exception) {
        errorMessageLiveData.postValue("Erro ao guardar foto de perfil. Erro: " + exception.getMessage());
        userRepository.deleteImageProfile(firebaseUser.getUid());
    }

    public LiveData<Boolean> updateUser(User user) {
        isSuccessful = new MutableLiveData<>();
        isLoading.setValue(true);
        userRepository.updateUser(user, task -> {
            if (task.isSuccessful()) {
                isSuccessful.setValue(true);
                isLoading.setValue(false);
            } else {
                isSuccessful.setValue(false);
                isLoading.setValue(false);
                errorMessageLiveData.setValue("Erro ao atualizar usuário. Erro: " + task.getException().getMessage());
            }
        });

        return isSuccessful;
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

    private byte[] resizeImage(Bitmap imageProfile) {
        int imageWidthMax = 300, imageHeightMax = 300; 
        return bitmapToByteArray(
                Bitmap.createScaledBitmap(imageProfile, imageWidthMax, imageHeightMax, false)
        );
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }


    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }
}