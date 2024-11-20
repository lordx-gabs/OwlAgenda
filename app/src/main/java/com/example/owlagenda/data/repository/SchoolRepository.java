package com.example.owlagenda.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.owlagenda.data.models.School;
import com.example.owlagenda.data.models.SchoolClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;

public class SchoolRepository {
    private final CollectionReference schoolCollection;
    private final ClassRepository classRepository;

    public SchoolRepository() {
        schoolCollection = FirebaseFirestore.getInstance().collection("escola");
        classRepository = new ClassRepository();
    }

    public void addSchool(School school, OnCompleteListener<Void> eventListener) {
        schoolCollection.document(school.getId()).set(school)
                .addOnCompleteListener(eventListener);
    }

    public void deleteSchool(String id, OnCompleteListener<Void> completeListener, Context context) {
        CollectionReference classCollection = FirebaseFirestore.getInstance().collection("turma");

        classCollection.whereEqualTo("schoolId", schoolCollection.document(id)).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task<Void>> deleteClasses = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            SchoolClass classModel = document.toObject(SchoolClass.class);


                            TaskCompletionSource<Void> deleteClassTaskSource = new TaskCompletionSource<>();
                            deleteClasses.add(deleteClassTaskSource.getTask());

                            classRepository.deleteClass(classModel.getId(), task1 -> {
                                if (task1.isSuccessful()) {
                                    deleteClassTaskSource.setResult(null);
                                } else {
                                    deleteClassTaskSource.setException(task1.getException());
                                }
                            }, context);
                        }

                        Task<Void> allTasks = Tasks.whenAll(deleteClasses);
                        allTasks.addOnCompleteListener(allTasksTask -> {
                            if (allTasksTask.isSuccessful()) {
                                // Após deletar todas as tarefas e anexos, delete a própria classe
                                schoolCollection.document(id).delete().addOnCompleteListener(completeListener);
                            } else {
                                // Lidar com erro nas exclusões das tarefas ou anexos
                                completeListener.onComplete(allTasksTask);
                            }
                        });

                    } else {
                        completeListener.onComplete(Tasks.forException(task.getException()));
                    }
                });
    }

    public void updateSchool(School school, OnCompleteListener<Void> completeListener) {
        schoolCollection.document(String.valueOf(school.getId())).set(school, SetOptions.merge())
                .addOnCompleteListener(completeListener);
    }

    public void getSchoolsByUserId(String userId, EventListener<QuerySnapshot> eventListener) {
        DocumentReference userRef = FirebaseFirestore.getInstance()
                .collection("usuario")
                .document(userId);
        schoolCollection.whereEqualTo("userId", userRef).addSnapshotListener(eventListener);
    }

    public void getSchoolByName(String schoolName, OnCompleteListener<QuerySnapshot> eventListener) {
        DocumentReference userRef = FirebaseFirestore.getInstance()
                .collection("usuario")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        schoolCollection.whereEqualTo("userId", userRef).whereEqualTo("schoolNameSearch", schoolName).get().addOnCompleteListener(eventListener);
    }

    public void getSchoolById(String schoolId, EventListener<DocumentSnapshot> eventListener) {
    }

}
