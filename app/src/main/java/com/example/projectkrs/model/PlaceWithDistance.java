package com.example.projectkrs.model;

import com.google.android.libraries.places.api.model.Place;

public class PlaceWithDistance {
    private Place place;
    private double distance;

    public PlaceWithDistance(Place place, double distance) {
        this.place = place;
        this.distance = distance;
    }

    public Place getPlace() {
        return place;
    }

    public double getDistance() {
        return distance;
    }
}