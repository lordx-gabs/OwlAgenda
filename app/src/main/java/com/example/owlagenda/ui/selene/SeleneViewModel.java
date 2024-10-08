package com.example.owlagenda.ui.selene;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.repository.UserRepository;
import com.example.owlagenda.util.ChatBot;
import com.example.owlagenda.util.ModelChatBotSelene;
import com.google.ai.client.generativeai.type.Content;

import java.util.ArrayList;
import java.util.List;

public class SeleneViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isLoading;
    private final UserRepository userRepository;
    private ChatBot chatBotSelene;
    private final MutableLiveData<String> errorMessage;

    public SeleneViewModel() {
        errorMessage = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
        userRepository = new UserRepository();
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
                Log.e("SeleneViewModel", "Erro ao enviar mensagem: " + t.getMessage());
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

    public void setChatBotSelene(List<Content> historyMessage) {
        chatBotSelene = new ChatBot(ModelChatBotSelene.createChatbotModelSelene().startChat(historyMessage));
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
