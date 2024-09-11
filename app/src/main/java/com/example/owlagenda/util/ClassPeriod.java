package com.example.owlagenda.util;

public enum ClassPeriod {
    MORNING("Manhã"),
    AFTERNOON("Tarde"),
    NIGHT("Noite");

    private String period;

    ClassPeriod(String s) {
        period = s;
    }

    public String getPeriod() {
        return period;
    }
}
