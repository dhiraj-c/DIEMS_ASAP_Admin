package com.dietms.asapadmin.model;

import com.google.firebase.database.Exclude;

public class Upload {
    private String name;
    private String source;
    private String imageUrl;
    private String key;

    private String date;

    public Upload() {
        //empty constructor needed
    }

    public Upload(String name, String source, String imageUrl, String date) {
        this.date = date;
        if (name.trim().equals("")) {
            name = "Notice";
        }
        if (source.trim().equals("")) {
            source = "Unknown";
        }
        this.name = name;
        this.source = source;
        this.imageUrl = imageUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate() {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Exclude
    public String getKey() {
        return key;
    }

    @Exclude
    public void setKey(String key) {
        this.key = key;
    }
}
