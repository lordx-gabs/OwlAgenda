package com.example.owlagenda.ui.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.repository.TaskRepository;
import com.example.owlagenda.ui.homepage.HomePageViewModel;

import java.util.ArrayList;
import java.util.List;

public class TaskWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private List<Task> taskList = new ArrayList<>();

    public TaskWidgetFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        loadTasksFromFirestore();
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (taskList.isEmpty()) {
            return null;  // Se não houver tarefas, não preenche nada
        }

        Task task = taskList.get(position);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_task_item);
        views.setTextViewText(R.id.widget_task_name, task.getTitle());

        // Ação de marcar como concluída
        Intent intent = new Intent(context, TaskWidgetProvider.class);
        intent.setAction("MARK_TASK_COMPLETED");
        intent.putExtra("TASK_ID", task.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                position,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_task_complete_button, pendingIntent);

        return views;
    }


    @Override
    public void onDataSetChanged() {
        // Aqui você deve carregar as tarefas da sua fonte de dados (ex: Firestore)
        Log.d("TaskWidgetFactory", "Carregando tarefas do Firestore...");
        TaskRepository repository = new TaskRepository();
        repository.getTasks("userId", (value, error) -> {
            if (error == null && value != null) {
                taskList.clear();
                taskList.addAll(value.toObjects(Task.class));
                Log.d("TaskWidgetFactory", "Tarefas carregadas: " + taskList.size());
            } else {
                Log.e("TaskWidgetFactory", "Erro ao carregar tarefas: " + (error != null ? error.getMessage() : "Erro desconhecido"));
            }
        });
    }

    @Override
    public void onDestroy() {
        taskList.clear();
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


    private void loadTasksFromFirestore() {
        // Obter o usuário autenticado
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Verifique se o usuário está autenticado
        if (currentUser != null) {
            String userId = currentUser.getUid();  // Obtém o ID do usuário autenticado

            TaskRepository repository = new TaskRepository();
            repository.getTasks(userId, (value, error) -> {
                if (error == null && value != null) {
                    taskList.clear();
                    taskList.addAll(value.toObjects(Task.class));  // Converte os dados para objetos Task
                    Log.d("TaskWidgetFactory", "Tarefas carregadas: " + taskList.size());

                    // Notifica a atualização do widget
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    ComponentName thisWidget = new ComponentName(context, TaskWidgetProvider.class);
                    appWidgetManager.notifyAppWidgetViewDataChanged(
                            appWidgetManager.getAppWidgetIds(thisWidget),
                            R.id.widget_task_list
                    );
                } else {
                    Log.e("TaskWidgetFactory", "Erro ao carregar tarefas: " + (error != null ? error.getMessage() : "Tarefas vazias"));
                }
            });
        } else {
            Log.e("TaskWidgetFactory", "Usuário não autenticado.");
        }
    }




}
