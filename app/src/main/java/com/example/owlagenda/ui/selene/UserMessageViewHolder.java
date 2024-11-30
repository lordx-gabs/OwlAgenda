package com.example.owlagenda.ui.selene;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;
import com.google.android.material.imageview.ShapeableImageView;

public class UserMessageViewHolder extends RecyclerView.ViewHolder {
    public TextView textMessage;
    public ShapeableImageView profilePhoto;
    public ImageView icError;

    public UserMessageViewHolder(@NonNull View itemView, onClickErrorListener listener) {
        super(itemView);
        textMessage = itemView.findViewById(R.id.tv_balloons_user);
        profilePhoto = itemView.findViewById(R.id.profile_photo_ballons_user);
        icError = itemView.findViewById(R.id.ic_error_message);
        icError.setOnClickListener(v -> listener.onClickError(getAbsoluteAdapterPosition()));
    }

    public interface onClickErrorListener {
        void onClickError(int position);
    }
}
