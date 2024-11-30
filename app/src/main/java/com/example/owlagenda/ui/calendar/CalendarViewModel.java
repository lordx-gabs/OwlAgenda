package com.example.owlagenda.ui.calendar;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.models.TaskAttachments;
import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.repository.TaskRepository;
import com.example.owlagenda.data.repository.UserRepository;
import com.example.owlagenda.util.NotificationUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

public class CalendarViewModel extends ViewModel {
    private final TaskRepository taskRepository;
    private MutableLiveData<ArrayList<Task>> tasks;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> isDeleted;
    private MutableLiveData<Boolean> isSuccessfully;
    private final UserRepository userRepository;

    public CalendarViewModel() {
        taskRepository = new TaskRepository();
        isLoading = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        userRepository = new UserRepository();
    }

    public LiveData<ArrayList<Task>> getTasks() {
        tasks = new MutableLiveData<>();

        taskRepository.getTasks(FirebaseAuth.getInstance().getCurrentUser().getUid(), (value, error) -> {
            isLoading.setValue(true);
            if (error != null) {
                if (error.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                    tasks.postValue(null);
                } else {
                    tasks.postValue(null);

                }
                isLoading.postValue(false);
                // Tratar o erro
                Log.e("FirestoreError", "Erro ao obter histórico de mensagens", error);
                return;
            }

            if (value != null) {
                ArrayList<Task> tasksObject = new ArrayList<>();

                for (DocumentSnapshot document : value.getDocuments()) {
                    Task task = document.toObject(Task.class);
                    if (task != null) {
                        tasksObject.add(task);
                        Log.d("Firestore", "Task: " + task.getTitle());
                    }
                }

                tasks.postValue(tasksObject);
            }
            isLoading.postValue(false);
        });

        return tasks;
    }

    public LiveData<Boolean> deleteTask(Task taskCalendar) {
        isDeleted = new MutableLiveData<>();
        isLoading.setValue(true);

        taskRepository.deleteTask(taskCalendar.getId(), task -> {
            if (task.isSuccessful()) {
                if (taskCalendar.getTaskDocuments() != null && !taskCalendar.getTaskDocuments().isEmpty()) {
                    taskRepository.deleteAttachmentsStorage(taskCalendar.getId(), taskCalendar.getTaskDocuments(), task1 -> {
                        if (task1.isSuccessful()) {
                            isDeleted.setValue(true);
                            isLoading.setValue(false);
                        } else {
                            errorMessage.postValue("Não foi possivel excluir os documentos dessa tarefa.");
                            isLoading.setValue(false);
                        }
                    });
                } else {
                    isDeleted.setValue(true);
                    isLoading.setValue(false);
                }
            } else {
                isDeleted.setValue(false);
                isLoading.setValue(false);
            }
        });

        return isDeleted;
    }

    public LiveData<Boolean> updateUserTaskCalendar(User user) {
        isSuccessfully = new MutableLiveData<>();
        isLoading.setValue(true);
        userRepository.updateUser(user, task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                isSuccessfully.setValue(true);
            } else {
                isSuccessfully.setValue(false);
                errorMessage.setValue("Erro ao atualizar usuário. Erro: " + task.getException().getMessage());
            }
        });

        return isSuccessfully;
    }

    public LiveData<Boolean> addTask(Task task) {
        isLoading.postValue(true);
        isSuccessfully = new MutableLiveData<>();

        ArrayList<String> downloadUrls = new ArrayList<>();

        task.setId(FirebaseFirestore.getInstance().collection("tarefa").document().getId());

        com.google.android.gms.tasks.Task<Void> lastTask = Tasks.forResult(null);
        if (task.getTaskDocuments() == null) {
            taskRepository.addTask(task, task2 -> {
                isLoading.postValue(false);
                if (task2.isSuccessful()) {
                    isSuccessfully.postValue(true);
                } else {
                    isSuccessfully.postValue(false);
                }
            });
        } else {
            lastTask = lastTask.continueWithTask(task1 -> taskRepository.saveAttachmentsStorage(task.getId(), task.getTaskDocuments()))
                    .addOnFailureListener(e -> {
                        if (e instanceof FirebaseNetworkException) {
                            errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                        } else {
                            isSuccessfully.postValue(false);
                        }
                        isLoading.postValue(false);
                    });

            lastTask.continueWithTask(task1 -> taskRepository.getAttachmentsUrls(task, downloadUrls))
                    .addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            for (int i = 0; i < downloadUrls.size(); i++) {
                                task.getTaskDocuments().get(i).setUrl(downloadUrls.get(i));
                            }

                            taskRepository.addTask(task, task2 -> {
                                isLoading.postValue(false);
                                if (task2.isSuccessful()) {
                                    isSuccessfully.postValue(true);
                                } else {
                                    handleErrorCreateTask(task.getId(), task.getTaskDocuments(), task2.getException());
                                }
                            });
                        } else {
                            handleErrorCreateTask(task.getId(), task.getTaskDocuments(), task1.getException());
                        }
                    });
        }

        return isSuccessfully;
    }

    private void handleErrorCreateTask(String taskId, ArrayList<TaskAttachments> documents, Exception exception) {
        if (exception instanceof FirebaseNetworkException) {
            errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
        }
        taskRepository.deleteAttachmentsStorage(taskId, documents, task -> {
            if (!task.isSuccessful()) {
                errorMessage.postValue("Erro ao interno, tente novamente. " + exception.getMessage());
            }
            isLoading.postValue(false);
        });
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}