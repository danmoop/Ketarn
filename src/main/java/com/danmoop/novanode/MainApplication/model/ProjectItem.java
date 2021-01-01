package com.danmoop.novanode.MainApplication.model;

import lombok.Data;

import java.util.UUID;

@Data
public class ProjectItem {

    private String text;
    private String projectName;
    private String key;

    public ProjectItem(String text, String projectName) {
        this.text = text;
        this.projectName = projectName;
        this.key = generateKey();
    }

    private String generateKey() {
        return UUID.randomUUID().toString();
    }
}
