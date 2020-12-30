package com.danmoop.novanode.MainApplication.model;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class ChatMessage {
    private String text;
    private String sender;
    private String timeStamp;
    private String key;

    public ChatMessage(String text, String sender) {
        this.text = text;
        this.sender = sender;
        this.timeStamp = new Date().toString();
        this.key = UUID.randomUUID().toString();
    }
}