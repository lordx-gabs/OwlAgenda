package com.example.owlagenda.ui.calendar;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.repository.TaskRepository;
import com.example.owlagenda.util.NotificationUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

public class CalendarViewModel extends ViewModel {
    private final TaskRepository taskRepository;
    private MutableLiveData<ArrayList<Task>> tasks;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> isDeleted;

    public CalendarViewModel() {
        taskRepository = new TaskRepository();
        isLoading = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
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

    public LiveData<Boolean> deleteTask(TaskCalendar taskCalendar, Context context) {
        isDeleted = new MutableLiveData<>();
        isLoading.setValue(true);

        taskRepository.deleteTask(taskCalendar.getId(), task -> {
            if(task.isSuccessful()) {
                int notificationId = 0;
                try {
                    notificationId = Integer.parseInt(taskCalendar.getId().replaceAll("[^0-9]", ""));
                } catch (NumberFormatException ignored) {

                }
                Log.d("teste", "" + notificationId);
                if (NotificationUtil.scheduleNotificationApp.isAlarmSet(context, taskCalendar.getNameTask(),
                        notificationId)) {
                    NotificationUtil.scheduleNotificationApp.cancelNotification(context, taskCalendar.getNameTask(),
                            notificationId);
                    Log.d("testeee", "chegouu");
                }
                isDeleted.setValue(true);
                isLoading.setValue(false);
            } else {
                isDeleted.setValue(false);
                isLoading.setValue(false);
            }
        });

        return isDeleted;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}