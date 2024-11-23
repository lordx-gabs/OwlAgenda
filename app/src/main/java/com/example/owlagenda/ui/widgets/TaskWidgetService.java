package com.example.owlagenda.ui.widgets;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.owlagenda.ui.widgets.TaskWidgetFactory;

public class TaskWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TaskWidgetFactory(getApplicationContext());
    }

    // A classe TaskWidgetFactory implementa RemoteViewsFactory,
    // ela será responsável por preencher os dados no widget
}
