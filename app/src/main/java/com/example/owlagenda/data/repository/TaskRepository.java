package com.example.owlagenda.data.repository;

import android.net.Uri;

import androidx.annotation.Nullable;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void updateTaskFields(DocumentReference task, String newName, String newType,
                                 DocumentReference school, DocumentReference newClass, String newDescription,
                                 String newDate, OnCompleteListener<Void> onCompleteListener) {
        Map<String, Object> updateTask = new HashMap<>();

        if (!newName.isEmpty()) {
            updateTask.put("title", newName);
            updateTask.put("titleSearch", newName.toUpperCase());
        }
        if (!newType.isEmpty()) {
            updateTask.put("tag", newType);
        }
        if (newClass != null) {
            updateTask.put("schoolClass", newClass);
        }
        if (school != null) {
            updateTask.put("school", school);
        }
        if (!newDescription.isEmpty()) {
            updateTask.put("description", newDescription);
        }
        if (!newDate.isEmpty()) {
            updateTask.put("date", newDate);
        }

        task.update(updateTask).addOnCompleteListener(onCompleteListener);
    }

    public void getTaskByTitle(String taskTitle, OnCompleteListener<QuerySnapshot> completeListener) {
        collectionReference.whereEqualTo("titleSearch", taskTitle).get().addOnCompleteListener(completeListener);
    }

    public void getTaskByTitleAndSchoolClass(String taskTitle, DocumentReference schoolClass, OnCompleteListener<QuerySnapshot> completeListener) {
        collectionReference.whereEqualTo("titleSearch", taskTitle)
                .whereEqualTo("schoolClass", schoolClass).get().addOnCompleteListener(completeListener);
    }

    public void deleteTask(String id, OnCompleteListener<Void> completeListener) {
        collectionReference.document(id).delete().addOnCompleteListener(completeListener);
    }

    public void getTaskByDateToday(String id, OnCompleteListener<QuerySnapshot> eventListener) {
        //TODO:arrumar por data
        collectionReference.whereEqualTo("userId", id).whereEqualTo("date",
                        DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.now()))
                .whereEqualTo("completed", false).get().addOnCompleteListener(eventListener);
    }

    public void getTaskByDateMonth(String id, OnCompleteListener<QuerySnapshot> eventListener) {
        //TODO:arrumar por data
        collectionReference.whereEqualTo("userId",
                FirebaseFirestore.getInstance().collection("usuario").document(id))
                .get().addOnCompleteListener(eventListener);
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
