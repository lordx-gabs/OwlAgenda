package com.example.owlagenda.ui.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.owlagenda.R;
import com.example.owlagenda.data.repository.TaskRepository;

public class TaskWidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if ("MARK_TASK_COMPLETED".equals(intent.getAction())) {
            String taskId = intent.getStringExtra("TASK_ID");

            // Chama o método para marcar a tarefa como concluída
            TaskRepository repository = new TaskRepository();
            repository.markTaskAsCompleted(taskId, true, task -> {
                // Após a tarefa ser marcada como concluída, atualiza o widget
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName thisWidget = new ComponentName(context, TaskWidgetProvider.class);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                onUpdate(context, appWidgetManager, appWidgetIds);
            });
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_task_view);

            // Configure o RemoteAdapter (conectando a lista de tarefas com o widget)
            Intent intent = new Intent(context, TaskWidgetService.class);
            views.setRemoteAdapter(R.id.widget_task_list, intent);

            // Definir a EmptyView para mostrar a mensagem quando não houver tarefas
            views.setEmptyView(R.id.widget_task_list, R.id.widget_no_tasks_message);

            // Atualizar o widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
            Log.d("TaskWidgetProvider", "Widget atualizado com sucesso.");
        }
    }


}
