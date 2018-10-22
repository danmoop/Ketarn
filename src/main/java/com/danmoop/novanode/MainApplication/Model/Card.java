package com.danmoop.novanode.MainApplication.Model;

import com.danmoop.novanode.MainApplication.Service.Encrypt;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class Card
{
    private String text;
    private String projectName;
    private String key;

    public Card(String text, String projectName) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        this.text = text;
        this.projectName = projectName;
        this.key = generateKey();
    }

    private String generateKey() throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        String possible = "qwertyuiopasdfghjklzxcvbnm1234567890";

        String result = "";

        for (int i = 0; i < 15; i++)
        {
            result += possible.charAt((int) Math.floor(Math.random() * possible.length()));
        }

        return Encrypt.toMD5(result);
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