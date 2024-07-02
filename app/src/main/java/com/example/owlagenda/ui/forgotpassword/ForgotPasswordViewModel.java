package com.example.owlagenda.ui.forgotpassword;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.util.SharedPreferencesUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;

public class ForgotPasswordViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;
    private final DatabaseReference databaseReference;
    private final FirebaseAuth mAuth;

    public ForgotPasswordViewModel() {
        this.isLoading = new MutableLiveData<>();
        this.errorMessage = new MutableLiveData<>();
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
        this.mAuth = FirebaseAuth.getInstance();
    }

    public LiveData<Instant> fetchCurrentTimeFirebase() {
        MutableLiveData<Instant> currentTimeLiveData = new MutableLiveData<>();
        isLoading.postValue(true);
        DatabaseReference tempRef = databaseReference.child("tempTime");

        tempRef.setValue(ServerValue.TIMESTAMP).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                tempRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Long timestamp = snapshot.getValue(Long.class);
                        if (timestamp != null) {
                            Instant instantTimestampServer = Instant.ofEpochMilli(timestamp);
                            currentTimeLiveData.postValue(instantTimestampServer);
                        } else {
                            currentTimeLiveData.postValue(null);
                        }
                        isLoading.postValue(false);
                        tempRef.removeValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        isLoading.postValue(false);
                        if(error.getCode() == DatabaseError.NETWORK_ERROR) {
                            errorMessage.postValue("Erro de conexão com o servidor, tente novamente mais tarde");
                            return;
                        }
                        currentTimeLiveData.postValue(null);
                    }
                });
            } else {
                currentTimeLiveData.postValue(null);
                isLoading.postValue(false);
            }
        });

        return currentTimeLiveData;
    }

    public LiveData<Boolean> sendResetPasswordEmail(String email) {
        isLoading.postValue(true);
        MutableLiveData<Boolean> emailResetPasswordResult = new MutableLiveData<>();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                emailResetPasswordResult.postValue(true);
            } else {
                emailResetPasswordResult.postValue(false);
            }
            isLoading.postValue(false);
        });

        return emailResetPasswordResult;
    }

    public void saveTimestampUserShared(long timestamp) {
        SharedPreferencesUtil.saveLong(SharedPreferencesUtil.KEY_USER_TIMESTAMP, timestamp);
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

}
