package com.example.owlagenda.ui.selene;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.owlagenda.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<Message> messages;
    private final String profilePhotoUrl;
    private final UserMessageViewHolder.onClickErrorListener listener;

    public MessageAdapter(List<Message> messages, String profilePhotoUrl, UserMessageViewHolder.onClickErrorListener listener) {
        this.messages = messages;
        this.profilePhotoUrl = profilePhotoUrl;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        long messageTypeLong = messages.get(position).getMessageType();
        return (int) messageTypeLong;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Message.TYPE_USER_MESSAGE) {
            return new UserMessageViewHolder(
                    LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.balloons_user, parent, false), listener);
        } else {
            return new SeleneMessageViewHolder(
                    LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.balloons_bot, parent, false));
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
            if(message.isMessageError()) {
                ((UserMessageViewHolder) holder).icError.setVisibility(View.VISIBLE);
            }
        } else {
            ((SeleneMessageViewHolder) holder).textMessage.setText(message.getText());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
