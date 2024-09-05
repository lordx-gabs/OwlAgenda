package com.example.owlagenda.data.models;

import com.example.owlagenda.R;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private String id;
    private String userId;
    private String title;
    private String description;
    private LocalDate date;
    private String TaskClass;
    private String school;
    private Tag tag;
    private boolean isCompleted;

    public Task(String id,String userId, String title, String description, LocalDate date, String school, String taskClass, Tag tag, boolean isCompleted) {
        this.userId = userId;
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        TaskClass = taskClass;
        this.tag = tag;
        this.school = school;
        this.isCompleted = isCompleted;
    }

    public Task() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTaskClass() {
        return TaskClass;
    }

    public void setTaskClass(String taskClass) {
        TaskClass = taskClass;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public static List<Task> generateTask() {
        ArrayList<Task> tasks = new ArrayList<>();
        // Suponha que `currentMonth` seja uma instância de `YearMonth`.
        YearMonth currentMonth = YearMonth.now(); // Inicialize conforme necessário

        LocalDate date = currentMonth.minusMonths(1).atDay(9);

        tasks.add(
                new Task(
                        "tarefaid",
                        "729394",
                        "Teste",
                        "testando",
                        date,
                        "Etec Itaquera1",
                        "1° dsb",
                    new Tag("1234", "prova", R.color.example_3_blue),
                        false
                )
        );

        date = currentMonth.minusMonths(1).atDay(15);

        tasks.add(
                new Task(
                        "tarefaid",
                        "729394",
                        "Teste",
                        "testando",
                        date,
                        "Etec Itaquera1",
                        "2° dsb",
                        new Tag("1234", "prova", R.color.cor_primaria),
                        false
                )
        );

        date = currentMonth.minusMonths(1).atDay(19);

        tasks.add(
                new Task(
                        "tarefaid",
                        "729394",
                        "Teste",
                        "testando",
                        date,
                        "Etec Itaquera1",
                        "3° dsb",
                        new Tag("1234", "prova", R.color.botao_cor),
                        false
                )
        );

        tasks.add(
                new Task(
                        "tarefaid",
                        "729394",
                        "Teste",
                        "testando",
                        date,
                        "Etec Itaquera1",
                        "3° dsb",
                        new Tag("1234", "prova", R.color.darkLilac),
                        false
                )
        );

        return tasks;
    }
}
