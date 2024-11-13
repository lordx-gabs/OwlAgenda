package com.example.owlagenda.ui.classesschools;

public class SchoolModel {
    private String idSchool;
    private String schoolName;

    public SchoolModel(String schoolId, String schoolName) {
        this.idSchool = schoolId;
        this.schoolName = schoolName;
    }

    public String getIdSchool() {
        return idSchool;
    }

    public void setIdSchool(String idSchool) {
        this.idSchool = idSchool;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }
}
