package com.example.projecto_cm.DB_entities;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Trip {

    private String title;
    private Date star_date;
    private Date end_date;
    private HashMap locations_and_poi; // keys will be locations values will be a list of points of interest
    private List invited_friend;

    public Trip() {}

    public Trip(String title, Date star_date, Date end_date, HashMap locations_and_poi) {
        this.title = title;
        this.star_date = star_date;
        this.end_date = end_date;
        this.locations_and_poi = locations_and_poi;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getStar_date() {
        return star_date;
    }

    public void setStar_date(Date star_date) {
        this.star_date = star_date;
    }

    public Date getEnd_date() {
        return end_date;
    }

    public void setEnd_date(Date end_date) {
        this.end_date = end_date;
    }

    public HashMap getLocations_and_poi() {
        return locations_and_poi;
    }

    public void setLocations_and_poi(HashMap locations_and_poi) {
        this.locations_and_poi = locations_and_poi;
    }

    public List getInvited_friend() {
        return invited_friend;
    }

    public void setInvited_friend(List invited_friend) {
        this.invited_friend = invited_friend;
    }
}
