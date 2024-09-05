package com.example.owlagenda.ui.telaprincipal;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.repository.UserRepository;
import com.example.owlagenda.ui.selene.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

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
        userRepository.getUserById(uid, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user.postValue(snapshot.getValue(User.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (error.getCode() == DatabaseError.DISCONNECTED || error.getCode() == DatabaseError.NETWORK_ERROR) {
                    errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                } else {
                    user.postValue(null);
                }
                isLoading.setValue(false);
            }
        });

        return user;
    }
    private final MutableLiveData<ArrayList<Message>> messages = new MutableLiveData<>(new ArrayList<>());

    public LiveData<ArrayList<Message>> getMessages(String uid) {
        userRepository.getMessageHistory(uid, new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    currentMessages.add(message);
                    messages.postValue(currentMessages);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (error.getCode() == DatabaseError.DISCONNECTED || error.getCode() == DatabaseError.NETWORK_ERROR) {
                    errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                } else {
                    messages.postValue(null);
                }
                isLoading.setValue(false);
            }
        });

        return messages;
    }


    public void logout() {
        mAuth.signOut();
        Log.d("MyApp", "usuario deslogado");
    }
}
