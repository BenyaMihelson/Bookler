package com.mitlosh.bookplayer.network.response;

import com.google.gson.annotations.SerializedName;
import com.mitlosh.bookplayer.model.Album;

import java.util.List;

public class AlbumList extends BaseData{

    private List<Album> albums;

    @SerializedName("new_albums")
    private List<Album> newAlbums;

    public List<Album> getAlbums() {
        return albums;
    }

    public List<Album> getNewAlbums() {
        return newAlbums;
    }
}
