package com.example.owlagenda.ui.calendar;

public class TaskCalendar {
    private String id;
    private String nameTask;
    private String schoolClass;
    private String date;
    private String tag;
    private boolean isCompleted;
    private boolean typeCalendarUser;

    public TaskCalendar() {
    }

    public TaskCalendar(String id, String nameTask, String schoolClass, String date, String tag, boolean isCompleted) {
        this.id = id;
        this.nameTask = nameTask;
        this.schoolClass = schoolClass;
        this.date = date;
        this.tag = tag;
        this.isCompleted = isCompleted;
    }

    public TaskCalendar(String nameTask, String date, String tag, boolean typeCalendarUser) {
        this.nameTask = nameTask;
        this.date = date;
        this.tag = tag;
        this.typeCalendarUser = typeCalendarUser;
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

    public boolean isTypeCalendarUser() {
        return typeCalendarUser;
    }

    public void setTypeCalendarUser(boolean typeCalendarUser) {
        this.typeCalendarUser = typeCalendarUser;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
