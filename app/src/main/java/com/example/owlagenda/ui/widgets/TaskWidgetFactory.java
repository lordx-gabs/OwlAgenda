package com.example.owlagenda.ui.widgets;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.repository.TaskRepository;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

public class TaskWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private List<Task> taskList = new ArrayList<>();  // Lista de tarefas

    public TaskWidgetFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        // Não precisamos de loadTasksFromFirebase, chamaremos getTasks diretamente
        loadTasksFromFirestore();
    }

    @Override
    public int getCount() {
        return taskList.size();  // Retorna o número de tarefas
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Task task = taskList.get(position);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_task_item);
        views.setTextViewText(R.id.widget_task_name, task.getTitle());

        // Configura o clique para marcar como concluída
        Intent intent = new Intent(context, TaskWidgetProvider.class);
        intent.setAction("MARK_TASK_COMPLETED");
        intent.putExtra("TASK_ID", task.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, position, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.widget_task_complete_button, pendingIntent);

        return views;
    }

    @Override
    public void onDataSetChanged() {
        loadTasksFromFirestore();  // Carregar as tarefas sempre que o widget for atualizado
    }

    @Override
    public void onDestroy() {
        // Limpeza de recursos, se necessário
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;  // Não há visualização de carregamento
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    // Usando o método getTasks do TaskRepository para carregar as tarefas
    private void loadTasksFromFirestore() {
        String userId = "userIdAqui";  // Substitua com o ID real do usuário

        TaskRepository repository = new TaskRepository();
        repository.getTasks(userId, (value, error) -> {
            if (error == null) {
                taskList = value.toObjects(Task.class);  // Atualiza a lista de tarefas
                onDataSetChanged();  // Chama onDataSetChanged para garantir que o widget seja atualizado
            } else {
                taskList.clear();  // Em caso de erro, limpa a lista
                onDataSetChanged();  // Garante que o widget seja atualizado
            }
        });
    }
}
