package com.example.owlagenda.ui.taskdetails;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TaskDetailsViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isSuccessfully;
    private MutableLiveData<String> errorMessage;

    public TaskDetailsViewModel() {
        isSuccessfully = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
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
