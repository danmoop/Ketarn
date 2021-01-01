package com.danmoop.novanode.MainApplication.model;

import lombok.Data;

import java.util.Date;

@Data
public class ProjectNotification {

    private String createdOn;
    private String authorName;
    private String text;

    public ProjectNotification(String authorName, String text) {
        this.authorName = authorName;
        this.text = text;
        this.createdOn = new Date().toString();
    }
}