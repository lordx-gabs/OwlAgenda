package com.example.owlagenda.ui.corubot;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.util.ChatBot;

public class CorubotViewModel extends ViewModel {
    MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    ChatBot chatBot;

    public CorubotViewModel() {
        chatBot = new ChatBot();
    }

    public LiveData<String> sendMessage(String userMessage) {
        isLoading.postValue(true);
        MutableLiveData<String> messageChatBot = new MutableLiveData<>();
        chatBot.sendMessage(userMessage, new ChatBot.Callback<>() {
            @Override
            public void onSuccess(String result) {
                messageChatBot.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(Throwable t) {
                messageChatBot.postValue(null);
                isLoading.postValue(false);
            }
        });


        return messageChatBot;
    }
}
