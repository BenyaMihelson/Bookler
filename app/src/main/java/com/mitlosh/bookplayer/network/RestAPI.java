package com.mitlosh.bookplayer.network;

import com.mitlosh.bookplayer.di.module.NetModule;
import com.mitlosh.bookplayer.network.response.AlbumContent;
import com.mitlosh.bookplayer.network.response.AlbumList;
import com.mitlosh.bookplayer.network.response.AlbumStat;
import com.mitlosh.bookplayer.network.response.ApiResponse;
import com.mitlosh.bookplayer.network.response.AuthData;
import com.mitlosh.bookplayer.network.response.BaseData;
import com.mitlosh.bookplayer.network.response.CategoryList;
import com.mitlosh.bookplayer.network.response.DevPayloadToken;
import com.mitlosh.bookplayer.network.response.SubscribeResponse;
import com.mitlosh.bookplayer.network.response.VerifyResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RestAPI {

    @FormUrlEncoded
    @POST("auth")
    Call<ApiResponse<AuthData>> auth(@Field("phone") String phone,
                                     @Field("digits_service") String digitsService,
                                     @Field("digits_auth") String digitsAuth);

    @Headers({NetModule.HEADER_CACHE_CONTROL + ": enable"})
    @GET("getAlbums")
    Call<ApiResponse<AlbumList>>    getAlbums(@Query("offset") int offset,
                                           @Query("count") int count,
                                           @Query("new") int needNew,
                                           @Query("token") String token);

    @Headers({NetModule.HEADER_CACHE_CONTROL + ": enable"})
    @GET("getContent")
    Call<ApiResponse<AlbumContent>> getContent(@Query("id") int id,
                                               @Query("token") String token);

    @FormUrlEncoded
    @POST("search")
    Call<ApiResponse<AlbumList>> search(@Field("query") String query,
                                        @Field("offset") int offset,
                                        @Field("count") int count,
                                        @Field("token") String token);

    @Headers({NetModule.HEADER_CACHE_CONTROL + ": enable"})
    @GET("getCategories")
    Call<ApiResponse<CategoryList>> getCategories(@Query("token") String token);

    @Headers({NetModule.HEADER_CACHE_CONTROL + ": enable"})
    @GET("getAlbumsByCategory")
    Call<ApiResponse<AlbumList>> getAlbumsByCategory(@Query("id") int categoryId,
                                                     @Query("offset") int offset,
                                                     @Query("count") int count,
                                                     @Query("token") String token);

    @FormUrlEncoded
    @POST("get_devpayload")
    Call<ApiResponse<DevPayloadToken>> getDevpayload(@Field("pid") String pid,
                                                     @Field("token") String token);

    @FormUrlEncoded
    @POST("verify_purchase")
    Call<ApiResponse<VerifyResponse>> verifyPurchase(@Field("purchase_data") String purchaseData,
                                                     @Field("signature") String signature,
                                                     @Field("token") String token);

    @FormUrlEncoded
    @POST("checkSubscribe")
    Call<ApiResponse<SubscribeResponse>> checkSubscribe(@Field("token") String token);

    @FormUrlEncoded
    @POST("renewSubscribe")
    Call<ApiResponse<SubscribeResponse>> renewSubscribe(@Field("devpayload_token") String devpayload,
                                                        @Field("token") String token);

    @FormUrlEncoded
    @POST("makeFeedback")
    Call<ApiResponse<BaseData>> sendFeedback(@Field("mail") String email,
                                             @Field("message") String message,
                                             @Field("token") String token);

    @FormUrlEncoded
    @POST("addStatistic")
    Call<ApiResponse<AlbumStat>> addStatistic(@Field("token") String token,
                                              @Field("album_id") int albumId);


}
