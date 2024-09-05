package com.example.owlagenda.ui.selene;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;

public class SeleneMessageViewHolder extends RecyclerView.ViewHolder {
    public TextView textMessage;

    public SeleneMessageViewHolder(@NonNull View itemView) {
        super(itemView);
        textMessage = itemView.findViewById(R.id.tv_balloons_selene);
    }
}
