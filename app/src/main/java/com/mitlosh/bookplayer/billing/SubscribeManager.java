package com.mitlosh.bookplayer.billing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.mitlosh.bookplayer.App;
import com.mitlosh.bookplayer.Constants;
import com.mitlosh.bookplayer.R;
import com.mitlosh.bookplayer.network.RestAPI;
import com.mitlosh.bookplayer.network.response.ApiResponse;
import com.mitlosh.bookplayer.network.response.BaseData;
import com.mitlosh.bookplayer.network.response.DevPayloadToken;
import com.mitlosh.bookplayer.network.response.SubscribeResponse;
import com.mitlosh.bookplayer.utils.LogUtils;
import com.mitlosh.bookplayer.utils.PrefUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscribeManager {

    private static final String TAG = "SubscribeManager";
    private static final String PID = "subscr_1day";

    public static final int REQ_CODE = 1001;

    private Activity act;
    private IabHelper mHelper;
    private OnSubscribeResultListener listener;

    @Inject
    RestAPI restAPI;
    @Inject
    PrefUtils prefUtils;

    private ProgressDialog progress;

    public SubscribeManager(Activity act){
        this.act = act;
        App.getAppComponent().inject(this);
    }

    public void subscribe(final OnSubscribeResultListener listener) {
        this.listener = listener;
        mHelper = new IabHelper(act);
        showProgress(true);
        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(Constants.IS_DEBUG);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    LogUtils.d(TAG, "Problem setting up in-app billing: " + result);
                    onError(R.string.inapp_init_error);
                    return;
                }
                // IAB is fully set up. Now, let's get dev payload.
                LogUtils.d(TAG, "Setup successful. Querying inventory.");
                queryInventory();
            }
        });
    }

    private void queryInventory() {
        mHelper.queryInventoryAsync(false, new IabHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                LogUtils.d(TAG, "Query inventory finished.");

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // Is it a failure?
                if (result.isFailure()) {
                    LogUtils.d(TAG, "Failed to query inventory: " + result);
                    onError(R.string.inapp_query_error);
                    return;
                }

                LogUtils.d(TAG, "Query inventory was successful.");
                Purchase ownPurchase = inv.getPurchase(PID);
                if (ownPurchase != null){
                    LogUtils.d(TAG, "We have ownPurchase. Consuming it.");
                    consume(ownPurchase);
                }else{
                    getDevpayload();
                }
            }
        });
    }

    private void getDevpayload() {
        restAPI.getDevpayload(PID, prefUtils.getAuthToken()).enqueue(new Callback<ApiResponse<DevPayloadToken>>() {
            @Override
            public void onResponse(Call<ApiResponse<DevPayloadToken>> call, Response<ApiResponse<DevPayloadToken>> response) {
                String error = null;
                if (response.isSuccessful()) {
                    if(response.body().isSuccess()){
                        purchase(response.body().getData().getDevpayload());
                    }else{
                        error = response.body().getData().getMessage();
                    }
                } else {
                    error = response.message();
                }
                if(error != null){
                    LogUtils.d(TAG, "getDevpayload error=" + error);
                    onError(R.string.error_and_msg, error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DevPayloadToken>> call, Throwable t) {
                onError(R.string.error_and_msg, t.getMessage());
            }
        });
    }

    private void purchase(String devpayload) {
        if (mHelper == null) return;
        showProgress(false);
        mHelper.launchPurchaseFlow(act, PID, REQ_CODE, new IabHelper.OnIabPurchaseFinishedListener() {
            @Override
            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                LogUtils.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
                // if we were disposed of in the meantime, quit.
                if (mHelper == null){
                    return;
                }
                if (result.isFailure()) {
                    LogUtils.d(TAG, "Error purchasing: " + result);
                    onError(R.string.inapp_purch_error, result.getMessage());
                    return;
                }

                LogUtils.d(TAG, "Purchase successful.");
                consume(purchase);
            }
        }, devpayload);
    }

    private void consume(Purchase purchase) {
        showProgress(true);
        mHelper.consumeAsync(purchase, new IabHelper.OnConsumeFinishedListener() {
            @Override
            public void onConsumeFinished(Purchase purchase, IabResult result) {
                LogUtils.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

                // if we were disposed of in the meantime, quit.
                if (mHelper == null) return;

                // We know this is the "gas" sku because it's the only one we consume,
                // so we don't check which sku was consumed. If you have more than one
                // sku, you probably should check...
                if (result.isSuccess()) {
                    // successfully consumed, so we apply the effects of the item in our
                    // game world's logic, which in our case means filling the gas tank a bit
                    LogUtils.d(TAG, "Consumption successful. Provisioning.");
                    renewSubscribe(purchase.getDeveloperPayload());
                }else {
                    LogUtils.d(TAG, "Error while consuming: " + result);
                    onError(R.string.inapp_cons_error, result.getMessage());
                }
            }
        });
    }

    private void renewSubscribe(String developerPayload) {
        restAPI.renewSubscribe(developerPayload, prefUtils.getAuthToken()).enqueue(new Callback<ApiResponse<SubscribeResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<SubscribeResponse>> call, Response<ApiResponse<SubscribeResponse>> response) {
                String error = null;
                if (response.isSuccessful()) {
                    if(response.body().isSuccess()){
                        onSuccess(response.body().getData().getExpiredString());
                    }else{
                        error = response.body().getData().getMessage();
                    }
                } else {
                    error = response.message();
                }
                if(error != null){
                    LogUtils.d(TAG, "renewSubscribe error=" + error);
                    onError(R.string.inapp_renew_error, error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<SubscribeResponse>> call, Throwable t) {
                onError(R.string.inapp_renew_error, t.getMessage());
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.d(TAG, "onActivityResult1 "+resultCode);
        if (mHelper == null) return;
        // Pass on the activity result to the helper for handling
        mHelper.handleActivityResult(requestCode, resultCode, data);
    }

    private void onSuccess(String subscrDate) {
        showProgress(false);
        listener.onSubscribeSuccess();
        new AlertDialog.Builder(act)
                .setMessage(act.getString(R.string.inapp_succ, subscrDate))
                .setPositiveButton(android.R.string.ok, null)
                .show();
        dispose();
    }

    private void onError(int stringRes, String param) {
        if(act == null) return;
        onError(act.getString(stringRes, param));
    }

    private void onError(int stringRes) {
        if(act == null) return;
        onError(act.getString(stringRes));
    }

    private void onError(String error) {
        showProgress(false);
        new AlertDialog.Builder(act)
                .setTitle(R.string.error_title)
                .setMessage(error)
                .setPositiveButton(android.R.string.ok, null)
                .show();
        dispose();
    }

    private void showProgress(boolean show) {
        if(show){
            if(progress == null) progress = ProgressDialog.show(act, null, null);
        }else{
            if(progress != null){
                progress.dismiss();
                progress = null;
            }
        }
    }

    public void dispose() {
        showProgress(false);
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
        act = null;
    }

    public interface OnSubscribeResultListener{
        void onSubscribeSuccess();
    }
}
