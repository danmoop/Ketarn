package com.danmoop.novanode.MainApplication.Model;

import java.util.UUID;

public class ChatMessage
{
    private String text;
    private String sender;
    private String timeStamp;
    private String key;

    public ChatMessage(String text, String sender, String timeStamp)
    {
        this.text = text;
        this.sender = sender;
        this.timeStamp = timeStamp;
        this.key = UUID.randomUUID().toString();
    }

    public String getKey()
    {
        return key;
    }

    public String getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public String getSender()
    {
        return sender;
    }

    public void setSender(String sender)
    {
        this.sender = sender;
    }
}