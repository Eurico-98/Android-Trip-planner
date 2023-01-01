package com.example.projecto_cm.DB_entities;

import android.net.Uri;

import java.util.List;

public class MyUser {

    private String username; // this is unique it is like the user ID
    private String password;
    private Uri photoPath; // for the profile image location
    private String fullName; // this is the user real name
    private List<Trip> my_trips;
    private List<String> my_friends;

    /**
     * constructor for user class
     * @param username
     * @param email
     * @param password
     */
    public MyUser(String username, String password, String fullName) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Uri getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(Uri photoPath) {
        this.photoPath = photoPath;
    }

    public String getFullName() { return fullName; }

    public void setFullName(String fullName) { this.fullName = fullName; }

    public List<Trip> getMy_trips() { return my_trips; }

    public void setMy_trips(List<Trip> my_trips) { this.my_trips = my_trips; }

    public List<String> getMy_friends() { return my_friends; }

    public void setMy_friends(List<String> my_friends) { this.my_friends = my_friends; }
}
