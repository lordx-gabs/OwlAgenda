package com.example.owlagenda.ui.corubot;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.util.ChatBot;
import com.example.owlagenda.util.ModelChatBotSelene;
import com.example.owlagenda.util.ModelChatbotTriggers;

public class CorubotViewModel extends ViewModel {
    MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    ChatBot chatBotTrigger;
    ChatBot chatBotSelene;

    public CorubotViewModel() {
        chatBotSelene = new ChatBot(ModelChatBotSelene.createChatbotModelSelene().startChat());
        chatBotTrigger = new ChatBot(ModelChatbotTriggers.createChatbotModelTrigger().startChat());
    }

    public LiveData<String> sendMessage(String userMessage) {
        isLoading.postValue(true);
        MutableLiveData<String> messageChatBot = new MutableLiveData<>();

        chatBotTrigger.sendMessage(userMessage, new ChatBot.Callback<>() {
            @Override
            public void onSuccess(String result) {
                int action = result.charAt(0);
                String messageSelene;
                switch (action) {
                    case ModelChatbotTriggers.ADD_TASK:
                        // Lógica para adicionar uma tarefa
                        break;

                    case ModelChatbotTriggers.DELETE_TASK:
                        // Lógica para excluir uma tarefa
                        break;

                    case ModelChatbotTriggers.EDIT_TASK:
                        // Lógica para editar uma tarefa
                        break;

                    case ModelChatbotTriggers.VIEW_TASKS_MONTH:

                        break;

                    case ModelChatbotTriggers.VIEW_TASKS_DAY:

                        break;
                    default:
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
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
}
