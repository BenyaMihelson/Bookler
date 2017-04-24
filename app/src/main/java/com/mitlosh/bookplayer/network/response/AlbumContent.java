package com.mitlosh.bookplayer.network.response;

import com.mitlosh.bookplayer.model.Album;
import com.mitlosh.bookplayer.model.Track;

import java.util.List;

public class AlbumContent extends BaseData{

    private Album album;
    private List<Track> content;

    public Album getAlbum() {
        return album;
    }

    public List<Track> getContent() {
        return content;
    }
}
