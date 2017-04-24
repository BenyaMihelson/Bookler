package com.mitlosh.bookplayer.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Track implements Serializable{

    private int id;
    @SerializedName("album_id")

    private int albumId;
    private String title;

    @SerializedName("filename")
    private String fileName;

    private long duration;
    private long time;
    private String url;

    public int getId() {
        return id;
    }

    public int getAlbumId() {
        return albumId;
    }

    public String getTitle() {
        return title;
    }

    public String getFileName() {
        return fileName;
    }

    public long getDuration() {
        return duration;
    }

    public long getTime() {
        return time;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Track track = (Track) o;

        if (id != track.id) return false;
        return albumId == track.albumId;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + albumId;
        return result;
    }
}
