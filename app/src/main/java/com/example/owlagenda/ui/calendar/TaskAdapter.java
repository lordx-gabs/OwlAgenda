package com.example.owlagenda.ui.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.Task;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Task> tasks;
    private final View.OnClickListener btnEditListener, btnDeleteListener, btnDetailsListener;
    private final Context context;

    public TaskAdapter(Context context, View.OnClickListener btnEditListener, View.OnClickListener btnDeleteListener
            ,View.OnClickListener btnDetailsListener) {
        this.btnEditListener = btnEditListener;
        this.btnDeleteListener = btnDeleteListener;
        this.btnDetailsListener = btnDetailsListener;
        this.tasks = new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskViewHolder(
                LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.details_task_view, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TaskViewHolder viewHolder = ((TaskViewHolder) holder);
        Task task = tasks.get(position);
        String title = task.getTitle() + "\n" + task.getTaskClass();
        viewHolder.nameTaskLayout.setText(title);
        viewHolder.dateTaskLayout.setText(DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .format(task.getDate()));
        viewHolder.btnEditTaskLayout.setOnClickListener(btnEditListener);
        viewHolder.btnDeleteTaskLayout.setOnClickListener(btnDeleteListener);
        viewHolder.btnDetailsTaskLayout.setOnClickListener(btnDetailsListener);

    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public List<Task> getTasks() {
        return tasks;
    }

}
