package com.example.owlagenda.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.Usuario;
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
    private FirebaseAuth mAuth;
    private Usuario user;

    public LoginViewModel() {
        mAuth = FirebaseAuth.getInstance();
        user = new Usuario();
    }

    public LiveData<Boolean> autenticaUserEmailSenha(String email, String senha) throws FirebaseAuthException {
        MutableLiveData<Boolean> userValidoOuNao = new MutableLiveData<>();
        mAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userValidoOuNao.setValue(true);
            } else {
                userValidoOuNao.setValue(false);
            }
        });
        return userValidoOuNao;
    }

    public LiveData<Boolean> autenticaUserGoogle(String idToken, GoogleSignInAccount account) {
        MutableLiveData<Boolean> userValidoOuNao = new MutableLiveData<>();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Login bem-sucedido, faça algo com o usuário
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        user.setEmail(firebaseUser.getEmail());
                        user.setId(firebaseUser.getUid());
                        user.setNome(account.getGivenName());
                        user.setSobrenome(account.getFamilyName());
                        user.setUrl_foto_perfil(account.getPhotoUrl().toString());

                        SincronizaBDViewModel.sincronizaUserComFirebase(user);

                        userValidoOuNao.setValue(true);
                    } else {
                        // Tratar falha no login
                        userValidoOuNao.setValue(false);
                    }
                });
        return userValidoOuNao;
    }

    public LiveData<Boolean> autenticaUserTwitter(AuthResult authResult){
        MutableLiveData<Boolean> userGuardadoOuNao = new MutableLiveData<>();
        Usuario user = new Usuario();
        mAuth.signInWithCredential(authResult.getCredential())
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
                        SincronizaBDViewModel.sincronizaUserComFirebase(user);
                        userGuardadoOuNao.setValue(true);
                    } else {
                        userGuardadoOuNao.setValue(false);
                    }
                });
        return userGuardadoOuNao;
    }

}
