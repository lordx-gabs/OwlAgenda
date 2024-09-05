package com.example.owlagenda.ui.selene;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.repository.UserRepository;
import com.example.owlagenda.util.ChatBot;
import com.example.owlagenda.util.ModelChatBotSelene;

import java.util.ArrayList;

public class SeleneViewModel extends ViewModel {
    private MutableLiveData<Boolean> isLoading;
    private UserRepository userRepository;
    private ChatBot chatBotSelene;
    private MutableLiveData<String> errorMessage;

    public SeleneViewModel() {
        errorMessage = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
        userRepository = new UserRepository();
        chatBotSelene = new ChatBot(ModelChatBotSelene.createChatbotModelSelene().startChat());
    }

    public LiveData<String> sendMessage(String userMessage) {
        isLoading.postValue(true);
        MutableLiveData<String> messageChatBot = new MutableLiveData<>();
        chatBotSelene.sendMessage(userMessage, new ChatBot.Callback<>() {
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

    public void saveHistoryMessageUser(User user, ArrayList<Message> history) {
        userRepository.saveMessageHistory(user.getId(), history, task -> {
            if(!task.isSuccessful()) {
                errorMessage.postValue("Erro ao salvar o histórico de conversa.");
            }
        });
    }

    public void deleteHistoryMessageUser(String uid) {
        userRepository.deleteMessageHistory(uid, task -> {
            if(!task.isSuccessful()) {
                errorMessage.postValue("Erro ao excluir histórico da conversa.");
            }
        });
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
