package com.example.owlagenda.ui.task;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.TaskAttachments;

import java.util.ArrayList;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<TaskAttachments> documents;
    private final OnItemClickListener clickListenerAddDocument, clickListenerDocument;
    private static final int VIEW_TYPE_DOCUMENT = 0;
    private static final int VIEW_TYPE_ADD = 1;

    public DocumentAdapter(OnItemClickListener clickListenerAddDocument, OnItemClickListener clickListenerDocument) {
        this.clickListenerAddDocument = clickListenerAddDocument;
        this.clickListenerDocument = clickListenerDocument;
        this.documents = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DocumentViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_document, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DocumentViewHolder viewHolder = ((DocumentViewHolder) holder);
        if(getItemViewType(position) == VIEW_TYPE_ADD) {
            viewHolder.documentName.setVisibility(View.GONE);
            viewHolder.documentIcon.setImageResource(R.drawable.ic_fab);
            viewHolder.itemView.setOnClickListener(v ->
                    clickListenerAddDocument.onItemClick(position));
            ViewGroup.LayoutParams layoutParams = viewHolder.cardView.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            viewHolder.cardView.setLayoutParams(layoutParams);
        } else {
            TaskAttachments document = documents.get(position);
            viewHolder.documentName.setText(document.getName());
            String typeDocument = document.getExtension();
            if(!typeDocument.isEmpty()) {

                switch (typeDocument) {
                    case "pdf" -> viewHolder.documentIcon.setImageResource(R.drawable.ic_pdf2);
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
            viewHolder.itemView.setOnClickListener(v ->
                    clickListenerDocument.onItemClick(position));
        }
    }

    @Override
    public int getItemCount() {
        return documents.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == documents.size() ? VIEW_TYPE_ADD : VIEW_TYPE_DOCUMENT;
    }

    public ArrayList<TaskAttachments> getDocuments() {
        return documents;
    }

}
