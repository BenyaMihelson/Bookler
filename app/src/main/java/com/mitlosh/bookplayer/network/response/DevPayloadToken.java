package com.mitlosh.bookplayer.network.response;

import com.google.gson.annotations.SerializedName;
import com.mitlosh.bookplayer.model.Album;
import com.mitlosh.bookplayer.model.Track;

import java.util.List;

public class DevPayloadToken extends BaseData{

    @SerializedName("devpayload_token")
    private String devpayload;

    public String getDevpayload() {
        return devpayload;
    }
}
