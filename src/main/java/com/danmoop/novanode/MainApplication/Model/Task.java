package com.danmoop.novanode.MainApplication.Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task
{
    private String authorName;
    private String text;
    private String executor;
    private String deadline;
    private String project;
    private String key;

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private String createdOn;

    public Task(String authorName, String text, String executor, String deadline, String project)
    {
        this.authorName = authorName;
        this.text = text;
        this.executor = executor;
        this.key = generateKey();
        this.deadline = deadline;
        this.project = project;

        this.createdOn = dtf.format(LocalDateTime.now());
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getExecutor() {
        return executor;
    }

    public String getCreatedOn()
    {
        return createdOn;
    }

    public String getProject()
    {
        return project;
    }

    public String getKey()
    {
        return key;
    }

    public void setProject(String project)
    {
        this.project = project;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    private String generateKey()
    {
        String possible = "1234567890";

        String result = "";

        for (int i = 0; i < 5; i++)
        {
            result += possible.charAt((int) Math.floor(Math.random() * possible.length()));
        }

        return "#" + result;
    }
}