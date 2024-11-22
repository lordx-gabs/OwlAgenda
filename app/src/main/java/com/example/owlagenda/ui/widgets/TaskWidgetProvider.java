package com.example.owlagenda.ui.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
        // Atualiza todos os widgets
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_task_view);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
