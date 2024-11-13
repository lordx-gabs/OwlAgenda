package com.example.owlagenda.ui.classesschools;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;

public class ClassViewHolder extends RecyclerView.ViewHolder {
    TextView className, numberStudents, period, schoolName;
    Button editClass, deleteClass;

    public ClassViewHolder(@NonNull View itemView, onClickClassListener onClickClassListener) {
        super(itemView);
        className = itemView.findViewById(R.id.tv_class_name);
        numberStudents = itemView.findViewById(R.id.tv_number_students_class);
        period = itemView.findViewById(R.id.tv_period_class);
        schoolName = itemView.findViewById(R.id.tv_class_school);
        editClass = itemView.findViewById(R.id.btn_edit_class);
        deleteClass = itemView.findViewById(R.id.btn_delete_class);

        editClass.setOnClickListener(v -> {
            int position = getAbsoluteAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                onClickClassListener.onEditClassClick(position);
            }
        });

        deleteClass.setOnClickListener(v -> {
            int position = getAbsoluteAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                onClickClassListener.onDeleteClassClick(position);
            }
        });
    }

    public interface onClickClassListener {
        void onEditClassClick(int position);

        void onDeleteClassClick(int position);
    }
}
