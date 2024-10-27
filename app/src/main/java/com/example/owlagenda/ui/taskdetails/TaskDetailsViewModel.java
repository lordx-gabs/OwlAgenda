package com.example.owlagenda.ui.taskdetails;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.models.TaskAttachments;
import com.example.owlagenda.data.repository.TaskRepository;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class TaskDetailsViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isSuccessfully;
    private MutableLiveData<Task> taskMutableLiveData;
    private MutableLiveData<String> errorMessage;
    private final TaskRepository taskRepository = new TaskRepository();

    public TaskDetailsViewModel() {
        isSuccessfully = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
    }

    public LiveData<Task> getTaskById(String id) {
        taskMutableLiveData = new MutableLiveData<>();
        isLoading.setValue(true);
        taskRepository.getTaskById(id, task -> {
            if(task.isSuccessful()) {
                if(task.getResult().exists()) {
                    taskMutableLiveData.postValue(task.getResult().toObject(Task.class));
                } else {
                    taskMutableLiveData.postValue(null);
                }
            } else {
                isLoading.setValue(false);
                errorMessage.setValue("Erro ao buscar tarefa: " + task.getException().getMessage());
            }
        });

        return taskMutableLiveData;
    }

    public LiveData<Boolean> deleteTask(Task taskCalendar) {
        isSuccessfully = new MutableLiveData<>();
        isLoading.setValue(true);

        taskRepository.deleteTask(taskCalendar.getId(), task -> {
            if(task.isSuccessful()) {
                if(taskCalendar.getTaskDocuments() != null && !taskCalendar.getTaskDocuments().isEmpty()) {
                    taskRepository.deleteAttachmentsStorage(taskCalendar.getTaskDocuments(), task1 -> {
                        if(task1.isSuccessful()) {
                            isSuccessfully.setValue(true);
                            isLoading.setValue(false);
                        } else {
                            errorMessage.postValue("Não foi possivel excluir os documentos dessa tarefa.");
                            isLoading.setValue(false);
                        }
                    });
                } else {
                    isSuccessfully.setValue(true);
                    isLoading.setValue(false);
                }
            } else {
                isSuccessfully.setValue(false);
                isLoading.setValue(false);
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

        lastTask = lastTask.continueWithTask(task1 -> taskRepository.saveAttachmentsStorage(task.getTaskDocuments()))
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
                            if (task2.isSuccessful()) {
                                isSuccessfully.postValue(true);
                            } else {
                                handleErrorCreateTask(task.getTaskDocuments(), task2.getException());
                            }
                        });
                    } else {
                        handleErrorCreateTask(task.getTaskDocuments(), task1.getException());
                    }
                });

        return isSuccessfully;
    }

    private void handleErrorCreateTask(ArrayList<TaskAttachments> documents, Exception exception) {
        if (exception instanceof FirebaseNetworkException) {
            errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
        }
        taskRepository.deleteAttachmentsStorage(documents, task -> {
            if (!task.isSuccessful()) {
                errorMessage.postValue("Erro ao interno, tente novamente. " + exception.getMessage());
            }
            isLoading.postValue(false);
        });
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<Boolean> getIsSuccessfully() {
        return isSuccessfully;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
