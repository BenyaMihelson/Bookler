package com.mitlosh.bookplayer.audio;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.mitlosh.bookplayer.model.Album;
import com.mitlosh.bookplayer.model.Track;

import java.util.ArrayList;
import java.util.List;

public class AudioServiceManager implements ServiceConnection, AudioService.OnAudioPlaybackListener {

    private final Context context;
    private AudioInterface audioInterface;
    private boolean isBound;

    private List<AudioService.OnAudioPlaybackListener> playbackListeners = new ArrayList<>();

    private List<Track> content;
    private int position;
    private boolean hasStartPlay;
    private Album album;

    public AudioServiceManager(Context context) {
        this.context = context;
    }

    public void playContent(Album album, List<Track> content, int position) {
        this.album = album;
        this.content = content;
        this.position = position;
        if(isBound){
            audioInterface.playContent(album, content, position);

        }else{
            bindServiceAndPlay();
        }
    }

    public void seekTo(int progress) {
        if(isBound){
            audioInterface.seekTo(progress);
        }
    }

    private void bindServiceAndPlay() {
        hasStartPlay = true;
        Intent serviceIntent = new Intent(context, AudioService.class);
        if(!isAudioServiceRunning()) context.startService(serviceIntent);
        context.bindService(serviceIntent, this, 0);
    }

    private boolean isAudioServiceRunning() {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (AudioService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void addOnAudioPlaybackListener(AudioService.OnAudioPlaybackListener audioPlaybackListener){
        playbackListeners.add(audioPlaybackListener);
    }

    public void removeOnAudioPlaybackListener(AudioService.OnAudioPlaybackListener audioPlaybackListener){
        playbackListeners.remove(audioPlaybackListener);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        audioInterface =((AudioService.AudioServiceBinder) iBinder).getService();
        audioInterface.setOnAudioPlaybackListener(this);
        isBound = true;
        if(hasStartPlay){
            audioInterface.playContent(album, content, position);
            hasStartPlay = false;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        isBound = false;
        audioInterface = null;
    }

    public void onStart() {

    }

    public void onStop() {

    }

    @Override
    public void onProgressUpdate(int albumId, int position, int progress, int buffer) {
        for(AudioService.OnAudioPlaybackListener listener : playbackListeners){
            listener.onProgressUpdate(albumId, position, progress, buffer);
        }
    }

    @Override
    public void onStateChanged(AudioState audioState) {
        for(AudioService.OnAudioPlaybackListener listener : playbackListeners){
            listener.onStateChanged(audioState);
        }
    }

    public AudioState getAudioState() {
        if(isBound){
            return audioInterface.getAudioState();
        }
        return null;
    }

}
