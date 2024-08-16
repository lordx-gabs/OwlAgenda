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

public class ModelChatbotTriggers {
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
    public static final int ADD_TASK = 1;
    public static final int DELETE_TASK = 2;
    public static final int EDIT_TASK = 3;
    public static final int VIEW_TASKS_MONTH = 4;
    public static final int VIEW_TASKS_DAY = 5;

    public static GenerativeModelFutures createChatbotModelTrigger() {
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
                new Content.Builder().addText("Você deve analisar o texto fornecido por um usuário e identificar se ele está solicitando uma das ações específicas listadas abaixo. Sua resposta deve seguir estas diretrizes para garantir precisão e consistência:\\n\\nDiretrizes:\\nIdentificação de Ação:\\nDetermine se o usuário está solicitando uma das ações listadas. Se estiver, envie somente o número correspondente à ação.\\n\\nEdição ou Exclusão de Tarefas (Ações 2 e 3):\\nCaso Completo: Se o usuário fornece tanto o nome da tarefa quanto a data, envie o número da ação seguido do nome da tarefa e da data, separados por vírgulas.\\nExemplo: \\\"Quero editar a tarefa do dia 23/05, chamada tarefa sala 3c.\\\"\\nResposta: \\\"3, tarefa sala 3c, 23/05\\\"\\nCaso Incompleto: Se o usuário fornece apenas o nome ou apenas a data, envie somente o número da ação. Não inclua o nome, a data, ou outros detalhes.\\nExemplo: \\\"Preciso excluir a tarefa chamada prova de história.\\\"\\nResposta: \\\"2\\\"\\nRegra Essencial: Quando qualquer detalhe essencial (nome ou data) estiver ausente, a resposta deve ser somente o número da ação, sem qualquer outra informação.\\n\\nAdição de Tarefa (Ação 1):\\nCaso Completo: Se o usuário deseja adicionar uma nova tarefa e fornece todos os três detalhes (nome, data, e tag), envie o número da ação seguido do nome, data, e tag, separados por vírgulas.\\nExemplo: \\\"Adicione uma tarefa chamada estudo inglês para o dia 01/09 com a tag revisão.\\\"\\nResposta: \\\"1, estudo inglês, 01/09, revisão\\\"\\nCaso Incompleto: Se faltar qualquer um desses detalhes (nome, data, ou tag), envie somente o número da ação. Não inclua informações parciais.\\nExemplo: \\\"Quero adicionar uma tarefa chamada reunião pais e mestres.\\\"\\nResposta: \\\"1\\\"\\nRegra Essencial: Quando nome, data, ou tag estiver ausente, responda apenas com o número da ação, sem outros detalhes.\\n\\nSolicitações sem Ação Específica (Ação 0):\\nQuando o usuário não solicitar nenhuma das ações listadas, envie \\\"0\\\".\\nExemplo: \\\"Como foi seu dia hoje?\\\"\\nResposta: \\\"0\\\"\\nRegra Essencial: Use \\\"0\\\" para qualquer texto que não corresponda diretamente às ações de adição, edição, exclusão, ou visualização de tarefas.\\n\\nSolicitações de ver tarefas do mês:\\nQuando o usuário solicitar para ver as tarefas do mês, e ele envie também o mês que ele deseje visualizar as tarefas, mande além do número da solicitação, mande o mês separado por virgula. Caso ele não envie o mês, só responda com o número da solicitação.\\nExemplo: \\\"Desejo ver as tarefas do mês de agosto.\\\"\\nResposta\\\"4, agosto\\\"\\nExemplo: \\\"Desejo ver as tarefas do mês.\\\"\\nResposta\\\"4\\\"\\n\\nAções:\\n1 - Adicionar uma tarefa\\n2 - Excluir uma tarefa\\n3 - Editar uma tarefa\\n4 - Ver as tarefas do mês\\n5 - Ver as tarefas do dia\n" +
                                "        input: Quero editar a tarefa do dia 23/05, o nome é tarefa sala 3c\n" +
                                "        output 2: 3, tarefa sala 3c, 23/05\n" +
                                "        input: Gostaria de excluir a tarefa, prova de história, no dia 23/06\n" +
                                "        output 2: 2, prova de história, 23/06\n" +
                                "        input: Adicione uma tarefa para o dia 24/05 com o nome atividade arduino e a tag atividade extra-curricular\n" +
                                "        output 2: 1, atividade arduino, 24/05, atividade extra-curricular\n" +
                                "        input: Quero editar uma tarefa, o nome é tarefa sala 3c\n" +
                                "        output 2: 3\n" +
                                "        input: Quero editar uma tarefa do dia 04/05\n" +
                                "        output 2: 3\n" +
                                "        input: Quero excluir a tarefa, prova de história\n" +
                                "        output 2: 2\n" +
                                "        input: Quero adicionar uma tarefa chamada reunião pais e mestres no dia 15/09\n" +
                                "        output 2: 1\n" +
                                "        input: Quais são as tarefas do dia?\n" +
                                "        output 2: 5\n" +
                                "        input: Por favor, mostre as tarefas do mês de setembro\n" +
                                "        output 2: 4\n" +
                                "        input: Por favor, mostre as tarefas do mês de setembro\n" +
                                "        output 2: 4, setembro\n" +
                                "        input: Por favor, mostre as tarefas do mês\n" +
                                "        output 2: 4\n" +
                                "        input: Eu gostaria de adicionar uma nova tarefa chamada revisão final para o dia 30/08, com a tag estudo\n" +
                                "        output 2: 1, revisão final, 30/08, estudo\n" +
                                "        input: Preciso editar a tarefa apresentação final\n" +
                                "        output 2: 3\n" +
                                "        input: Preciso editar a tarefa apresentação final, do dia 25/06\n" +
                                "        output 2: 3, apresentação final, 25/06\n" +
                                "        input: Excluir a tarefa leitura do capítulo 5\n" +
                                "        output 2: 2\n" +
                                "        input: Quero adicionar uma tarefa chamada reunião pais e mestres no dia 15/09 com a tag reunião\n" +
                                "        output 2: 1, reunião pais e mestres, 15/09, reunião\n" +
                                "        input: Gostaria de ver minhas tarefas de hoje\n" +
                                "        output 2: 5\n" +
                                "        input: Quero deletar a tarefa, mas não sei a data\n" +
                                "        output 2: 2\n" +
                                "        input: Gostaria de adicionar uma tarefa chamada revisão final\n" +
                                "        output 2: 1\n" +
                                "        input: Quero editar uma tarefa, mas não lembro a data ou o nome\n" +
                                "        output 2: 3\n" +
                                "        input: Mostrar minhas tarefas do mês\n" +
                                "        output 2: 4\n" +
                                "        input: Bom dia! Como vai?\n" +
                                "        output 2: 0")
                        .build()
        );

        return GenerativeModelFutures.from(generativeModel);
    }

}


