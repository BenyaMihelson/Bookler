package com.mitlosh.bookplayer.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Album implements Serializable{

    public final static int STATUS_LISTENING = 0;
    public final static int STATUS_LISTENED = 1;
    public final static int STATUS_NO_LISTENED = 2;

    private int id;
    private int position;
    private String title;
    private String author;
    private String description;
    @SerializedName("description_full")
    private String descriptionFull;
    private String image;
    @SerializedName("new")
    private boolean isNew;
    private long time;
    private int status = STATUS_NO_LISTENED;
    private long lastListened;

    public String getTopButtonName() {
        return topButtonName;
    }

    public void setTopButtonName(String topButtonName) {
        this.topButtonName = topButtonName;
    }

    private String topButtonName;


    public int getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDescriptionFull() {
        return descriptionFull;
    }

    public boolean isNew() {
        return isNew;
    }

    public long getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Album album = (Album) o;

        return id == album.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public String getAuthor() {
        return author;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getLastListened() {
        return lastListened;
    }

    public void setLastListened(long lastListened) {
        this.lastListened = lastListened;
    }
}
