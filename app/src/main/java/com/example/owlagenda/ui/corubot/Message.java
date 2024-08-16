package com.example.owlagenda.ui.corubot;

public class Message {
    private String text;
    private int messageType;
    public static final int TYPE_USER_MESSAGE = 0;
    public static final int TYPE_SELENE_MESSAGE = 1;

    public Message(String text, int messageType) {
        this.text = text;
        this.messageType = messageType;
    }

    public String getText() {
        return text;
    }

    public int getMessageType() {
        return messageType;
    }
}
