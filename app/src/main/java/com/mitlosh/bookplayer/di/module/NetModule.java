package com.mitlosh.bookplayer.di.module;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mitlosh.bookplayer.App;
import com.mitlosh.bookplayer.Constants;
import com.mitlosh.bookplayer.di.scope.PerApplication;
import com.mitlosh.bookplayer.network.BooleanSerializer;
import com.mitlosh.bookplayer.network.RestAPI;
import com.mitlosh.bookplayer.network.RestClient;
import com.mitlosh.bookplayer.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
public class NetModule {

    public static final String HEADER_CACHE_CONTROL = "Cache-Control";

    private static final String TAG = "NetModule";

    @Provides
    @PerApplication
    RestAPI provideRestAPI(RestClient restClient){
        return restClient.createRestAPI();
    }

    @Provides
    @PerApplication
    RestClient provideRestClient(OkHttpClient okHttpClient, Gson gson){
        return new RestClient(okHttpClient, gson);
    }

    @Provides
    @PerApplication
    OkHttpClient provideOkHttpClient(Cache cache){
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.addNetworkInterceptor(provideCacheInterceptor());
        clientBuilder.addInterceptor(provideOfflineCacheInterceptor());
        if(Constants.IS_DEBUG){
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            clientBuilder.addInterceptor(logging);
        }
        clientBuilder.cache(cache);
        return clientBuilder.build();
    }

    private static Interceptor provideOfflineCacheInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept (Chain chain) throws IOException {
                Request request = chain.request();
                if (!Utils.isOnline(App.getAppContext()) && request.header(HEADER_CACHE_CONTROL) != null){
                    CacheControl cacheControl = new CacheControl.Builder()
                            .maxStale(7, TimeUnit.DAYS)
                            .build();

                    request = request.newBuilder()
                            .cacheControl(cacheControl)
                            .build();
                }
                return chain.proceed(request);
            }
        };
    }

    private static Interceptor provideCacheInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept (Chain chain) throws IOException {
                Response response = chain.proceed( chain.request() );
                // re-write response header to force use of cache
                CacheControl cacheControl = new CacheControl.Builder()
                        .maxAge(1, TimeUnit.SECONDS)
                        .build();
                return response.newBuilder()
                        .removeHeader("Pragma")
                        .header(HEADER_CACHE_CONTROL, cacheControl.toString())
                        .build();
            }
        };
    }

    @Provides
    @PerApplication
    Cache provideCache(Context context){
        Cache cache = null;
        try {
            cache = new Cache(new File(context.getCacheDir(), "http-cache"),
                    10 * 1024 * 1024 ); // 10 MB
        }catch (Exception e){
            e.printStackTrace();
        }
        return cache;
    }

    @Provides
    @PerApplication
    Gson provideGson() {
        BooleanSerializer boolSerializer = new BooleanSerializer();
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Boolean.class, boolSerializer)
                .registerTypeAdapter(boolean.class, boolSerializer)
                .create();
    }

}
