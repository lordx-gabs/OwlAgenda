package com.example.owlagenda.util;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SyncData extends ViewModel {
    private static final MutableLiveData<Boolean> syncStatus = new MutableLiveData<>();
    private static final MutableLiveData<Boolean> isLoadingSync = new MutableLiveData<>();
    public static final CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("usuario");
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public static void synchronizeUserWithFirebase(User user) {
        isLoadingSync.setValue(true);
        collectionReference.add(user).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                syncStatus.setValue(false);

                scheduledExecutorService.schedule(() -> synchronizeUserWithFirebase(user), 5, TimeUnit.SECONDS);

            } else {
                syncStatus.setValue(true);
                scheduledExecutorService.shutdown();
                isLoadingSync.setValue(false);
            }
        });
    }

    public LiveData<Boolean> getSyncStatus() {
        return syncStatus;
    }
}
