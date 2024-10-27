package com.example.owlagenda.ui.filtertask;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.repository.TaskRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

public class FilterTaskViewModel extends ViewModel {
    private MutableLiveData<ArrayList<Task>> tasks;
    private TaskRepository taskRepository;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;

    public FilterTaskViewModel() {
        taskRepository = new TaskRepository();
        isLoading = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
    }

    public LiveData<ArrayList<Task>> getAllTask(String uid) {
        tasks = new MutableLiveData<>();
        taskRepository.getAllTaskById(uid, (value, error) -> {
            isLoading.postValue(true);
            if (error != null) {
                if (error.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                    tasks.postValue(null);
                } else {
                    tasks.postValue(null);
                }
                isLoading.postValue(false);
                return;
            }

            if (value != null) {
                ArrayList<Task> tasksObject = new ArrayList<>();

                for (DocumentSnapshot document : value.getDocuments()) {
                    Task task = document.toObject(Task.class);
                    if (task != null) {
                        tasksObject.add(task);
                        Log.d("Firestore9", "Task: " + task.getTitle());
                    }
                }
                isLoading.postValue(false);
                tasks.postValue(tasksObject);
            }
        });

        return tasks;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
