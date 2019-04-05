package com.danmoop.novanode.MainApplication.Model;

import com.danmoop.novanode.MainApplication.Service.Encrypt;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class ProjectItem
{
    private String text;
    private String projectName;
    private String key;

    public ProjectItem(String text, String projectName)
    {
        this.text = text;
        this.projectName = projectName;
        this.key = generateKey();
    }

    private String generateKey()
    {
        return UUID.randomUUID().toString();
    }

    public String getText() {
        return text;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getKey() {
        return key;
    }
}