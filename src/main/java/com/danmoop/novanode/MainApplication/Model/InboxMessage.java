package com.danmoop.novanode.MainApplication.Model;
import com.danmoop.novanode.MainApplication.Service.Encrypt;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

@Document(collection = "messages")
public class InboxMessage
{
    private String text;

    private String authorName;

    private String type;

    private String timeDate;

    private String messageKey;

    private String details;

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public InboxMessage(String text, String authorName, String type) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        this.text = text;
        this.authorName = authorName;
        this.messageKey = generateMessageKey();
        this.timeDate = dtf.format(LocalDateTime.now());
        this.details = "none";
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimeDate() {
        return timeDate;
    }

    public void setTimeDate(String timeDate) {
        this.timeDate = timeDate;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    private String generateMessageKey() throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        String finalString = this.authorName + this.text + this.timeDate + UUID.randomUUID();

        return Encrypt.toMD5(finalString);
    }
}