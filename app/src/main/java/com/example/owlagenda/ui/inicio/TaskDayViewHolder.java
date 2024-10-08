package com.example.owlagenda.ui.inicio;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;

public class TaskDayViewHolder extends RecyclerView.ViewHolder {
    TextView tvTaskDayTitle, tvTaskDayClass, tvTaskDayTag, tvTaskDayDate;
    Button btnTaskCheck;

    public TaskDayViewHolder(@NonNull View itemView, onClickListener listener) {
        super(itemView);
        tvTaskDayTitle = itemView.findViewById(R.id.tv_task_day_name);
        tvTaskDayClass = itemView.findViewById(R.id.tv_task_day_class);
        tvTaskDayDate = itemView.findViewById(R.id.tv_task_day_date);
        tvTaskDayTag = itemView.findViewById(R.id.tv_task_day_tag);
        btnTaskCheck = itemView.findViewById(R.id.btn_task_check);
        btnTaskCheck.setOnClickListener(v -> listener.onClickCheck(getAbsoluteAdapterPosition()));
        itemView.setOnClickListener(v -> listener.onClickTask(getAbsoluteAdapterPosition()));
    }

    public interface onClickListener{
        void onClickCheck(int position);
        void onClickTask(int position);
    }

}
