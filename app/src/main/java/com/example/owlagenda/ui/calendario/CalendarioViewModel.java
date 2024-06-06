package com.example.owlagenda.ui.calendario;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CalendarioViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public CalendarioViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}