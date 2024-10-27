package com.example.owlagenda.ui.telaprincipal;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.repository.TaskRepository;
import com.example.owlagenda.data.repository.UserRepository;
import com.example.owlagenda.ui.selene.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TelaPrincipalViewModel extends ViewModel {
    FirebaseAuth mAuth;
    private MutableLiveData<User> user;
    private UserRepository userRepository;
    private TaskRepository taskRepository;
    private MutableLiveData<ArrayList<Task>> tasks;
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    ArrayList<Message> currentMessages;
    MutableLiveData<ArrayList<Message>> messages;

    public TelaPrincipalViewModel() {
        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();
        taskRepository = new TaskRepository();
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
            if (value.exists()) {
                user.postValue(value.toObject(User.class));
                isLoading.postValue(false);
                return;
            }
            user.postValue(null);
            isLoading.postValue(false);
        });

        return user;
    }

    public LiveData<ArrayList<Task>> getTasks(String uid) {
        tasks = new MutableLiveData<>();
        taskRepository.getTasks(uid, (value, error) -> {
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

            if (value != null && !value.isEmpty()) {
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
        });

        return tasks;
    }

    public LiveData<Boolean> updateEmail(String id, String email) {
        MutableLiveData<Boolean> success = new MutableLiveData<>();
        isLoading.postValue(true);
        userRepository.updateEmail(id, email, task -> {
            if(task.isSuccessful()) {
                success.postValue(true);
                isLoading.postValue(false);
            } else {
                Log.e("UpdateError", "Erro ao atualizar email: " + task.getException());
                success.postValue(false); // Notifique o fracasso sem tentar novamente

            }
        });
        return success;
    }

    public LiveData<ArrayList<Message>> getMessages(String uid) {
        messages = new MutableLiveData<>();

        userRepository.getMessageHistory(uid, (value, error) -> {
            currentMessages = new ArrayList<>();
            if (error != null) {
                if (error.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                    messages.postValue(null);
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
                            Message message = new Message((String) messageMap.get("text"), (long) messageMap.get("messageType"));

                            currentMessages.add(message);
                        }
                    }
                }
                messages.setValue(currentMessages);
                isLoading.postValue(false);
            }
        });

        return messages;
    }

    public void logout() {
        mAuth.signOut();
        Log.d("MyApp", "usuario deslogado");
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
