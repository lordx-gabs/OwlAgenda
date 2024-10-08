package com.example.owlagenda.ui.calendar;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;
import com.google.android.material.button.MaterialButton;

public class TaskViewHolder extends RecyclerView.ViewHolder {
    public TextView nameTaskLayout, dateTaskLayout;
    public MaterialButton btnEditTaskLayout, btnDeleteTaskLayout, btnDetailsTaskLayout;

    public TaskViewHolder(@NonNull View itemView, OnClickTask onClickTask) {
        super(itemView);
        nameTaskLayout = itemView.findViewById(R.id.tv_name_details_task_layout);
        dateTaskLayout = itemView.findViewById(R.id.tv_date_details_task_layout);
        btnEditTaskLayout = itemView.findViewById(R.id.btn_details_task_layout_edit);
        btnEditTaskLayout.setOnClickListener(v -> onClickTask.onClickBtnEdit(getAbsoluteAdapterPosition()));
        btnDeleteTaskLayout = itemView.findViewById(R.id.btn_details_task_layout_delete);
        btnDeleteTaskLayout.setOnClickListener(v -> onClickTask.onClickBtnDelete(getAbsoluteAdapterPosition()));
        btnDetailsTaskLayout = itemView.findViewById(R.id.btn_details_task_layout_details);
        btnDetailsTaskLayout.setOnClickListener(v -> onClickTask.onClickBtnDetails(getAbsoluteAdapterPosition()));
    }

    public interface OnClickTask {
        void onClickBtnEdit(int position);
        void onClickBtnDelete(int position);
        void onClickBtnDetails(int position);
    }
}
