package com.example.owlagenda.util;

import androidx.annotation.ColorRes;
import com.example.owlagenda.R;

public enum TaskTypeColor {

    PROVA("Prova", R.color.prova_color),
    ATIVIDADE_AVALIATIVA("Atividade Avaliativa", R.color.atividade_avaliativa_color),
    TRABALHO_DE_CASA("Trabalho de Casa", R.color.trabalho_de_casa_color),
    RELATORIO("Relatório", R.color.relatorio_color),
    PROJETO("Projeto", R.color.projeto_color),
    ATIVIDADE_EXTRA_CURRICULAR("Atividade Extra-Curricular", R.color.atividade_extra_curricular_color),
    EVENTO_ESCOLAR("Evento Escolar", R.color.evento_escolar_color),
    REUNIAO_COM_PAIS("Reunião com Pais", R.color.reuniao_com_pais_color),
    PREPARACAO_DE_AULA("Preparação de Aula", R.color.preparacao_de_aula_color),
    ATIVIDADE_DESENVOLVIMENTO_PROFISSIONAL("Atividade de Desenvolvimento Profissional", R.color.atividade_desenvolvimento_profissional_color),
    OUTROS("Outros", R.color.outros_color);

    private final String tagName;
    @ColorRes
    private final int colorResId;

    TaskTypeColor(String tagName, int colorHex) {
        this.tagName = tagName;
        this.colorResId = colorHex;
    }

    public String getTagName() {
        return tagName;
    }

    public int getColorHex() {
        return colorResId;
    }

    public static TaskTypeColor fromTagName(String tagName) {
        for (TaskTypeColor taskType : TaskTypeColor.values()) {
            if (taskType.getTagName().equalsIgnoreCase(tagName)) {
                return taskType;
            }
        }
        throw new IllegalArgumentException("Unknown tag name: " + tagName);
    }
}
