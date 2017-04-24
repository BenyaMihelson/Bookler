package com.mitlosh.bookplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.mitlosh.bookplayer.R;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final String PLAY_MARKET_LINK = "https://play.google.com/store/apps/details?id=";

    public static Intent createShareAppIntent(Context context) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.app_name));
        String sAux = context.getString(R.string.app_share_message, getPlayMarketLink(context));
        i.putExtra(Intent.EXTRA_TEXT, sAux);
        return Intent.createChooser(i, null);
    }

    public static String getPlayMarketLink(Context context){
        return PLAY_MARKET_LINK + context.getPackageName();
    }

    public static boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile("^.+@.+\\..+$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static void hideKeyboard(Activity activity) {
        View focusView = activity.getCurrentFocus();
        if (focusView == null) {
            focusView = new View(activity);
        }
        hideKeyboard(activity, focusView);
    }

    public static void hideKeyboard(Context context, View focusView) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
    }

    public static boolean isOnline(Context ctx) {
        ConnectivityManager cm =
                (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static String getTimeOfDay(Context ctx) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        String tod = "";
        if(hour >= 5 && hour < 12){
            tod = ctx.getString(R.string.morning);
        }else if(hour >= 12 && hour < 17){
            tod = ctx.getString(R.string.day);
        }else if(hour >= 17 && hour < 22){
            tod = ctx.getString(R.string.evening);
        }else if(hour >= 22 && hour < 5){
            tod = ctx.getString(R.string.night);
        }
        return tod;
    }
}
