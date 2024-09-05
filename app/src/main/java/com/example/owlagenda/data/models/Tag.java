package com.example.owlagenda.data.models;

import androidx.annotation.ColorRes;

public class Tag {
    private String id;
    private String name;
    private @ColorRes int tagColor;

    public Tag() {
    }

    public Tag(String id, String name, int tagColor) {
        this.id = id;
        this.name = name;
        this.tagColor = tagColor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTagColor() {
        return tagColor;
    }

    public void setTagColor(int tagColor) {
        this.tagColor = tagColor;
    }

}
