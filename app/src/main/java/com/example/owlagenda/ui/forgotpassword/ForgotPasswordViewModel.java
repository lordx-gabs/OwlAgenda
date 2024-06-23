package com.example.owlagenda.ui.forgotpassword;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;

public class ForgotPasswordViewModel extends ViewModel {
    private MutableLiveData<Boolean> isLoading;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    public static final String KEY_USER_TIMESTAMP = "timeCredentials";

    public ForgotPasswordViewModel() {

        this.isLoading = new MutableLiveData<>();
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
                        currentTimeLiveData.postValue(null);
                        isLoading.postValue(false);
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

    public void saveTimestampUserShared(SharedPreferences userTimestampCredentials, long timestamp) {
        SharedPreferences.Editor editor = userTimestampCredentials.edit();
        editor.putLong(KEY_USER_TIMESTAMP, timestamp);
        editor.apply();
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

}
