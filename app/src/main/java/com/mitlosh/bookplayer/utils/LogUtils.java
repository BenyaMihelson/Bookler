package com.mitlosh.bookplayer.utils;

import android.util.Log;

import com.mitlosh.bookplayer.Constants;

public class LogUtils {

    public static void d(String tag, String msg) {
        if(Constants.IS_DEBUG) Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        if(Constants.IS_DEBUG) Log.e(tag, msg);
    }
}
