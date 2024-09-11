package com.example.owlagenda.ui.selene;

import androidx.annotation.NonNull;

public class Message {
    private String text;
    private long messageType;
    public static final int TYPE_USER_MESSAGE = 0;
    public static final int TYPE_SELENE_MESSAGE = 1;

    public Message(String text, long messageType) {
        this.text = text;
        this.messageType = messageType;
    }

    public Message() {

    }

    public String getText() {
        return text;
    }

    public long getMessageType() {
        return messageType;
    }

    @NonNull
    public String toString() {
        return text + "////" + messageType;
    }
}
