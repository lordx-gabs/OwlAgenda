package com.example.owlagenda.util;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.owlagenda.data.repository.TaskRepository;
import com.google.firebase.FirebaseNetworkException;


public class CompleteTaskReceiver extends BroadcastReceiver {
    private final TaskRepository taskRepository = new TaskRepository();

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskName = intent.getStringExtra("taskId");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Cancelar a notificação com o código de requisição correspondente
        notificationManager.cancel(intent.getIntExtra("requestCode", 0));

        taskRepository.getTaskByTitle(taskName.toUpperCase(), task -> {
            if(task.isSuccessful()) {
                if (task.getResult() != null) {
                    if(!task.getResult().getDocuments().isEmpty()) {
                        task.getResult().getDocuments().get(0).getReference().update("completed", true)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(context, "Tarefa concluída com sucesso!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (task1.getException() instanceof FirebaseNetworkException) {
                                            Toast.makeText(context, "Erro de conexão. Verifique sua conexão e tente novamente.", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        Toast.makeText(context, "Erro ao concluir tarefa", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            } else {
                if (task.getException() instanceof FirebaseNetworkException) {
                   Toast.makeText(context, "Erro de conexão. Verifique sua conexão e tente novamente.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(context, "Erro ao concluir tarefa", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

