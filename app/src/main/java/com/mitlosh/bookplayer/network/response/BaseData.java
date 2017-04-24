package com.mitlosh.bookplayer.network.response;

public class BaseData {

    public static final int CODE_SUBSCRIPTION_OVER = 7;

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
