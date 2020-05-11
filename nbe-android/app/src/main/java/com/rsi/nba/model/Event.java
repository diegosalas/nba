package com.rsi.nba.model;

public class Event {

    //Atributos
    private String event;
    private Extra extra;
    private Callback callback;

    //Constructor
    public Event() {
    }

    //Getters
    public String getEvent() {
        return event;
    }

    public Extra getExtra() {
        return extra;
    }

    public Callback getCallback() {
        return callback;
    }

    //Setters
    public void setEvent(String event) {
        this.event = event;
    }

    public void setExtra(Extra extra) {
        this.extra = extra;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    //ToString
    @Override
    public String toString() {
        return "{" +
                "event:'" + event + '\'' +
                ", extra:" + extra +
                '}';
    }
}
