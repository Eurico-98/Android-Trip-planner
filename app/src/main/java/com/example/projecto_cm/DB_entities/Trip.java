package com.example.projecto_cm.DB_entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Trip {

    private String title;
    private String start_date;
    private String end_date;
    private ArrayList<String> locations; // keys will be locations values will be a list of points of interest
    private List invited_friend;

    public Trip() {}

    public Trip(String title, String start_date, String end_date, ArrayList<String> locations) {
        this.title = title;
        this.start_date = start_date;
        this.end_date = end_date;
        this.locations = locations;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStar_date() {
        return start_date;
    }

    public void setStar_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public ArrayList<String> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<String> locations) {this.locations = locations;}

    public List getInvited_friend() {
        return invited_friend;
    }

    public void setInvited_friend(List invited_friend) {
        this.invited_friend = invited_friend;
    }
}
