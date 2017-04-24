package com.mitlosh.bookplayer.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.MotionEvent;
import android.view.View;

import com.mitlosh.bookplayer.App;
import com.mitlosh.bookplayer.BR;
import com.mitlosh.bookplayer.R;
import com.mitlosh.bookplayer.audio.AudioService;
import com.mitlosh.bookplayer.audio.AudioServiceManager;
import com.mitlosh.bookplayer.audio.AudioState;
import com.mitlosh.bookplayer.model.Track;
import com.mitlosh.bookplayer.utils.LogUtils;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class TrackViewModel extends BaseObservable implements AudioService.OnAudioPlaybackListener {

    private static final String TAG = "TrackViewModel";
    private String title;
    private String duration;

    private String playTime;

    private boolean isStarted;
    private boolean isPlaying;
    private int progress;
    private int bufferProgress;

    private OnTrackClickListener onTrackClickListener;
    private int position;

    @Inject
    AudioServiceManager audioServiceManager;
    private int albumId;
    private boolean isError;
    private int userProgress;

    public TrackViewModel(){
        App.getAppComponent().inject(this);
    }

    public void setTrack(Track track, int position) {
        audioServiceManager.addOnAudioPlaybackListener(this);
        albumId = track.getAlbumId();
        this.position = position;
        title = String.format(Locale.getDefault(), "%02d. %s", position + 1, track.getTitle());
        long seconds = track.getDuration();
        duration = secondsToDuration(seconds);

        AudioState state = audioServiceManager.getAudioState();

        if(state != null && state.getCurrentTrack().equals(track)){
            isStarted = state.isStarted();
            isPlaying = state.isPlaying();
            isError = state.isError();
            progress = state.getProgress();
            bufferProgress = state.getBufferProgress();
        }else{
            isStarted = false;
            isPlaying = false;
            isError = false;
            progress = 0;
            bufferProgress = 0;
        }

        notifyPropertyChanged(BR.started);
        notifyPropertyChanged(BR.imageResource);
        notifyPropertyChanged(BR.title);
        notifyPropertyChanged(BR.duration);
        notifyPropertyChanged(BR.progress);
        notifyPropertyChanged(BR.bufferProgress);
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    @Bindable
    public String getDuration() {
        return duration;
    }

    @Bindable
    public int getImageResource() {
        int imageResource;
        if(isStarted){
            if(isPlaying){
                imageResource = R.drawable.ic_music_pause_button;
            }else if(isError){
                imageResource = R.drawable.ic_music_error;
            }else{
                imageResource = R.drawable.ic_music_play_button;
            }
        }else{
            imageResource = R.drawable.ic_music_note_multiple;
        }
        return imageResource;
    }

    @Bindable
    public int getProgress() {
        return progress;
    }

    @Bindable
    public int getBufferProgress() {
        return bufferProgress;
    }

    @Bindable
    public boolean isStarted() {
        return isStarted && !isError;
    }

    @Bindable
    public String getPlayTime() {
        return playTime;
    }

    public void onTrackClick(View view){
        isStarted = true;
        isPlaying = !isPlaying;
        notifyPropertyChanged(BR.imageResource);
        notifyPropertyChanged(BR.started);
        if(onTrackClickListener != null) onTrackClickListener.onTrackClick(position);
    }

    public void onPlayClick(View view){

    }

    public void onSeekBarChange(int value, boolean fromUser){
        LogUtils.d(TAG, "onSeekBarChange value="+value +" fromUser="+fromUser);
        if(fromUser) userProgress = value;
    }

    public void onStopTrackingTouch(){
        LogUtils.d(TAG, "onStopTrackingTouch userProgress=" + userProgress);
        audioServiceManager.seekTo(userProgress);
    }

    public void setOnTrackClickListener(OnTrackClickListener onTrackClickListener){
        this.onTrackClickListener = onTrackClickListener;
    }

    private String secondsToDuration(long seconds) {
        return String.format(Locale.getDefault(), "%d:%02d",
                TimeUnit.SECONDS.toMinutes(seconds),
                seconds - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds)));
    }

    @Override
    public void onProgressUpdate(int albumId, int position, int progress, int buffer) {
        if(this.albumId == albumId && this.position == position){
            if(this.progress != progress){
                this.progress = progress;
                notifyPropertyChanged(BR.progress);
            }
            if(bufferProgress != buffer){
                bufferProgress = buffer;
                notifyPropertyChanged(BR.bufferProgress);
            }
        }else{
            if(isStarted){
                isStarted = false;
                isPlaying = false;
                notifyPropertyChanged(BR.imageResource);
                notifyPropertyChanged(BR.started);
            }
        }
    }

    @Override
    public void onStateChanged(AudioState audioState) {
        if(this.albumId == audioState.getCurrentTrack().getAlbumId() && position == audioState.getCurrentPosition()){
            isStarted = audioState.isStarted();
            isPlaying = audioState.isPlaying();
            isError = audioState.isError();
        }else{
            isStarted = false;
            isPlaying = false;
            isError = false;
        }
        notifyPropertyChanged(BR.imageResource);
        notifyPropertyChanged(BR.started);
    }

    public void onDestroy(){
        audioServiceManager.removeOnAudioPlaybackListener(this);
    }

    public interface OnTrackClickListener{
        void onTrackClick(int position);
    }

}
