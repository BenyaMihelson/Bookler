package com.mitlosh.bookplayer.network.response;

public class AuthData extends BaseData {

    private String token;
    private String name;

    public String getToken() {
        return token;
    }

    public String getName() {
        return name;
    }
}
