package com.mitlosh.bookplayer.viewmodel;

import android.databinding.ObservableBoolean;
import android.view.View;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.AuthConfig;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsOAuthSigning;
import com.digits.sdk.android.DigitsSession;
import com.mitlosh.bookplayer.network.RestAPI;
import com.mitlosh.bookplayer.network.response.ApiResponse;
import com.mitlosh.bookplayer.network.response.AuthData;
import com.mitlosh.bookplayer.utils.LogUtils;
import com.mitlosh.bookplayer.utils.PrefUtils;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel {

    private static final String TAG = "LoginViewModel";
    public final ObservableBoolean showProgress = new ObservableBoolean();
    private LoginInterface callback;
    private PrefUtils prefUtils;
    private RestAPI restAPI;
    private AuthCallback authCallback;

    public LoginViewModel(boolean showProgress, RestAPI restAPI, PrefUtils prefUtils, LoginInterface callback) {
        LogUtils.d(TAG, "init");
        this.showProgress.set(showProgress);
        this.restAPI = restAPI;
        this.prefUtils = prefUtils;
        this.callback = callback;
        authCallback = new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                LogUtils.d(TAG, "Sign in with Digits success");
                String phone = session.getPhoneNumber();
                TwitterAuthConfig authConfig = TwitterCore.getInstance().getAuthConfig();
                TwitterAuthToken authToken = session.getAuthToken();
                DigitsOAuthSigning oauthSigning = new DigitsOAuthSigning(authConfig, authToken);
                Map<String, String> authHeaders = oauthSigning.getOAuthEchoHeadersForVerifyCredentials();
                String digitsService = authHeaders.get("X-Auth-Service-Provider");
                String digitsAuth = authHeaders.get("X-Verify-Credentials-Authorization");
                auth(phone, digitsService, digitsAuth);
            }

            @Override
            public void failure(DigitsException exception) {
                LogUtils.d(TAG, "Sign in with Digits failure");
                onError(exception.getLocalizedMessage());
            }
        };
    }

    public void signIn(View view){
        LogUtils.d(TAG, "Sign in");
        showProgress.set(true);
        Digits.clearActiveSession();
        final AuthConfig.Builder digitsAuthConfigBuilder = new AuthConfig.Builder().withAuthCallBack(authCallback);
        Digits.authenticate(digitsAuthConfigBuilder.build());
    }

    private void auth(final String phone, String digitsService, String digitsAuth) {
        LogUtils.d(TAG, "auth phone=" + phone +" digitsService=" + digitsService + " digitsAuth=" + digitsAuth);
        restAPI.auth(phone, digitsService, digitsAuth).enqueue(new Callback<ApiResponse<AuthData>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthData>> call, Response<ApiResponse<AuthData>> response) {
                if (response.isSuccessful()) {
                    if(response.body().isSuccess()){
                        AuthData res = response.body().getData();
                        prefUtils.storeAuthToken(res.getToken());
                        prefUtils.storePhone(phone);
                        if(callback != null) callback.onSuccess();
                    }else{
                        onError(response.body().getData().getMessage());
                    }
                } else {
                    onError(response.message());
                }
                showProgress.set(false);
                LogUtils.d(TAG, "auth onResponse");
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthData>> call, Throwable t) {
                LogUtils.d(TAG, "auth onFailure");
                onError(t.getLocalizedMessage());
            }
        });
    }

    private void onError(String error) {
        showProgress.set(false);
        if(callback != null) callback.onFail(error);
    }

    public void onDestroy() {
        callback = null;
        prefUtils = null;
        restAPI = null;
        authCallback = null;
    }

    public interface LoginInterface{
        void onSuccess();
        void onFail(String error);
    }
}
