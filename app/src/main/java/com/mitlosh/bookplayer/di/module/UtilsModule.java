package com.mitlosh.bookplayer.di.module;

import android.content.Context;

import com.mitlosh.bookplayer.di.scope.PerApplication;
import com.mitlosh.bookplayer.utils.PrefUtils;

import dagger.Module;
import dagger.Provides;

@Module
public class UtilsModule {

    @Provides
    @PerApplication
    public PrefUtils providePrefUtils(Context context) {
        return new PrefUtils(context);
    }

}
