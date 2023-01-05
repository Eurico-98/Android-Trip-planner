package com.example.projecto_cm.DB_entities;

import java.util.ArrayList;
import java.util.List;

public class Trip {

    private String title;
    private String start_date;
    private String end_date;
    private ArrayList<String> locations;
    private ArrayList<String> trip_album;
    private ArrayList<String> invited_friends;
    private int election_duration;

    public Trip() {}

    public Trip(String title, String start_date, String end_date, ArrayList<String> locations) {
        this.title = title;
        this.start_date = start_date;
        this.end_date = end_date;
        this.locations = locations;
    }

    public void setTrip_album(ArrayList<String> trip_album) { this.trip_album = trip_album; }

    public ArrayList<String> getTrip_album() { return trip_album; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
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

    public ArrayList<String> getInvited_friends() {
        return invited_friends;
    }

    public void setInvited_friends(ArrayList<String> invited_friends) { this.invited_friends = invited_friends; }

    public int getElection_duration() {
        return election_duration;
    }

    public void setElection_duration(int election_duration) { this.election_duration = election_duration; }
}
