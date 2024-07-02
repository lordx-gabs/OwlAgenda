package com.example.owlagenda.ui.inicio;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

public class InicioViewModel extends ViewModel {
    private FirebaseAuth mAuth;
    private final MutableLiveData<String> mText;

    public InicioViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
        mAuth = FirebaseAuth.getInstance();
    }

    public void logout() {
        mAuth.signOut();
    }

    public LiveData<String> getText() {
        return mText;
    }
}