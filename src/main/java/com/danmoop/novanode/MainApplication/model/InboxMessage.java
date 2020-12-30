package com.danmoop.novanode.MainApplication.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.UUID;

@Data
@Document(collection = "messages")
public class InboxMessage {

    private String text;
    private String authorName;

    /**
     * @param type
     * inboxMessage - just a regular message with text
     * inboxRequest - message that looks like a request - includes "accept" & "reject" buttons. It is used when we send a request to join a project
     * inboxRequestToMember - same as inboxRequest, but when we send an invite to a user while being a project admin
     * inboxTaskRequest - a type of message that appears when you send a task review to a project's admins. Includes "accept" & "reject" buttons.
     */
    private String type;

    private String timeDate;

    /**
     * @param messageKey is very useful - it is the way we find message in array and manipulate with it
     */
    private String messageKey;

    private String details;

    public InboxMessage(String text, String authorName, String type) {
        this.text = text;
        this.authorName = authorName;
        this.messageKey = generateMessageKey();
        this.timeDate = new Date().toString();
        this.type = type;
        this.details = "";
    }

    private String generateMessageKey() {
        return UUID.randomUUID().toString();
    }
}