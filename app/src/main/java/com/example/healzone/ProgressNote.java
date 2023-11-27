package com.example.healzone;

import com.google.firebase.Timestamp;

public class ProgressNote {

    String howAreYou;
    String struggles;
    String gratitude;
    String extraNotes;
    String rate;
    com.google.firebase.Timestamp timestamp;

    public ProgressNote() {
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }


    public String getHowAreYou() {
        return howAreYou;
    }

    public void setHowAreYou(String howAreYou) {
        this.howAreYou = howAreYou;
    }

    public String getStruggles() {
        return struggles;
    }

    public void setStruggles(String struggles) {
        this.struggles = struggles;
    }

    public String getGratitude() {
        return gratitude;
    }

    public void setGratitude(String gratitude) {
        this.gratitude = gratitude;
    }

    public String getExtraNotes() {
        return extraNotes;
    }

    public void setExtraNotes(String extraNotes) {
        this.extraNotes = extraNotes;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }
}
