package com.example.owlagenda.data.repository;

import android.net.Uri;

import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.models.TaskAttachments;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDate;
import java.util.ArrayList;

public class TaskRepository {
    private final CollectionReference collectionReference;

    public TaskRepository() {
        collectionReference = FirebaseFirestore.getInstance().collection("tarefa");
    }

    public void getTasks(String id, EventListener<QuerySnapshot> eventListener) {
        collectionReference.whereEqualTo("userId", id).addSnapshotListener(eventListener);
    }

    public void addTask(Task task, OnCompleteListener<Void> completeListener) {
        collectionReference.document().set(task).addOnCompleteListener(completeListener);
    }

    public void deleteTask(String id, OnCompleteListener<Void> completeListener){
        collectionReference.document(id).delete().addOnCompleteListener(completeListener);
    }

    public void getTaskByDateToday(String id, EventListener<QuerySnapshot> eventListener) {
        //TODO:arrumar por data
        collectionReference.whereEqualTo("userId", id).whereEqualTo("date", LocalDate.now())
                .addSnapshotListener(eventListener);
    }

    public void getTaskByIsCompleted(ValueEventListener completeListener) {
        //TODO:pesquisar por isCompleted
    }

    public void saveAttachmentsStorage(ArrayList<TaskAttachments> documents, OnCompleteListener<UploadTask.TaskSnapshot> completeListener) {
        documents.forEach(document -> FirebaseStorage.getInstance().getReference().child("usuarios")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("document")
                .child(document.getName()).putFile(Uri.parse(document.getUri())).addOnCompleteListener(completeListener));
    }

    public void getAttachmentsUrl(String id, OnCompleteListener<Uri> completeListener) {
        FirebaseStorage.getInstance().getReference().child("usuarios")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("foto_perfil.jpg")
                .getDownloadUrl().addOnCompleteListener(completeListener);
    }

}
