package com.example.owlagenda.ui.telaprincipal;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

public class TelaPrincipalViewModel extends ViewModel {
    FirebaseAuth mAuth;

    public TelaPrincipalViewModel() {
        mAuth = FirebaseAuth.getInstance();
    }

    public void logout() {
        mAuth.signOut();
        Log.d("MyApp", "usuario deslogado");
    }
}
