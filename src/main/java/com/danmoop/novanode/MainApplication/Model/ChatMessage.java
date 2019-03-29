package com.danmoop.novanode.MainApplication.Model;

public class ChatMessage
{
    private String text;
    private String sender;

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