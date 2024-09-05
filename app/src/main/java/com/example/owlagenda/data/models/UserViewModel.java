package com.example.owlagenda.data.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.ui.selene.Message;

import java.util.ArrayList;

public class UserViewModel extends ViewModel {
    private final MutableLiveData<User> userMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Message>> messagesMutableLiveData = new MutableLiveData<>();

    public LiveData<User> getUser() {
        return userMutableLiveData;
    }

    public void setUser(User user) {
        this.userMutableLiveData.postValue(user);
    }

    public LiveData<ArrayList<Message>> getMessages() {
        return messagesMutableLiveData;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messagesMutableLiveData.postValue(messages);
    }
}
