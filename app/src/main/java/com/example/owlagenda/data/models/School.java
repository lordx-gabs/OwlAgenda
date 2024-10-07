package com.example.owlagenda.data.models;

import com.google.firebase.firestore.DocumentReference;

public class School {
    private String id;
    private DocumentReference userId;
    private String schoolName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public DocumentReference getUserId() {
        return userId;
    }

    public void setUserId(DocumentReference userId) {
        this.userId = userId;
    }

}