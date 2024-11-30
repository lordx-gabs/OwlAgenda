package com.example.owlagenda.ui.selene;

public class Message {
    private String text;
    private long messageType;
    private boolean isMessageError;
    public static final int TYPE_USER_MESSAGE = 0;
    public static final int TYPE_SELENE_MESSAGE = 1;

    public Message(String text, long messageType, boolean isMessageError) {
        this.text = text;
        this.messageType = messageType;
        this.isMessageError = isMessageError;
    }

    public Message() {

    }

    public boolean isMessageError() {
        return isMessageError;
    }

    public void setMessageError(boolean messageError) {
        isMessageError = messageError;
    }

    public String getText() {
        return text;
    }

    public long getMessageType() {
        return messageType;
    }

}
