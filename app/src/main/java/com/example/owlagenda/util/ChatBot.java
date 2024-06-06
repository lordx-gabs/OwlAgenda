package com.example.owlagenda.util;

import android.os.Handler;
import android.os.Looper;

import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatBot {
    private ChatFutures chatFutures = ModelChatBot.createChatbotModel().startChat();

    public void sendMessage(String userMessage, Callback<String> callback) {
        Content.Builder messageContent = new Content.Builder();
                messageContent.addText(userMessage);
                messageContent.setRole("user");
        Content userContent = messageContent.build();

        Executor executor = Executors.newSingleThreadExecutor();

        ListenableFuture<GenerateContentResponse> responseFuture = chatFutures.sendMessage(userContent);

        Futures.addCallback(responseFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(result.getText()));
            }

            @Override
            public void onFailure(Throwable t) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(t));
            }
        }, executor);

    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onFailure(Throwable t);
    }

}
