package com.example.owlagenda.data.repository;

import com.facebook.AccessToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UserRepository {
    private final FirebaseAuth firebaseAuth;
    public final DatabaseReference databaseUserReference;

    public UserRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        databaseUserReference = FirebaseDatabase.getInstance().getReference("Usuario");
    }

    public void authUser(OnCompleteListener<AuthResult> completeListener, String email, String password){
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(completeListener);
    }

    public void authUser(OnCompleteListener<AuthResult> completeListener, GoogleIdTokenCredential account){
        firebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(account.getIdToken(), null))
                .addOnCompleteListener(completeListener);
    }

    public void authUser(OnCompleteListener<AuthResult> completeListener, AccessToken token){
        firebaseAuth.signInWithCredential(FacebookAuthProvider.getCredential(token.getToken()))
                .addOnCompleteListener(completeListener);
    }

    public void existsUserDatabase(String id, ValueEventListener completeListener) {
        databaseUserReference.child(id).addListenerForSingleValueEvent(completeListener);
    }

    public void registerUser(String email, String password, OnCompleteListener<AuthResult> completeListener){
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(completeListener);
    }

    public void uploadProfilePhoto(byte[] imagePath, StorageReference imageStorageReference, OnCompleteListener<UploadTask.TaskSnapshot> completeListener){
        imageStorageReference.putBytes(imagePath).addOnCompleteListener(completeListener);
    }

    public void sendVerificationEmail(){
        firebaseAuth.getCurrentUser().sendEmailVerification();
    }

    public void deleteUser(OnCompleteListener<Void> completeListener) {
        firebaseAuth.getCurrentUser().delete().addOnCompleteListener(completeListener);
    }
}
