/* Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mitlosh.bookplayer.billing;

import com.mitlosh.bookplayer.App;
import com.mitlosh.bookplayer.network.RestAPI;
import com.mitlosh.bookplayer.network.response.ApiResponse;
import com.mitlosh.bookplayer.network.response.VerifyResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Security-related methods. For a secure implementation, all of this code
 * should be implemented on a server that communicates with the
 * application on the device. For the sake of simplicity and clarity of this
 * example, this code is included here and is executed on the device. If you
 * must verify the purchases on the phone, you should obfuscate this code to
 * make it harder for an attacker to replace the code with stubs that treat all
 * purchases as verified.
 */
public class Security {

    private static final String TAG = "IABUtil/Security";

    /**
     * Verifies that the data was signed with the given signature, and returns
     * the verified purchase. The data is in JSON format and signed
     * with a private key. The data also contains the
     * and product ID of the purchase.
     * @param signedData the signed JSON string (signed, not encrypted)
     * @param signature the signature for the data, signed with the private key
     */
    public static void verifyPurchaseAsync(String signedData, String signature, final OnVerifyResultListener listener) {
        RestAPI api = App.getAppComponent().getRestApi();
        String token = App.getAppComponent().getPrefUtils().getAuthToken();
        api.verifyPurchase(signedData, signature, token).enqueue(new Callback<ApiResponse<VerifyResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<VerifyResponse>> call, Response<ApiResponse<VerifyResponse>> response) {
                listener.onResult(response.isSuccessful() && response.body().isSuccess() && response.body().getData().isValid());
            }

            @Override
            public void onFailure(Call<ApiResponse<VerifyResponse>> call, Throwable t) {
                listener.onResult(false);
            }
        });
    }

    public static boolean verifyPurchase(String signedData, String signature) {
        RestAPI api = App.getAppComponent().getRestApi();
        String token = App.getAppComponent().getPrefUtils().getAuthToken();
        try {
            Response<ApiResponse<VerifyResponse>> response = api.verifyPurchase(signedData, signature, token).execute();
            return response.isSuccessful() && response.body().isSuccess() && response.body().getData().isValid();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public interface OnVerifyResultListener{
        void onResult(boolean success);
    }
}
