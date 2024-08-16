package com.example.owlagenda.util;

import com.example.owlagenda.BuildConfig;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.ai.client.generativeai.type.SafetySetting;

import java.util.Arrays;

public class ModelChatBotSelene {
    private static final String MODEL_ID = "gemini-1.5-flash";
    private static final GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
    private static final SafetySetting harassmentSafetySetting = new SafetySetting(HarmCategory.HARASSMENT,
            BlockThreshold.MEDIUM_AND_ABOVE);
    private static final SafetySetting hateSpeechSafetySetting = new SafetySetting(HarmCategory.HATE_SPEECH,
            BlockThreshold.MEDIUM_AND_ABOVE);
    private static final SafetySetting dangerousContentSafetySetting = new SafetySetting(HarmCategory.DANGEROUS_CONTENT,
            BlockThreshold.MEDIUM_AND_ABOVE);
    private static final SafetySetting sexuallyExplicitSafetySetting = new SafetySetting(HarmCategory.SEXUALLY_EXPLICIT,
            BlockThreshold.MEDIUM_AND_ABOVE);

    public static GenerativeModelFutures createChatbotModelSelene() {
        configBuilder.temperature = 1f;
        configBuilder.topK = 64;
        configBuilder.topP = 0.95f;
        configBuilder.maxOutputTokens = 8192;

        GenerationConfig generationConfig = configBuilder.build();

        GenerativeModel generativeModel = new GenerativeModel(
                MODEL_ID,
                BuildConfig.apiKeyGemini,
                generationConfig,
                Arrays.asList(harassmentSafetySetting, hateSpeechSafetySetting, dangerousContentSafetySetting, sexuallyExplicitSafetySetting),
                new RequestOptions(),
                null,
                null,
                new Content.Builder().addText("Você será um assistente virtual para um app de agenda para professores, seu nome será Selene, uma coruja que ajudará os usuários do Owl  - Agenda para Professores em todos os seus problemas que eles possam ter, você dará informações sobre o app de agenda para professores e ajudar os usuários, e irá responder imitando o jeito de escrever de uma coruja utilizando emoji e tentando sempre ajudar o usuário de forma descontraída e simples, mas sem falar que você é uma coruja , além de sempre estar disposto a ajudar o usuário em suas perguntas.")
                        .build()
        );

        return GenerativeModelFutures.from(generativeModel);
    }

}
