package com.example.owlagenda.data.models;

public class TaskDay {
    private String idTaskDay, titleTaskDay, ClassTaskDay, tagTaskDay, dateTaskDay;
    private boolean checkTaskDay;

    public TaskDay(String idTaskDay, String titleTaskDay, String descriptionTaskDay, String tagTaskDay, String dateTaskDay, boolean checkTaskDay) {
        this.titleTaskDay = titleTaskDay;
        this.ClassTaskDay = descriptionTaskDay;
        this.tagTaskDay = tagTaskDay;
        this.dateTaskDay = dateTaskDay;
        this.checkTaskDay = checkTaskDay;
        this.idTaskDay = idTaskDay;
    }

    public String getIdTaskDay() {
        return idTaskDay;
    }

    public void setIdTaskDay(String idTaskDay) {
        this.idTaskDay = idTaskDay;
    }

    public String getTitleTaskDay() {
        return titleTaskDay;
    }

    public void setTitleTaskDay(String titleTaskDay) {
        this.titleTaskDay = titleTaskDay;
    }

    public String getClassTaskDay() {
        return ClassTaskDay;
    }

    public void setClassTaskDay(String descriptionTaskDay) {
        this.ClassTaskDay = descriptionTaskDay;
    }

    public String getTagTaskDay() {
        return tagTaskDay;
    }

    public void setTagTaskDay(String tagTaskDay) {
        this.tagTaskDay = tagTaskDay;
    }

    public String getDateTaskDay() {
        return dateTaskDay;
    }

    public void setDateTaskDay(String dateTaskDay) {
        this.dateTaskDay = dateTaskDay;
    }

    public boolean isCheckTaskDay() {
        return checkTaskDay;
    }

    public void setCheckTaskDay(boolean checkTaskDay) {
        this.checkTaskDay = checkTaskDay;
    }
}
