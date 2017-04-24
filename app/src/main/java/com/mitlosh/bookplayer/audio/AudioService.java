package com.mitlosh.bookplayer.audio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.RemoteViews;

import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.gson.JsonObject;
import com.mitlosh.bookplayer.App;
import com.mitlosh.bookplayer.R;
import com.mitlosh.bookplayer.model.Album;
import com.mitlosh.bookplayer.model.Track;
import com.mitlosh.bookplayer.ui.activity.MainActivity;
import com.mitlosh.bookplayer.utils.LogUtils;
import com.mitlosh.bookplayer.utils.PrefUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AudioService extends Service implements AudioInterface, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, CacheListener {

    private static final String TAG = "AudioService";

    public static final String MAIN_ACTION = "com.mitlosh.bookplayer.audio.action.main";
    public static final String PREV_ACTION = "com.mitlosh.bookplayer.audio.action.prev";
    public static final String PLAY_ACTION = "com.mitlosh.bookplayer.audio.action.play";
    public static final String NEXT_ACTION = "com.mitlosh.bookplayer.audio.action.next";
    public static final String STARTFOREGROUND_ACTION = "com.mitlosh.bookplayer.audio.action.startforeground";
    public static final String STOPFOREGROUND_ACTION = "com.mitlosh.bookplayer.audio.action.stopforeground";
    private static final int NOTIFICATION_ID = 101;

    private AudioServiceBinder binder = new AudioServiceBinder();
    private AudioState audioState;
    private Handler mHandler = new Handler();

    private MediaPlayer mediaPlayer;
    private List<Track> trackList;
    private int currentPosition;
    private OnAudioPlaybackListener audioPlaybackListener;
    private Album currentAlbum;

    @Inject
    PrefUtils prefUtils;
    @Inject
    HttpProxyCacheServer proxy;







    @Override
    public void onCreate() {
        super.onCreate();
        App.getAppComponent().inject(this);
        LogUtils.d(TAG, "onCreate");
        audioState = new AudioState();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);

        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(psListener, PhoneStateListener.LISTEN_CALL_STATE);
        }



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null){
            if (intent.getAction().equals(PREV_ACTION)) {
                LogUtils.d(TAG, "Clicked Previous");
                skipBack();
            } else if (intent.getAction().equals(PLAY_ACTION)) {
                LogUtils.d(TAG, "Clicked Play");
                pauseOrResume();
            } else if (intent.getAction().equals(NEXT_ACTION)) {
                LogUtils.d(TAG, "Clicked Next");
                skipForward();
            } else if (intent.getAction().equals(STOPFOREGROUND_ACTION)) {
                LogUtils.d(TAG, "Received Stop Foreground Intent");
                stop();
            }
        }
        return START_STICKY;
    }

    @Override
    public void playContent(Album album, List<Track> content, int position) {
        if(currentAlbum != null
                && currentAlbum.getId() == album.getId()
                && currentPosition == position && !audioState.isError()){
            pauseOrResume();
        }else{
            currentAlbum = album;
            trackList = new ArrayList<>(content);
            currentPosition = position;
            if(audioPlaybackListener != null){
                audioPlaybackListener.onProgressUpdate(currentAlbum.getId(), currentPosition, 0, 0);
            }
            playTrack(currentPosition);
            setAlbumListenStatus(album, Album.STATUS_LISTENING);
            startForeground(NOTIFICATION_ID, getStatusBar());
        }
    }

    private Notification getStatusBar() {
        // Using RemoteViews to bind custom layouts into Notification
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.status_bar);
        RemoteViews bigViews = new RemoteViews(getPackageName(), R.layout.status_bar_expanded);

        // showing default album image
        views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE);
        views.setViewVisibility(R.id.status_bar_album_art, View.GONE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent previousIntent = new Intent(this, AudioService.class);
        previousIntent.setAction(PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0, previousIntent, 0);

        Intent playIntent = new Intent(this, AudioService.class);
        playIntent.setAction(PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);

        Intent nextIntent = new Intent(this, AudioService.class);
        nextIntent.setAction(NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0, nextIntent, 0);

        Intent closeIntent = new Intent(this, AudioService.class);
        closeIntent.setAction(STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0, closeIntent, 0);

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);

        views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);

        views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);

        views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);

        LogUtils.d(TAG, "getStatusBar isPlaying="+audioState.isPlaying());

        int playRes = audioState.isPlaying() ? R.drawable.ic_notif_pause : R.drawable.ic_notif_play;
        views.setImageViewResource(R.id.status_bar_play, playRes);
        bigViews.setImageViewResource(R.id.status_bar_play, playRes);

        String title = trackList.get(currentPosition).getTitle();
        views.setTextViewText(R.id.status_bar_track_name, title);
        bigViews.setTextViewText(R.id.status_bar_track_name, title);

        String artist = currentAlbum.getAuthor();
        views.setTextViewText(R.id.status_bar_artist_name, artist);
        bigViews.setTextViewText(R.id.status_bar_artist_name, artist);

        bigViews.setTextViewText(R.id.status_bar_album_name, currentAlbum.getTitle());

        NotificationCompat.Builder nb = new NotificationCompat.Builder(this);
        nb.setCustomContentView(views);
        nb.setCustomBigContentView(bigViews);
        nb.setOngoing(true);
        nb.setSmallIcon(R.mipmap.ic_launcher);
        nb.setContentIntent(pendingIntent);
        Notification notif = nb.build();

        Picasso.with(getApplicationContext())
                .load(currentAlbum.getImage())
                .resizeDimen(R.dimen.notification_expanded_height, R.dimen.notification_expanded_height)
                .centerCrop()
                .into(bigViews, R.id.status_bar_album_art, NOTIFICATION_ID, notif);

        return notif;
    }

    private void updateStatusBar(){
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, getStatusBar());
    }

    private void playTrack(int position){
        currentPosition = position;
        audioState.setCurrentPosition(position);
        audioState.setProgress(0);
        audioState.setError(false);
        try {
            Track track = trackList.get(position);
            String url = track.getUrl();
            boolean cached = proxy.isCached(url);
            audioState.setBufferProgress(cached ? 100 : 0);
            if(!cached) proxy.registerCacheListener(this, url);
            String proxyUrl = proxy.getProxyUrl(url);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(proxyUrl);
            mediaPlayer.prepareAsync();
            audioState.setStarted(true);
            audioState.setCurrentTrack(track);


        } catch (Exception e) {
            audioState.setError(true);
            audioState.setPlaying(false);
            if(audioPlaybackListener != null){
                audioPlaybackListener.onStateChanged(audioState);
            }
            e.printStackTrace();
            //FIXME
            sendStackTrace(e);
        }
    }

    PhoneStateListener psListener = new PhoneStateListener(){
        int myCurrenPosition = 0;
        int mySelectedTrack = 0;
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {

                //Incoming call: Pause music
                //mediaPlayer.pause();
              if(mediaPlayer!=null&&mediaPlayer.isPlaying()){}

                myCurrenPosition = mediaPlayer.getCurrentPosition();

                //audioState.setPlaying(false);

                pauseOrResume();

            } else if(state == TelephonyManager.CALL_STATE_IDLE) {


                /*mediaPlayer.reset();
                mediaPlayer.start();
                */

                if(myCurrenPosition!=0){


                //Not in call: Play music
            } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                //A call is dialing, active or on hold
            }
                        super.onCallStateChanged(state, incomingNumber);
        }
    };



    private void sendStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        MediaType JSON = MediaType.parse("application/json");
        String URL = "https://maker.ifttt.com/trigger/ipost_crash/with/key/cS8SqITQendJYeXPJdY3iG";
        JsonObject report = new JsonObject();
        report.addProperty("value1", exceptionAsString);
        RequestBody body = RequestBody.create(JSON, report.toString());
        Request request = new Request.Builder().url(URL).post(body).build();
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    @Override
    public void pauseOrResume() {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            audioState.setPlaying(false);
        }else{
            mediaPlayer.start();
            audioState.setPlaying(true);
            updateProgressBar();
        }
        if(audioPlaybackListener != null){
            audioPlaybackListener.onStateChanged(audioState);
        }
        updateStatusBar();
    }

    @Override
    public void stop() {
        audioState.setPlaying(false);
        audioState.setStarted(false);
        if(audioPlaybackListener != null){
            audioPlaybackListener.onStateChanged(audioState);
        }
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void skipForward() {
        if(currentPosition < trackList.size() - 1){
            playTrack(currentPosition + 1);
            updateStatusBar();
        }else{
            setAlbumListenStatus(currentAlbum, Album.STATUS_LISTENED);
            audioState.setEnded(true);
            stop();
        }
    }

    @Override
    public void skipBack() {
        playTrack(currentPosition != 0 ? currentPosition - 1 : currentPosition);
        updateStatusBar();
    }

    @Override
    public void seekTo(int progress) {
        mediaPlayer.seekTo(mediaPlayer.getDuration() * progress / 100);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        mHandler.removeCallbacks(mUpdateTimeTask);
        trackList.clear();
        trackList = null;
        audioPlaybackListener = null;
        LogUtils.d(TAG, "onDestroy");
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        skipForward();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        LogUtils.d(TAG, "onPrepared");
        mediaPlayer.start();
        audioState.setPlaying(true);
        if(audioPlaybackListener != null){
            audioPlaybackListener.onStateChanged(audioState);
        }
        updateProgressBar();
        updateStatusBar();
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 1000);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if(!mediaPlayer.isPlaying())return;
            if(audioPlaybackListener != null){
                audioState.setProgress(getProgressPercentage(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration()));

                audioPlaybackListener.onProgressUpdate(currentAlbum.getId(), currentPosition, audioState.getProgress(), audioState.getBufferProgress());
            }
            // Running this thread after 100 milliseconds
            updateProgressBar();
        }
    };

    @Override
    public void setOnAudioPlaybackListener(OnAudioPlaybackListener audioPlaybackListener){
        this.audioPlaybackListener = audioPlaybackListener;
    }

    @Override
    public AudioState getAudioState() {
        return audioState;
    }

    private void setAlbumListenStatus(Album album, int status) {
        album.setStatus(status);
        prefUtils.addToHistory(album);
    }

    public int getProgressPercentage(long currentDuration, long totalDuration){
        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        Double percentage =(((double)currentSeconds)/totalSeconds)*100;

        // return percentage
        return percentage.intValue();
    }

    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
        audioState.setBufferProgress(percentsAvailable);
        if(audioPlaybackListener != null && currentPosition < trackList.size() && trackList.get(currentPosition).getUrl().equals(url)){
            audioPlaybackListener.onProgressUpdate(currentAlbum.getId(), currentPosition, audioState.getProgress(), percentsAvailable);
        }
        if(percentsAvailable >= 100) proxy.unregisterCacheListener(this, url);
    }

    public class AudioServiceBinder extends Binder {
        public AudioInterface getService() {
            return AudioService.this;
        }
    }

    public interface OnAudioPlaybackListener{
        void onProgressUpdate(int albumId, int position, int progress, int buffer);
        void onStateChanged(AudioState audioState);
    }

}
