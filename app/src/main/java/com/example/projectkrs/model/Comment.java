package com.example.projectkrs.model;

public class Comment {
    private String author;
    private String text;
    private float rating;

    public Comment() {}

    public Comment(String author, String text, float rating) {
        this.author = author;
        this.text = text;
        this.rating = rating;
    }

    public String getAuthor() { return author; }
    public String getText() { return text; }
    public float getRating() { return rating; }

    public void setAuthor(String author) { this.author = author; }
    public void setText(String text) { this.text = text; }
    public void setRating(float rating) { this.rating = rating; }
}
