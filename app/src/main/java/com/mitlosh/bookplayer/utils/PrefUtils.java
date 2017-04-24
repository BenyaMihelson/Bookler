package com.mitlosh.bookplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mitlosh.bookplayer.model.Album;

import java.util.ArrayList;
import java.util.List;

public class PrefUtils {

    private static final String PREF_NAME = "book_player_prefs";
    private static final String PREF_AUTH_TOKEN = "auth_token";
    private static final String PREF_PHONE = "phone";
    private static final String HISTORY_PREF = "history";
    private static final String FREE_SUBSCR_PREF = "free_subscr";

    private final SharedPreferences preferences;

    public PrefUtils(Context ctx){
        preferences = getPreferences(ctx);
    }

    public static SharedPreferences getPreferences(Context ctx){
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void storeAuthToken(String token) {
        preferences.edit()
                .putString(PREF_AUTH_TOKEN, token)
                .apply();
    }

    public String getAuthToken() {
        return preferences.getString(PREF_AUTH_TOKEN, null);
    }

    public void storePhone(String phone) {
        preferences.edit()
                .putString(PREF_PHONE, phone)
                .apply();
    }

    public String getPhone() {
        return preferences.getString(PREF_PHONE, "");
    }

    public List<Album> getHistory() {
        List<Album> list = new ArrayList<>();
        String historyStr = preferences.getString(HISTORY_PREF, "[]");
        JsonArray historyArr = new JsonParser().parse(historyStr).getAsJsonArray();
        Gson gson = getGson();
        for(JsonElement albumEl : historyArr){
            list.add(gson.fromJson(albumEl, Album.class));
        }
        return list;
    }

    public void storeHistory(List<Album> history) {
        JsonArray historyArr = getGson().toJsonTree(history).getAsJsonArray();
        preferences.edit()
            .putString(HISTORY_PREF, historyArr.toString())
            .apply();
    }

    public void addToHistory(Album album) {
        List<Album> history = getHistory();
        Album historyAlb = null;
        for(Album alb : history){
            if(alb.getId() == album.getId()){
                historyAlb = alb;
                break;
            }
        }
        if(historyAlb != null){
            if(historyAlb.getStatus() != Album.STATUS_LISTENED){
                historyAlb.setStatus(album.getStatus());
            }
            historyAlb.setLastListened(System.currentTimeMillis() / 1000);
        }else{
            album.setLastListened(System.currentTimeMillis() / 1000);
            history.add(album);
        }
        storeHistory(history);
    }

    public void setFreeSubscrExpired(long free_expired) {
        preferences.edit().putLong(FREE_SUBSCR_PREF, free_expired).apply();
    }

    public boolean isFreeSubscrFirstTime() {
        return preferences.getLong(FREE_SUBSCR_PREF, -1) == -1;
    }

    public boolean isFreeSubscrExtended(long free_expired) {
        return preferences.getLong(FREE_SUBSCR_PREF, -1) != free_expired;
    }


    private static Gson getGson() {
        return new GsonBuilder().create();
    }

}
