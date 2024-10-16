package com.example.owlagenda.ui.inicio;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.TaskDay;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;

public class TaskDayAdapter extends RecyclerView.Adapter<TaskDayViewHolder> {
    private final ArrayList<TaskDay> taskDays;
    private final TaskDayViewHolder.onClickListener listener;

    public TaskDayAdapter(ArrayList<TaskDay> taskDays, TaskDayViewHolder.onClickListener listener) {
        // Ordena o array de tasks por data (menor para maior)
        this.taskDays = taskDays;
        this.taskDays.sort(Comparator.comparing(taskDay ->
                LocalDate.parse(taskDay.getDateTaskDay(), DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        ));
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskDayViewHolder(
                LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.layout_task_day, parent, false), listener);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskDayViewHolder holder, int position) {
        TaskDay taskDay = taskDays.get(position);
        holder.tvTaskDayTitle.setText(taskDay.getTitleTaskDay());
        holder.tvTaskDayClass.setText(taskDay.getSchoolNameTaskDay() +  " - " + taskDay.getClassTaskDay());
        holder.tvTaskDayTag.setText(taskDay.getTagTaskDay());
        int positionBefore = position - 1;
        if (positionBefore != -1 && taskDays.get(positionBefore).getDateTaskDay().equals(taskDay.getDateTaskDay())) {
            holder.tvTaskDayDate.setVisibility(View.GONE);
        } else {
            holder.tvTaskDayDate.setText(taskDay.getDateTaskDay());
        }
    }

    @Override
    public int getItemCount() {
        return taskDays.size();
    }
}
