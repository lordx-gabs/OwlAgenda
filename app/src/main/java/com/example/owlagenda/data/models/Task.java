package com.example.owlagenda.data.models;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

public class Task {
    private String id;
    private DocumentReference userId;
    private String title;
    private String titleSearch;
    private String description;
    private String date;
    private DocumentReference schoolClass;
    private DocumentReference school;
    private String tag;
    private boolean isCompleted;
    private Integer notificationBefore;
    private ArrayList<TaskAttachments> taskDocuments;

    public Task(DocumentReference userId, String title, String titleSearch, String description, String date, DocumentReference school, DocumentReference schoolClass, String tag, ArrayList<TaskAttachments> taskDocuments, Integer notificationBefore) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.date = date;
        this.schoolClass = schoolClass;
        this.tag = tag;
        this.school = school;
        this.isCompleted = false;
        this.taskDocuments = taskDocuments;
        this.notificationBefore = notificationBefore;
        this.titleSearch = titleSearch;
    }

    public Task() {
    }

    public Task(DocumentReference userId, String taskName, String titleSearch, String taskDescription, String taskType, String taskDate, DocumentReference classRef, DocumentReference schoolRef) {
        this.title = taskName;
        this.description = taskDescription;
        this.date = taskDate;
        this.schoolClass = classRef;
        this.tag = taskType;
        this.school = schoolRef;
        this.titleSearch = titleSearch;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DocumentReference getUserId() {
        return userId;
    }

    public void setUserId(DocumentReference userId) {
        this.userId = userId;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public DocumentReference getSchoolClass() {
        return schoolClass;
    }

    public void setClass(DocumentReference schoolClass) {
        this.schoolClass = schoolClass;
    }

    public DocumentReference getSchool() {
        return school;
    }

    public void setSchool(DocumentReference school) {
        this.school = school;
    }

    public ArrayList<TaskAttachments> getTaskDocuments() {
        return taskDocuments;
    }

    public void setTaskDocuments(ArrayList<TaskAttachments> taskDocuments) {
        this.taskDocuments = taskDocuments;
    }

    public void setSchoolClass(DocumentReference schoolClass) {
        this.schoolClass = schoolClass;
    }

    public Integer getNotificationBefore() {
        return notificationBefore;
    }

    public void setNotificationBefore(Integer notificationBefore) {
        this.notificationBefore = notificationBefore;
    }

    public String getTitleSearch() {
        return titleSearch;
    }

    public void setTitleSearch(String titleSearch) {
        this.titleSearch = titleSearch;
    }
}
