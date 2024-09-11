package com.example.owlagenda.data.repository;

import android.net.Uri;

import com.example.owlagenda.data.models.User;
import com.example.owlagenda.ui.selene.Message;
import com.facebook.AccessToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;

public class UserRepository {
    private final FirebaseAuth firebaseAuth;
    private final CollectionReference collectionReference;

    public UserRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.collectionReference = FirebaseFirestore.getInstance().collection("usuario");
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

    public void getUserById(String id, EventListener<DocumentSnapshot> completeListener) {
        collectionReference.document(id).addSnapshotListener(completeListener);
    }

    public void registerUser(String email, String password, OnCompleteListener<AuthResult> completeListener){
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(completeListener);
    }

    public void uploadProfilePhoto(byte[] imagePath, String uid, OnCompleteListener<UploadTask.TaskSnapshot> completeListener){
        FirebaseStorage.getInstance().getReference().child("usuarios").child(uid)
                .child("foto_perfil.jpg").putBytes(imagePath).addOnCompleteListener(completeListener);
    }

    public void getDownloadUrlProfileImage(String uid, OnCompleteListener<Uri> completeListener) {
        FirebaseStorage.getInstance().getReference().child("usuarios").child(uid)
                .child("foto_perfil.jpg").getDownloadUrl().addOnCompleteListener(completeListener);
    }

    public void addUser(User user, OnCompleteListener<Void> completeListener) {
        collectionReference.document(user.getId()).set(user).addOnCompleteListener(completeListener);
    }

    public void sendVerificationEmail(){
        firebaseAuth.getCurrentUser().sendEmailVerification();
    }

    public void deleteUserAuth(OnCompleteListener<Void> completeListener) {
        firebaseAuth.getCurrentUser().delete().addOnCompleteListener(completeListener);
    }

    public void saveMessageHistory(String id, ArrayList<Message> historyMessage, OnCompleteListener<Void> completeListener) {
        collectionReference.document(id).update(Collections.singletonMap("historyMessage", historyMessage))
                .addOnCompleteListener(completeListener);
    }

    public void deleteMessageHistory(String id, OnCompleteListener<Void> completeListener) {
        collectionReference.document(id).update("historyMessage", FieldValue.delete())
                .addOnCompleteListener(completeListener);
    }

    public void getMessageHistory(String id, EventListener<DocumentSnapshot> childEventListener) {
        collectionReference.document(id).addSnapshotListener(childEventListener);
    }

    public void deleteImageProfile(String uid) {
        FirebaseStorage.getInstance().getReference().child("usuarios").child(uid).delete();
    }
}
