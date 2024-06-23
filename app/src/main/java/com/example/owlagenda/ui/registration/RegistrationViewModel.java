package com.example.owlagenda.ui.registration;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.User;
import com.example.owlagenda.ui.viewmodels.SincronizaBDViewModel;
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

public class RegistrationViewModel extends ViewModel {
    private final FirebaseAuth firebaseAuth;
    private final StorageReference storageReference;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessageLiveData;

    public RegistrationViewModel() {
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        isLoading = new MutableLiveData<>();
        errorMessageLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<Boolean> registerUser(User user, Uri imagePath) {
        isLoading.postValue(true);
        MutableLiveData<Boolean> registrationResultLiveData = new MutableLiveData<>();

        firebaseAuth.createUserWithEmailAndPassword(user.getEmail(), user.getSenha())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        user.setId(firebaseUser.getUid());
                        user.setSenha(null);

                        if (imagePath != null) {
                            uploadProfileImage(imagePath, user, firebaseUser, registrationResultLiveData);
                        } else {
                            setDefaultProfileImage(user, firebaseUser, registrationResultLiveData);
                        }
                    } else {
                        handleRegistrationFailure(task.getException(), registrationResultLiveData);
                    }
                });

        return registrationResultLiveData;
    }

    private void uploadProfileImage(Uri imagePath, User user, FirebaseUser firebaseUser, MutableLiveData<Boolean> registrationResultLiveData) {
        Bitmap bitmapImage = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(imagePath.getPath()), 200, 200, false);
        byte[] imageBytes = bitmapToByteArray(bitmapImage);

        StorageReference folderPathUser = storageReference.child("usuarios");
        StorageReference imageStorageReference = folderPathUser.child(firebaseUser.getUid()).child("foto_perfil.jpg");
        imageStorageReference.putBytes(imageBytes).addOnCompleteListener(task3 -> {
            if (task3.isSuccessful()) {
                imageStorageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    user.setUrl_foto_perfil(uri.toString());
                    sendVerificationEmail(user, firebaseUser, registrationResultLiveData);
                }).addOnFailureListener(e -> {
                    handleImageUploadFailure(e, registrationResultLiveData, firebaseUser, imageStorageReference);
                });
            } else {
                handleImageUploadFailure(task3.getException(), registrationResultLiveData, firebaseUser, imageStorageReference);
            }
        });
    }

    private void setDefaultProfileImage(User user, FirebaseUser firebaseUser, MutableLiveData<Boolean> registrationResultLiveData) {

        StorageReference imageStorageReference = storageReference.child(firebaseUser.getUid()).child("foto_perfil.jpg");
        imageStorageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            user.setUrl_foto_perfil(uri.toString());
            sendVerificationEmail(user, firebaseUser, registrationResultLiveData);
        }).addOnFailureListener(e -> {
            isLoading.postValue(false);
            deleteUser(firebaseUser, registrationResultLiveData);
            registrationResultLiveData.postValue(false);
        });
    }

    private void sendVerificationEmail(User user, FirebaseUser firebaseUser, MutableLiveData<Boolean> registrationResultLiveData) {
        firebaseUser.sendEmailVerification().addOnCompleteListener(task -> {
            SincronizaBDViewModel.synchronizeUserWithFirebase(user);
            registrationResultLiveData.postValue(true);
        });
    }

    private void handleRegistrationFailure(Exception exception, MutableLiveData<Boolean> registrationResultLiveData) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            errorMessageLiveData.postValue("Este e-mail já está cadastrado. Por favor, tente outro e-mail.");
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

    public File createImageFileAvatar(Context context) throws IOException {
        Bitmap bitmap = ((BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.avatar_1))
                .getBitmap();

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File outputFile;

        outputFile = File.createTempFile("avatar", ".png", storageDir);
        OutputStream outputStream = new FileOutputStream(outputFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        outputStream.close();

        return outputFile;
    }

    public long getFilePathToPhotoID(String imagePath, Context context) {
        long id = 0;
        ContentResolver cr = context.getContentResolver();

        Uri uri = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.Images.Media.DATA;
        String[] selectionArgs = {imagePath};
        String[] projection = {MediaStore.Images.Media._ID};
        String sortOrder = MediaStore.Images.Media.TITLE + " ASC";

        Cursor cursor = cr.query(uri, projection, selection + "=?", selectionArgs, sortOrder);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                id = Long.parseLong(cursor.getString(idIndex));
            }
        }

        cursor.close();
        return id;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

}
