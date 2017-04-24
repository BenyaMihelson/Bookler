package com.mitlosh.bookplayer.di.module;

import android.content.Context;
import android.os.Environment;

import com.danikula.videocache.HttpProxyCacheServer;
import com.mitlosh.bookplayer.Constants;
import com.mitlosh.bookplayer.audio.AudioServiceManager;
import com.mitlosh.bookplayer.di.scope.PerApplication;

import java.io.File;

import dagger.Module;
import dagger.Provides;

@Module
public class AudioModule {

    @Provides
    @PerApplication
    public AudioServiceManager provideAudioServiceManager(Context context) {
        return new AudioServiceManager(context);
    }

    @Provides
    @PerApplication
    public HttpProxyCacheServer provideHttpProxyCacheServer(Context context) {
        File tmpFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.TMP_FOLDER);
        tmpFolder.mkdirs();
        File cacheFolder = new File(tmpFolder.getAbsolutePath() + File.separator + "audioCache");
        cacheFolder.mkdirs();
        return new HttpProxyCacheServer.Builder(context)
                .cacheDirectory(cacheFolder)
                .maxCacheSize(1024 * 1024 * 1024)       // 1 Gb for cache
                .build();
    }

}
