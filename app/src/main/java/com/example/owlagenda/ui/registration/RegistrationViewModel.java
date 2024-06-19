package com.example.owlagenda.ui.registration;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.User;
import com.example.owlagenda.ui.viewmodels.SincronizaBDViewModel;
import com.google.common.primitives.Bytes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
        MutableLiveData<Boolean> registrationResultLiveData = new MutableLiveData<>();
        firebaseAuth.createUserWithEmailAndPassword(user.getEmail(), user.getSenha())
                .addOnCompleteListener(task -> {
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
                            // Update isLoading after all operations have completed
                            Log.e("MENSAGEM", "terminou");
                            isLoading.postValue(false);
                        });

                    } else {
                        registrationResultLiveData.postValue(false);
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseAuthUserCollisionException) {
                            errorMessageLiveData.postValue("Este e-mail já está cadastrado. Por favor, tente outro e-mail.");
                        } else {
                            errorMessageLiveData.postValue("Erro ao cadastrar usuário: " + exception.getMessage());
                        }

                        // Update isLoading in case of initial task failure
                        isLoading.postValue(false);
                    }
                });

        return registrationResultLiveData;
    }

    public LiveData<String> storeImageInStorage(Uri imagePath) {
        isLoading.postValue(true);
        MutableLiveData<String> photoUrlStorageLiveData = new MutableLiveData<>();
        if (imagePath != null) {
            Bitmap bitmapImage = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(imagePath.getPath()),
                    200,
                    200,
                    false);
            byte[] imageBytes = bitmapToByteArray(bitmapImage);

            imageStorageReference = storageReference.child("fotosdeperfil/" + imagePath.getLastPathSegment());
            imageStorageReference.putBytes(imageBytes).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    imageStorageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        photoUrlStorageLiveData.postValue(uri.toString());
                    }).addOnFailureListener(e -> {
                        photoUrlStorageLiveData.postValue(null);
                    });
                } else {
                    photoUrlStorageLiveData.postValue(null);
                }
            });
        } else {
            imageStorageReference = storageReference.child("fotosdeperfil/Foto_Perfil_Padrao.jpeg");
            imageStorageReference.getDownloadUrl().addOnSuccessListener(uri ->
                            photoUrlStorageLiveData.postValue(uri.toString()))
                    .addOnFailureListener(e -> photoUrlStorageLiveData.postValue(null));
        }
        return photoUrlStorageLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
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

}
