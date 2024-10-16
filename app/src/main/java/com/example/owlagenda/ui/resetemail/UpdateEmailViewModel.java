package com.example.owlagenda.ui.resetemail;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class UpdateEmailViewModel extends ViewModel {
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<Boolean> isSuccessfully;
    private FirebaseAuth firebaseAuth;

    public UpdateEmailViewModel() {
       isLoading = new MutableLiveData<>();
       isSuccessfully = new MutableLiveData<>();
       firebaseAuth = FirebaseAuth.getInstance();
    }

    public LiveData<Boolean> updateEmail(String email) {
        firebaseAuth.getCurrentUser().verifyBeforeUpdateEmail(email).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                isLoading.postValue(false);
                isSuccessfully.postValue(true);
            } else {
                isLoading.postValue(false);
                isSuccessfully.postValue(false);
            }

        });
        return isSuccessfully;
    }
}
