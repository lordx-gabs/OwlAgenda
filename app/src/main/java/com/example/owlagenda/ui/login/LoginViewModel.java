package com.example.owlagenda.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.User;
import com.example.owlagenda.ui.viewmodels.SincronizaBDViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Map;

public class LoginViewModel extends ViewModel {
    private FirebaseAuth firebaseAuth;
    private User user;
    private MutableLiveData<Boolean> validUser;

    public LoginViewModel() {
        firebaseAuth = FirebaseAuth.getInstance();
        user = new User();
        validUser = new MutableLiveData<>();
    }

    public LiveData<Boolean> authUserWithEmailAndPassoword(String email, String password) throws FirebaseAuthException {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                validUser.setValue(true);
            } else {
                validUser.setValue(false);
            }
        });
        return validUser;
    }

    public LiveData<Boolean> authUserWithGoogle(String idToken, GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        user.setEmail(firebaseUser.getEmail());
                        user.setId(firebaseUser.getUid());
                        user.setNome(account.getGivenName());
                        user.setSobrenome(account.getFamilyName());
                        user.setUrl_foto_perfil(account.getPhotoUrl().toString());

                        SincronizaBDViewModel.synchronizeUserWithFirebase(user);
                        validUser.setValue(true);
                    } else {
                        validUser.setValue(false);
                    }
                });
        return validUser;
    }

    public LiveData<Boolean> authUserWithTwitter(AuthResult authResult){
        firebaseAuth.signInWithCredential(authResult.getCredential())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        user.setId(authResult.getUser().getUid());
                        Map<String, Object> profile = authResult.getAdditionalUserInfo().getProfile();
                        if (profile != null) {
                            user.setNome((String) profile.get("name"));
                            user.setSexo((String) profile.get("gender"));
                            user.setData_aniversario((String) profile.get("birthday"));
                            user.setUrl_foto_perfil((String) profile.get("profile_image_url"));
                        }
                        SincronizaBDViewModel.synchronizeUserWithFirebase(user);
                        validUser.setValue(true);
                    } else {
                        validUser.setValue(false);
                    }
                });
        return validUser;
    }

}
