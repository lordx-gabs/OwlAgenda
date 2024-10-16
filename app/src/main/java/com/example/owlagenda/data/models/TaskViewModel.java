package com.example.owlagenda.data.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class TaskViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<Task>> taskMutableLiveData = new MutableLiveData<>();

    public LiveData<ArrayList<Task>> getTask() {
        return taskMutableLiveData;
    }

    public void setTask(ArrayList<Task> tasks) {
        this.taskMutableLiveData.postValue(tasks);
    }
}
