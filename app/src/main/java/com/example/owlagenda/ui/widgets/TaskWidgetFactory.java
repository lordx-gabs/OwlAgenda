package com.example.owlagenda.ui.widgets;

import android.content.Context;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private List<Task> taskList = new ArrayList<>();

    public TaskWidgetFactory(Context context) {
        this.context = context.getApplicationContext();  // Garantindo que seja o ApplicationContext
    }

    @Override
    public void onCreate() {
        // Carregar as tarefas do Firestore aqui
        loadTasksFromFirebase();
    }

    @Override
    public void onDataSetChanged() {
        // Método para atualizar a lista de tarefas
    }

    @Override
    public void onDestroy() {
        // Limpeza, se necessário
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Task task = taskList.get(position);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_task_item);
        views.setTextViewText(R.id.widget_task_name, task.getTitle());

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null; // Você pode personalizar a tela de carregamento
    }

    @Override
    public int getViewTypeCount() {
        return 1;  // Aqui você pode especificar quantos tipos de views diferentes o widget possui
    }

    @Override
    public long getItemId(int position) {
        return position;  // Retorna o ID da tarefa, ou o índice
    }

    @Override
    public boolean hasStableIds() {
        return true;  // Defina como verdadeiro se os IDs dos itens não mudarem
    }

    private void loadTasksFromFirebase() {
        // Use TaskRepository para buscar as tarefas do Firestore
        // Preencher taskList com as tarefas obtidas
    }
}
