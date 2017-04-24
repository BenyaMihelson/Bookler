package com.mitlosh.bookplayer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mitlosh.bookplayer.App;
import com.mitlosh.bookplayer.utils.PrefUtils;

import javax.inject.Inject;

public class SplashActivity extends AppCompatActivity {

    @Inject
    PrefUtils prefUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.getAppComponent().inject(this);

        //setContentView(R.layout.activity_splash);

        if(hasAuthToken()){
            startActivity(new Intent(this, MainActivity.class));
        }else{
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }

    private boolean hasAuthToken() {
        return prefUtils.getAuthToken() != null;
    }
}
