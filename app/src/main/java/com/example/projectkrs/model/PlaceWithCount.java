package com.example.projectkrs.model;

public class PlaceWithCount {
    private String name;
    private int count;

    public PlaceWithCount(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public String getName() { return name; }
    public int getCount() { return count; }
}
