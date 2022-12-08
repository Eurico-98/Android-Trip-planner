package com.example.projecto_cm.DB_entities;

import android.net.Uri;

public class MyUser {

    private String username;
    private String email;
    private String password;
    private Uri filePath; // for the profile image location
    private String fullName;

    public MyUser() {}

    /**
     * constructor for user class
     * @param username
     * @param email
     * @param password
     */
    public MyUser(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Uri getFilePath() {
        return filePath;
    }

    public void setFilePath(Uri filePath) {
        this.filePath = filePath;
    }

    public String getFullName() { return fullName; }

    public void setFullName(String fullName) { this.fullName = fullName; }
}
