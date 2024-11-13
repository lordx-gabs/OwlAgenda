package com.example.owlagenda.ui.classesschools;

public class ClassModel {
    private String idClass;
    private String nameClass;
    private String period;
    private int numberStudents;
    private String schoolName;

    public ClassModel(String idClass, String nameClass, String period, int numberStudents, String schoolName) {
        this.idClass = idClass;
        this.nameClass = nameClass;
        this.period = period;
        this.numberStudents = numberStudents;
        this.schoolName = schoolName;
    }

    public String getIdClass() {
        return idClass;
    }

    public void setIdClass(String idClass) {
        this.idClass = idClass;
    }

    public String getNameClass() {
        return nameClass;
    }

    public void setNameClass(String nameClass) {
        this.nameClass = nameClass;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getNumberStudents() {
        return numberStudents;
    }

    public void setNumberStudents(int numberStudents) {
        this.numberStudents = numberStudents;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }
}
