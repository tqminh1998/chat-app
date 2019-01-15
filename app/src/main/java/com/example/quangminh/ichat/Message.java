package com.example.quangminh.ichat;

public class Message {
    private String from, message, type;

    public Message(String from, String message, String type) {
        this.from = from;
        this.message = message;
        this.type = type;
    }

    public Message(){

    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
