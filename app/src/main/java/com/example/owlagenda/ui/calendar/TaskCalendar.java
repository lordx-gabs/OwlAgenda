package com.example.owlagenda.ui.calendar;

public class TaskCalendar {
    private String id;
    private String nameTask;
    private String schoolClass;
    private String date;
    private String tag;

    public TaskCalendar(String id, String nameTask, String schoolClass, String date, String tag) {
        this.id = id;
        this.nameTask = nameTask;
        this.schoolClass = schoolClass;
        this.date = date;
        this.tag = tag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameTask() {
        return nameTask;
    }

    public void setNameTask(String nameTask) {
        this.nameTask = nameTask;
    }

    public String getSchoolClass() {
        return schoolClass;
    }

    public void setSchoolClass(String schoolClass) {
        this.schoolClass = schoolClass;
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
}
