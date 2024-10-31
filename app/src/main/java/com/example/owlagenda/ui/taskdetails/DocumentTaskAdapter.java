package com.example.owlagenda.ui.taskdetails;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.TaskAttachments;
import com.example.owlagenda.ui.task.DocumentViewHolder;
import com.example.owlagenda.ui.task.OnItemClickListener;

import java.util.ArrayList;

public class DocumentTaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<TaskAttachments> documents;
    private final OnItemClickListener clickListenerDocument;

    public DocumentTaskAdapter(OnItemClickListener clickListenerDocument, ArrayList<TaskAttachments> documents) {
        this.clickListenerDocument = clickListenerDocument;
        this.documents = documents;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DocumentTaskDetailsViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_document_task_details, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DocumentTaskDetailsViewHolder viewHolder = ((DocumentTaskDetailsViewHolder) holder);
        TaskAttachments document = documents.get(position);
        viewHolder.documentName.setText(document.getName());
        String typeDocument = document.getExtension();

        if (!document.isLoading()) {
            viewHolder.loading.setVisibility(View.GONE);
            viewHolder.documentIcon.setVisibility(View.VISIBLE);
            viewHolder.documentName.setVisibility(View.VISIBLE);
            if (!typeDocument.isEmpty()) {
                switch (typeDocument) {
                    case "pdf" -> viewHolder.documentIcon.setImageResource(R.drawable.ic_pdf);
                    case "doc", "docx" ->
                            viewHolder.documentIcon.setImageResource(R.drawable.ic_docx);
                    case "xls", "xlsx" ->
                            viewHolder.documentIcon.setImageResource(R.drawable.ic_xls);
                    case "ppt", "pptx" ->
                            viewHolder.documentIcon.setImageResource(R.drawable.ic_pptx);
                    case "jpg", "jpeg", "png" ->
                            viewHolder.documentIcon.setImageResource(R.drawable.ic_img);

                    default -> viewHolder.documentIcon.setImageResource(R.drawable.ic_document);
                }

            } else {
                viewHolder.documentIcon.setImageResource(R.drawable.ic_document);
            }
        } else {
            viewHolder.loading.setVisibility(View.VISIBLE);
            viewHolder.documentIcon.setVisibility(View.GONE);
            viewHolder.documentName.setVisibility(View.GONE);
            viewHolder.loading.setProgress(document.getPercent());
        }

        viewHolder.itemView.setOnClickListener(v -> clickListenerDocument.onItemClick(position));

    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    public ArrayList<TaskAttachments> getDocuments() {
        return documents;
    }

}
