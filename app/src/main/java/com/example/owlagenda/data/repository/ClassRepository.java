package com.example.owlagenda.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.owlagenda.data.models.SchoolClass;
import com.example.owlagenda.util.NotificationUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;

public class ClassRepository {
    private final CollectionReference classCollection;
    private final TaskRepository taskRepository;

    public ClassRepository() {
        classCollection = FirebaseFirestore.getInstance().collection("turma");
        taskRepository = new TaskRepository();
    }

    public void addClass(SchoolClass schoolClassData, OnCompleteListener<Void> completeListener) {
        classCollection.document(schoolClassData.getId()).set(schoolClassData)
                .addOnCompleteListener(completeListener);
    }

    public void deleteClass(String id, OnCompleteListener<Void> completeListener, Context context) {
        CollectionReference taskCollection = FirebaseFirestore.getInstance().collection("tarefa");
        taskCollection.whereEqualTo("schoolClass", FirebaseFirestore.getInstance().collection("turma").document(id))
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Crie uma lista para armazenar todas as TaskCompletionSource de exclusão
                        List<Task<Void>> deleteTasks = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Converter o documento para o modelo Task
                            com.example.owlagenda.data.models.Task taskModel = document.toObject(com.example.owlagenda.data.models.Task.class);
                            int notificationId = 0;
                            try {
                                notificationId = Integer.parseInt(taskModel.getId().replaceAll("[^0-9]", ""));
                            } catch (NumberFormatException ignored) {
                            }

                            if (NotificationUtil.scheduleNotificationApp.isAlarmSet(context.getApplicationContext(), taskModel.getTitle(), notificationId)) {
                                NotificationUtil.scheduleNotificationApp.cancelNotification(context.getApplicationContext(), taskModel.getTitle(), notificationId);
                                Log.d("testeChe", "chegou");
                            }

                            // Cria uma TaskCompletionSource para gerenciar a exclusão dos anexos
                            TaskCompletionSource<Void> deleteAttachmentTaskSource = new TaskCompletionSource<>();
                            deleteTasks.add(deleteAttachmentTaskSource.getTask());

                            // Chama o método de exclusão de anexos com o callback
                            taskRepository.deleteAttachmentsStorage(taskModel.getId(), taskModel.getTaskDocuments(), task1 -> {
                                if (task1.isSuccessful()) {
                                    // Após exclusão dos anexos, adiciona a exclusão do documento da tarefa
                                    document.getReference().delete().addOnCompleteListener(deleteTask -> {
                                        if (deleteTask.isSuccessful()) {
                                            // Completa a TaskCompletionSource indicando sucesso
                                            deleteAttachmentTaskSource.setResult(null);
                                        } else {
                                            // Completa a TaskCompletionSource com erro caso a exclusão da tarefa falhe
                                            deleteAttachmentTaskSource.setException(deleteTask.getException());
                                        }
                                    });
                                } else {
                                    // Completa a TaskCompletionSource com erro caso a exclusão dos anexos falhe
                                    deleteAttachmentTaskSource.setException(task1.getException());
                                }
                            });
                        }

                        // Usa Task.whenAll para aguardar todas as exclusões das tarefas e anexos serem concluídas
                        Task<Void> allTasks = Tasks.whenAll(deleteTasks);
                        allTasks.addOnCompleteListener(allTasksTask -> {
                            if (allTasksTask.isSuccessful()) {
                                // Após deletar todas as tarefas e anexos, delete a própria classe
                                classCollection.document(id).delete().addOnCompleteListener(completeListener);
                            } else {
                                // Lidar com erro nas exclusões das tarefas ou anexos
                                if (completeListener != null) {
                                    completeListener.onComplete(allTasksTask);
                                }
                            }
                        });
                    } else {
                        // Lidar com erro ao buscar as tarefas
                        if (completeListener != null) {
                            completeListener.onComplete(Tasks.forException(task.getException()));
                        }
                    }
                });
    }


    public void updateClass(SchoolClass schoolClassData, OnCompleteListener<Void> completeListener) {
        classCollection.document(schoolClassData.getId()).set(schoolClassData, SetOptions.merge())
                .addOnCompleteListener(completeListener);
    }

    public void getClassesByUserId(String userId, EventListener<QuerySnapshot> eventListener) {
        DocumentReference userRef = FirebaseFirestore.getInstance()
                .collection("usuario")
                .document(userId);

        classCollection.whereEqualTo("userId", userRef).addSnapshotListener(eventListener);
    }

    public void getClassesBySchoolId(DocumentReference schoolId, OnCompleteListener<QuerySnapshot> completeListener) {
        classCollection.whereEqualTo("schoolId", schoolId).get().addOnCompleteListener(completeListener);
    }

    public void getClassByName(String name, OnCompleteListener<QuerySnapshot> completeListener) {
        classCollection.whereEqualTo("classNameSearch", name).get().addOnCompleteListener(completeListener);
    }

    public void getClassByNameAndSchool(String name, DocumentReference schoolId, OnCompleteListener<QuerySnapshot> completeListener) {
        classCollection.whereEqualTo("classNameSearch", name)
                .whereEqualTo("schoolId", schoolId).get().addOnCompleteListener(completeListener);
    }

}
