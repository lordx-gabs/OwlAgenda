package com.example.owlagenda.ui.telaprincipal;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.repository.UserRepository;
import com.example.owlagenda.ui.selene.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TelaPrincipalViewModel extends ViewModel {
    FirebaseAuth mAuth;
    private MutableLiveData<User> user;
    private UserRepository userRepository;
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private ArrayList<Message> currentMessages = new ArrayList<>();

    public TelaPrincipalViewModel() {
        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();
    }

    public MutableLiveData<User> getUser(String uid) {
        user = new MutableLiveData<>();
        isLoading.postValue(true);
        userRepository.getUserById(uid, (value, error) -> {
            if (error != null) {
                if (error.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                } else {
                    user.postValue(null);
                }
                isLoading.postValue(false);
                // Tratar o erro
                Log.e("FirestoreError", "Erro ao obter histórico de mensagens", error);
                return;
            }
            if(value.exists()){
                user.postValue(value.toObject(User.class));
                isLoading.postValue(false);
                return;
            }
            user.postValue(null);
            isLoading.postValue(false);
        });

        return user;
    }

    private final MutableLiveData<ArrayList<Message>> messages = new MutableLiveData<>(new ArrayList<>());

    public LiveData<ArrayList<Message>> getMessages(String uid) {
        userRepository.getMessageHistory(uid, (value, error) -> {
            if (error != null) {
                if (error.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                } else {
                    messages.postValue(null);
                }
                isLoading.postValue(false);
                // Tratar o erro
                Log.e("FirestoreError", "Erro ao obter histórico de mensagens", error);
                return;
            }

            if (value != null && value.exists()) {
                Object historyMessage = value.get("historyMessage");

                if (historyMessage instanceof List<?> historyMessageList) {

                    for (Object item : historyMessageList) {
                        if (item instanceof Map<?, ?> messageMap) {
                            try {
                                Message message = new Message((String) messageMap.get("text"), (long) messageMap.get("messageType"));

                                Log.e("teste", message.getText());
                                currentMessages.add(message);
                            } catch (Exception e) {
                                Log.e("teste", "Erro ao converter mapa para Message", e);
                            }
                        }
                    }

                    messages.postValue(currentMessages);
                    isLoading.postValue(false);
                }
            }
        });

        return messages;
    }

    public void logout() {
        mAuth.signOut();
        Log.d("MyApp", "usuario deslogado");
    }
}
