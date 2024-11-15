package com.example.owlagenda.ui.calendar;

public class CalendarModel {
    private long id;
    private String displayName;
    private String accountName;
    private String accountType;

    public CalendarModel(long id, String displayName, String accountName, String accountType) {
        this.id = id;
        this.displayName = displayName;
        this.accountName = accountName;
        this.accountType = accountType;
    }

    // Getters e Setters
    public long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountType() {
        return accountType;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
