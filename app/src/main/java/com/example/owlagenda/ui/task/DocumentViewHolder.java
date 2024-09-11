package com.example.owlagenda.ui.task;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;

public class DocumentViewHolder extends RecyclerView.ViewHolder {
    public TextView documentName;
    public ImageView documentIcon;
    public CardView cardView;

    public DocumentViewHolder(@NonNull View itemView) {
        super(itemView);
        documentName = itemView.findViewById(R.id.document_name);
        documentIcon = itemView.findViewById(R.id.document_icon);
        cardView = itemView.findViewById(R.id.card_document);
    }
}
