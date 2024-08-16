package com.example.owlagenda.ui.corubot;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.owlagenda.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<Message> messages;
    private final String profilePhotoUrl;

    public MessageAdapter(List<Message> messages, String profilePhotoUrl) {
        this.messages = messages;
        this.profilePhotoUrl = profilePhotoUrl;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getMessageType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Message.TYPE_USER_MESSAGE) {
            return new UserMessageViewHolder(
                    LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.balloons_user, parent, false)
            );
        } else {
            return new SeleneMessageViewHolder(
                    LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.balloons_bot, parent, false)
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).textMessage.setText(message.getText());
            Glide.with(holder.itemView.getContext())
                    .load(profilePhotoUrl)
                    .circleCrop()
                    .into(((UserMessageViewHolder) holder).profilePhoto);
        } else {
            ((SeleneMessageViewHolder) holder).textMessage.setText(message.getText());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
