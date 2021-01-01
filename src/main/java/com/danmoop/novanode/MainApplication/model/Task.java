package com.danmoop.novanode.MainApplication.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class Task {

    private String authorName;
    private String text;
    private String executor;
    private String deadline;
    private String project;
    private String key;
    private String createdOn;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public Task(String authorName, String text, String executor, String deadline, String project) {
        this.authorName = authorName;
        this.text = text;
        this.executor = executor;
        this.key = generateKey();
        this.deadline = deadline;
        this.project = project;

        this.createdOn = dtf.format(LocalDateTime.now());
    }

    private String generateKey() {
        String possible = "1234567890";
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            builder.append(possible.charAt((int) Math.floor(Math.random() * possible.length())));
        }

        return "#" + builder.toString();
    }
}
