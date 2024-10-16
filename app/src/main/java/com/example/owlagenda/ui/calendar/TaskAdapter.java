package com.example.owlagenda.ui.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<TaskCalendar> tasks;
    private final TaskViewHolder.OnClickTask onClickTask;

    public TaskAdapter(TaskViewHolder.OnClickTask onClickTask) {
        this.tasks = new ArrayList<>();
        this.onClickTask = onClickTask;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskViewHolder(
                LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.details_task_view, parent, false), onClickTask
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TaskViewHolder viewHolder = ((TaskViewHolder) holder);
        TaskCalendar task = tasks.get(position);
        String title = task.getNameTask() + "\n" + task.getSchoolClass();
        viewHolder.nameTaskLayout.setText(title);
        viewHolder.dateTaskLayout.setText(LocalDate.parse(task.getDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" + task.getTag());
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public List<TaskCalendar> getTasks() {
        return tasks;
    }

}
