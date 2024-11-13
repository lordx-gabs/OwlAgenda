package com.example.owlagenda.data.models;

import com.google.firebase.firestore.DocumentReference;

public class SchoolClass {
    private String id;
    private DocumentReference userId;
    private DocumentReference schoolId;
    private String className;
    private String classNameSearch;
    private String period;
    private int numberOfStudents;

    public SchoolClass(String id, DocumentReference userId, DocumentReference schoolId, String className, String classNameSearch, String period, int numberOfStudents) {
        this.id = id;
        this.userId = userId;
        this.schoolId = schoolId;
        this.className = className;
        this.classNameSearch = classNameSearch;
        this.period = period;
        this.numberOfStudents = numberOfStudents;
    }

    public SchoolClass() {}

    public String getClassNameSearch() {
        return classNameSearch;
    }

    public void setClassNameSearch(String classNameSearch) {
        this.classNameSearch = classNameSearch;
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

    public DocumentReference getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(DocumentReference schoolId) {
        this.schoolId = schoolId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getNumberOfStudents() {
        return numberOfStudents;
    }

    public void setNumberOfStudents(int numberOfStudents) {
        this.numberOfStudents = numberOfStudents;
    }
}
