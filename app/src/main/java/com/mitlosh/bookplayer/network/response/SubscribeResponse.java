package com.mitlosh.bookplayer.network.response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SubscribeResponse extends BaseData{

    private long expired;
    private long free_expired = -1;

    public long getExpired() {
        return expired;
    }

    public long getFree_expired() {
        return free_expired;
    }

    public String getExpiredString() {
        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd.MM", Locale.getDefault());
        return simpleDate.format(new Date(expired * 1000));
    }

    public String getFreeExpiredString() {
        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd.MM", Locale.getDefault());
        return simpleDate.format(new Date(free_expired * 1000));
    }

}
