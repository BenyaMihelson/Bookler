package com.mitlosh.bookplayer.di.component;

import com.mitlosh.bookplayer.audio.AudioService;
import com.mitlosh.bookplayer.billing.SubscribeManager;
import com.mitlosh.bookplayer.di.module.AppModule;
import com.mitlosh.bookplayer.di.module.AudioModule;
import com.mitlosh.bookplayer.di.module.NetModule;
import com.mitlosh.bookplayer.di.module.UtilsModule;
import com.mitlosh.bookplayer.di.scope.PerApplication;
import com.mitlosh.bookplayer.network.RestAPI;
import com.mitlosh.bookplayer.ui.activity.LoginActivity;
import com.mitlosh.bookplayer.ui.activity.MainActivity;
import com.mitlosh.bookplayer.ui.activity.SplashActivity;
import com.mitlosh.bookplayer.ui.fragment.AlbumContentFragment;
import com.mitlosh.bookplayer.ui.fragment.AlbumListFragment;
import com.mitlosh.bookplayer.utils.PrefUtils;
import com.mitlosh.bookplayer.viewmodel.TrackViewModel;

import dagger.Component;

@Component(modules = {AppModule.class, NetModule.class, AudioModule.class, UtilsModule.class})
@PerApplication
public interface AppComponent {

    void inject(SplashActivity splashActivity);
    void inject(LoginActivity loginActivity);
    void inject(MainActivity mainActivity);

    void inject(TrackViewModel trackViewModel);

    void inject(AudioService audioService);

    void inject(AlbumListFragment albumListFragment);
    void inject(AlbumContentFragment albumContentFragment);

    void inject(SubscribeManager subscribeManager);

    RestAPI getRestApi();
    PrefUtils getPrefUtils();
}
