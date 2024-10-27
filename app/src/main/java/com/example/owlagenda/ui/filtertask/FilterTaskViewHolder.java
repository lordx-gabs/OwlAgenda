package com.example.owlagenda.ui.filtertask;


import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;

public class FilterTaskViewHolder extends RecyclerView.ViewHolder {
    TextView tvTaskDayTitle, tvTaskDayClass, tvTaskDayTag, tvTaskDayDate;
    Button btnTaskCheck;

    public FilterTaskViewHolder(@NonNull View itemView, onClickListenerTaskFilter listener) {
        super(itemView);
        tvTaskDayTitle = itemView.findViewById(R.id.tv_task_day_name);
        tvTaskDayClass = itemView.findViewById(R.id.tv_task_day_class);
        tvTaskDayDate = itemView.findViewById(R.id.tv_task_day_date);
        tvTaskDayTag = itemView.findViewById(R.id.tv_task_day_tag);
        btnTaskCheck = itemView.findViewById(R.id.btn_task_check);
        btnTaskCheck.setVisibility(View.GONE);
        itemView.setOnClickListener(v -> listener.onClickTask(getAbsoluteAdapterPosition()));
    }

    public interface onClickListenerTaskFilter{
        void onClickTask(int position);
    }
}
