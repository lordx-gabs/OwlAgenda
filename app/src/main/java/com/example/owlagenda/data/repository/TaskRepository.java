package com.example.owlagenda.data.repository;

import android.net.Uri;

import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.models.TaskAttachments;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private final CollectionReference collectionReference;

    public TaskRepository() {
        collectionReference = FirebaseFirestore.getInstance().collection("tarefa");
    }

    public void getTasks(String id, EventListener<QuerySnapshot> eventListener) {
        collectionReference.whereEqualTo("userId",
                FirebaseFirestore.getInstance().collection("usuario")
                        .document(id)).addSnapshotListener(eventListener);
    }

    public void addTask(Task task, OnCompleteListener<Void> completeListener) {
        collectionReference.document(task.getId()).set(task).addOnCompleteListener(completeListener);
    }

    public void updateTask(Task task, OnCompleteListener<Void> completeListener) {
        collectionReference.document(task.getId()).set(task, SetOptions.merge())
                .addOnCompleteListener(completeListener);
    }

    public void deleteTask(String id, OnCompleteListener<Void> completeListener) {
        collectionReference.document(id).delete().addOnCompleteListener(completeListener);
    }

    public void getTaskByDateToday(String id, EventListener<QuerySnapshot> eventListener) {
        //TODO:arrumar por data
        collectionReference.whereEqualTo("userId", id).whereEqualTo("date", LocalDate.now())
                .addSnapshotListener(eventListener);
    }

    public void getTaskByNotCompleted(String id, EventListener<QuerySnapshot> eventListener) {
        //TODO:pesquisar por isCompleted
        collectionReference.whereEqualTo("userId",
                        FirebaseFirestore.getInstance().collection("usuario").document(id))
                .whereEqualTo("completed", false).addSnapshotListener(eventListener);
    }

    public com.google.android.gms.tasks.Task<Void> saveAttachmentsStorage(ArrayList<TaskAttachments> documents) {
        List<com.google.android.gms.tasks.Task<?>> uploadTasks = new ArrayList<>();
        for (TaskAttachments document : documents) {
            final Uri fileUri = Uri.parse(document.getUri());
            final StorageReference fileRef = FirebaseStorage.getInstance().getReference()
                    .child("usuarios")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("document")
                    .child(document.getName());

            uploadTasks.add(fileRef.putFile(fileUri).continueWith(task -> {
                if (task.isSuccessful()) {
                    return null; // Void, ok
                } else {
                    throw task.getException();
                }
            }));
        }
        return Tasks.whenAll(uploadTasks);
    }

    public void deleteAttachmentsStorage(ArrayList<TaskAttachments> documents, OnCompleteListener<Void> completeListener) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        documents.forEach(document -> {
            StorageReference fileRef = storageRef.child("usuarios")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("document")
                    .child(document.getName());

            fileRef.delete().addOnCompleteListener(completeListener);
        });
    }

    public com.google.android.gms.tasks.Task<Void> getAttachmentsUrls(Task task, ArrayList<String> downloadUrls) {
        List<com.google.android.gms.tasks.Task<?>> urlTasks = new ArrayList<>();
        for (TaskAttachments document : task.getTaskDocuments()) {
            final StorageReference fileRef = FirebaseStorage.getInstance().getReference()
                    .child("usuarios")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("document")
                    .child(document.getName());

            urlTasks.add(fileRef.getDownloadUrl().continueWith(urlTask -> {
                if (urlTask.isSuccessful()) {
                    downloadUrls.add(urlTask.getResult().toString());
                    return null;
                } else {
                    throw urlTask.getException();
                }
            }));
        }
        return Tasks.whenAll(urlTasks);
    }

}
