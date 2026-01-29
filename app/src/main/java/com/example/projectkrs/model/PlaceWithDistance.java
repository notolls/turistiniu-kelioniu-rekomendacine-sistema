package com.example.projectkrs.model;

import com.google.android.libraries.places.api.model.Place;

public class PlaceWithDistance {
    private Place place;
    private double distance;
    private String status; // "want", "visited", "all"

    // Konstruktoras
    public PlaceWithDistance(Place place, double distance) {
        this.place = place;
        this.distance = distance;
        this.status = "all"; // default
    }

    // Getteriai
    public Place getPlace() {
        return place;
    }

    public double getDistance() {
        return distance;
    }

    public String getStatus() {
        return status;
    }

    // Setteris statusui
    public void setStatus(String status) {
        this.status = status;
    }
}
