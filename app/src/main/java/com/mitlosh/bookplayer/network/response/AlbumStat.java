package com.mitlosh.bookplayer.network.response;

/**
 * Created by Shipohvost on 19.01.2017.
 */

public class AlbumStat extends BaseData {
    String token;
    int albumId;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }
}
