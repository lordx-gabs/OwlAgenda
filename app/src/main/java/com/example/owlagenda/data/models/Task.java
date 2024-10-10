package com.example.owlagenda.data.models;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

public class Task {
    private String id;
    private DocumentReference userId;
    private String title;
    private String description;
    private String date;
    private DocumentReference schoolClass;
    private DocumentReference school;
    private String tag;
    private boolean isCompleted;
    private Integer notificationBefore;
    private ArrayList<TaskAttachments> taskDocuments;

    public Task(DocumentReference userId, String title, String description, String date, DocumentReference school, DocumentReference schoolClass, String tag, ArrayList<TaskAttachments> taskDocuments, Integer notificationBefore) {
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
    }

    public Task() {
    }

    public Task(String taskName, String taskDescription, String taskType, String taskDate, DocumentReference classRef, DocumentReference schoolRef) {
        this.title = taskName;
        this.description = taskDescription;
        this.date = taskDate;
        this.schoolClass = classRef;
        this.tag = taskType;
        this.school = schoolRef;
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

    //    public static List<Task> generateTask() {
//        ArrayList<Task> tasks = new ArrayList<>();
//        // Suponha que `currentMonth` seja uma instância de `YearMonth`.
//        YearMonth currentMonth = YearMonth.now(); // Inicialize conforme necessário
//
//        LocalDate date = currentMonth.minusMonths(1).atDay(9);
//
//        tasks.add(
//                new Task(
//                        "tarefaid",
//                        "729394",
//                        "Teste",
//                        "testando",
//                        date,
//                        "Etec Itaquera1",
//                        "1° dsb",
//                    new Tag("1234", "prova", R.color.example_3_blue),
//                        false,
//                        ""
//                )
//        );
//
//        date = currentMonth.minusMonths(1).atDay(15);
//
//        tasks.add(
//                new Task(
//                        "tarefaid",
//                        "729394",
//                        "Teste",
//                        "testando",
//                        date,
//                        "Etec Itaquera1",
//                        "2° dsb",
//                        new Tag("1234", "prova", R.color.cor_primaria),
//                        false,
//                        ""
//                )
//        );
//
//        date = currentMonth.minusMonths(1).atDay(19);
//
//        tasks.add(
//                new Task(
//                        "tarefaid",
//                        "729394",
//                        "Teste",
//                        "testando",
//                        date,
//                        "Etec Itaquera1",
//                        "3° dsb",
//                        new Tag("1234", "prova", R.color.botao_cor),
//                        false,
//                        ""
//                )
//        );
//
//        tasks.add(
//                new Task(
//                        "tarefaid",
//                        "729394",
//                        "Teste",
//                        "testando",
//                        date,
//                        "Etec Itaquera1",
//                        "3° dsb",
//                        new Tag("1234", "prova", R.color.darkLilac),
//                        false,
//                        ""
//                )
//        );
//
//        return tasks;
//    }
}
