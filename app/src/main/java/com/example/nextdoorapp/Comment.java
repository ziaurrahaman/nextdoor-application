package com.example.nextdoorapp;

public class Comment {
    public String username, time, date, comment;


    public Comment() {
    }

    public Comment(String username, String time, String date, String comment) {
        this.username = username;
        this.time = time;
        this.date = date;
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
