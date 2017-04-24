package com.mitlosh.bookplayer.model;

import java.io.Serializable;

public class Category implements Serializable{

    private int id;
    private String title;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
