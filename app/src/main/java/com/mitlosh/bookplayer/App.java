package com.mitlosh.bookplayer;

import android.app.Application;
import android.content.Context;

import com.digits.sdk.android.Digits;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.mitlosh.bookplayer.di.component.AppComponent;
import com.mitlosh.bookplayer.di.component.DaggerAppComponent;
import com.mitlosh.bookplayer.di.module.AppModule;
import com.mitlosh.bookplayer.di.module.AudioModule;
import com.mitlosh.bookplayer.di.module.NetModule;
import com.mitlosh.bookplayer.di.module.UtilsModule;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.yandex.metrica.YandexMetrica;

import io.fabric.sdk.android.Fabric;

public class App extends Application {

    private static final String TWITTER_KEY = "CPLxbworWpTVQr6jEOwXMKuQK";
    private static final String TWITTER_SECRET = "5Svit98umkL48ohjHaqU7r3p1dseL4IQcgJiG378yQRx4G5VLI";
    private static final String YANDEX_METRICA_KEY = "07783c81-da36-48ee-9749-12e0fadc7db7";

    private static Context mContext;
    private static AppComponent appComponent;

    public static Context getAppContext() {
        return mContext;
    }

    public static AppComponent getAppComponent() {
        return appComponent;
    }

    /**
     * Это подкласс {@link Application}, с помощью которого приложению передаются общие объекты,
     * например {@link Tracker}.
     */
    private static GoogleAnalytics sAnalytics;
    private static Tracker sTracker;
        /**
         * Получает счетчик {@link Tracker}, используемый по умолчанию для этого приложения {@link Application}.
         * @return tracker
         */


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        appComponent = buildComponent();

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Digits.Builder digitsBuilder = new Digits.Builder().withTheme(R.style.DigitsTheme);
        Fabric.with(this, new TwitterCore(authConfig), digitsBuilder.build());

        // Инициализация AppMetrica SDK
        YandexMetrica.activate(getApplicationContext(), YANDEX_METRICA_KEY);
        // Отслеживание активности пользователей
        YandexMetrica.enableActivityAutoTracking(this);

        sAnalytics = GoogleAnalytics.getInstance(this);

    }

    synchronized public Tracker getDefaultTracker() {
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        if (sTracker == null) {
            sTracker = sAnalytics.newTracker(R.xml.global_tracker);
        }

        return sTracker;
    }

    private AppComponent buildComponent() {
        return DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .netModule(new NetModule())
                .audioModule(new AudioModule())
                .utilsModule(new UtilsModule())
                .build();
    }
}
