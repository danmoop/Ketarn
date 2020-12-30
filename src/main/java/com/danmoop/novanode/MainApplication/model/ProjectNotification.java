package com.danmoop.novanode.MainApplication.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class ProjectNotification {
    private String createdOn;
    private String authorName;
    private String text;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public ProjectNotification(String authorName, String text) {
        this.authorName = authorName;
        this.text = text;
        this.createdOn = dtf.format(LocalDateTime.now());
    }
}