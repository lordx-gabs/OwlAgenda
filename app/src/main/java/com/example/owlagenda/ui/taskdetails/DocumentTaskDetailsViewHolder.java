package com.example.owlagenda.ui.taskdetails;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class DocumentTaskDetailsViewHolder extends RecyclerView.ViewHolder {
    public TextView documentName;
    public ImageView documentIcon;
    public CardView cardView;
    public CircularProgressIndicator loading;

    public DocumentTaskDetailsViewHolder(@NonNull View itemView) {
        super(itemView);
        documentName = itemView.findViewById(R.id.document_name_task_details);
        documentIcon = itemView.findViewById(R.id.document_icon_task_details);
        cardView = itemView.findViewById(R.id.card_document_task_details);
        loading = itemView.findViewById(R.id.loading_document_task_details);
    }
}
