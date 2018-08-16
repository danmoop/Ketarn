package com.danmoop.novanode.MainApplication.Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProjectNotification
{
    private String createdOn;
    private String authorName;
    private String text;

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public ProjectNotification(String authorName, String text)
    {
        this.authorName = authorName;
        this.text = text;

        this.createdOn = dtf.format(LocalDateTime.now());
    }

    public String getAuthorName()
    {
        return authorName;
    }

    public void setAuthorName(String authorName)
    {
        this.authorName = authorName;
    }

    public String getText()
    {
        return text;
    }

    public String getCreatedOn()
    {
        return createdOn;
    }

    public void setText(String text)
    {
        this.text = text;
    }
}