package com.example.owlagenda.ui.classesschools;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;

public class SchoolViewHolder extends RecyclerView.ViewHolder {
    TextView schoolName;
    Button btnEditSchool, btnDeleteSchool;

    public SchoolViewHolder(@NonNull View itemView, onSchoolClickListener onSchoolClickListener) {
        super(itemView);
        schoolName = itemView.findViewById(R.id.tv_school_name);
        btnEditSchool = itemView.findViewById(R.id.btn_edit_school);
        btnDeleteSchool = itemView.findViewById(R.id.btn_delete_school);
        btnEditSchool.setOnClickListener(v -> {
            if (getAbsoluteAdapterPosition() != RecyclerView.NO_POSITION) {
                onSchoolClickListener.onEditSchoolClick(getAbsoluteAdapterPosition());
            }
        });
        btnDeleteSchool.setOnClickListener(v -> {
            if (getAbsoluteAdapterPosition() != RecyclerView.NO_POSITION) {
                onSchoolClickListener.onDeleteSchoolClick(getAbsoluteAdapterPosition());
            }
        });
    }

    public interface onSchoolClickListener {
        void onEditSchoolClick(int position);
        void onDeleteSchoolClick(int position);
    }
}
