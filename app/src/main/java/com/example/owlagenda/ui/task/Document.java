package com.example.owlagenda.ui.task;

import android.graphics.drawable.Drawable;

public class Document {
    private String nameDocument;

    public Document(String nameDocument) {
        this.nameDocument = nameDocument;
    }
    public Document() {

    }

    public String getNameDocument() {
        return nameDocument;
    }

    public void setNameDocument(String nameDocument) {
        this.nameDocument = nameDocument;
    }

    public String getExtension() {
        // Verifica se o nome do documento contém um ponto
        int dotIndex = nameDocument.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex < nameDocument.length() - 1) {
            return nameDocument.substring(dotIndex + 1);
        }
        return "";
    }

}
