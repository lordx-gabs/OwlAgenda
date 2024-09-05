package com.example.owlagenda.data.models;

import com.example.owlagenda.ui.selene.Message;

import java.util.ArrayList;

public class User {
    private String id;
    private String name;
    private String surname;
    private String password;
    private String email;
    private String birthdate;
    private String gender;
    private String urlProfilePhoto;
    private Long phoneNumber;
    private ArrayList<Message> historyMessage;

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrlProfilePhoto() {
        return urlProfilePhoto;
    }

    public void setUrlProfilePhoto(String urlProfilePhoto) {
        this.urlProfilePhoto = urlProfilePhoto;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public ArrayList<Message> getHistoryMessage() {
        return historyMessage;
    }

    public void setHistoryMessage(ArrayList<Message> historyMessage) {
        this.historyMessage = historyMessage;
    }
}
