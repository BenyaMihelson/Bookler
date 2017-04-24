package com.mitlosh.bookplayer.ui.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.mitlosh.bookplayer.App;
import com.mitlosh.bookplayer.R;
import com.mitlosh.bookplayer.databinding.ActivityLoginBinding;
import com.mitlosh.bookplayer.network.RestAPI;
import com.mitlosh.bookplayer.utils.LogUtils;
import com.mitlosh.bookplayer.utils.PrefUtils;
import com.mitlosh.bookplayer.viewmodel.LoginViewModel;

import javax.inject.Inject;

public class LoginActivity extends AppCompatActivity implements LoginViewModel.LoginInterface {

    private static final String TAG = "LoginActivity";

    @Inject
    PrefUtils prefUtils;
    @Inject
    RestAPI restAPI;

    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogUtils.d(TAG, "onCreate");

        App.getAppComponent().inject(this);

        loginViewModel = new LoginViewModel(false, restAPI, prefUtils, this);

        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setViewModel(loginViewModel);
    }

    @Override
    public void onSuccess() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onFail(String error) {
        Toast.makeText(getApplicationContext(), getString(R.string.error_login)
                + " (" + error + ")", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        if(loginViewModel != null) loginViewModel.onDestroy();
        super.onDestroy();
    }
}
