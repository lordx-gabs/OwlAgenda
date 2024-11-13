package com.example.owlagenda.ui.classesschools;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;
import com.example.owlagenda.ui.filtertask.FilterTaskViewHolder;

import java.util.ArrayList;

public class SchoolAdapter extends RecyclerView.Adapter<SchoolViewHolder> {
    private final ArrayList<SchoolModel> schools;
    private final SchoolViewHolder.onSchoolClickListener listener;

    public SchoolAdapter(ArrayList<SchoolModel> schools, SchoolViewHolder.onSchoolClickListener listener) {
        this.schools = schools;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SchoolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SchoolViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.layout_school, parent, false), listener);
    }

    @Override
    public void onBindViewHolder(@NonNull SchoolViewHolder holder, int position) {
        SchoolModel school = schools.get(position);
        holder.schoolName.setText(school.getSchoolName());

    }

    @Override
    public int getItemCount() {
        return schools.size();
    }
}
