package com.example.owlagenda.ui.inicio;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.repository.TaskRepository;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

public class InicioViewModel extends ViewModel {
    private MutableLiveData<Boolean> isSuccessfully;
    private final MutableLiveData<String> errorMessage;
    private final TaskRepository taskRepository;
    private MutableLiveData<ArrayList<Task>> tasks;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public InicioViewModel() {
        taskRepository = new TaskRepository();
        errorMessage = new MutableLiveData<>();
    }

    public LiveData<Boolean> setTaskIsCompleted(Task task) {
        isSuccessfully = new MutableLiveData<>();
        taskRepository.updateTask(task, task1 -> {
            if(task1.isSuccessful()) {
                isSuccessfully.postValue(true);
            } else {
                if (task1.getException() instanceof FirebaseNetworkException) {
                    errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                    return;
                }
                isSuccessfully.postValue(false);
            }
        });
        return isSuccessfully;
    }

    public LiveData<ArrayList<Task>> getTasksByNotCompleted(String uid) {
        tasks = new MutableLiveData<>();
        taskRepository.getTaskByNotCompleted(uid, (value, error) -> {
            isLoading.postValue(true);
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
                        Log.d("Firestore6", "Task: " + task.getTitle());
                    }
                }
                isLoading.postValue(false);
                tasks.postValue(tasksObject);
            }
        });

        return tasks;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

}