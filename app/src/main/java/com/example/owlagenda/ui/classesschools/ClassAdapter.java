package com.example.owlagenda.ui.classesschools;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;

import java.util.ArrayList;

public class ClassAdapter extends RecyclerView.Adapter<ClassViewHolder> {
    private ArrayList<ClassModel> classes;
    private final ClassViewHolder.onClickClassListener listener;

    public ClassAdapter(ArrayList<ClassModel> classes, ClassViewHolder.onClickClassListener listener) {
        this.classes = classes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ClassViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.layout_classes, parent, false), listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        ClassModel classModel = classes.get(position);

        holder.className.setText(classModel.getNameClass());
        holder.period.setText(classModel.getPeriod());
        holder.schoolName.setText(classModel.getSchoolName());
        holder.numberStudents.setText("Número de alunos: " + classModel.getNumberStudents());

    }

    @Override
    public int getItemCount() {
        return classes.size();
    }
}
