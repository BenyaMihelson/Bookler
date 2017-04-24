package com.mitlosh.bookplayer.audio;

import com.mitlosh.bookplayer.model.Track;

public class AudioState {

    private int currentPosition;
    private Track currentTrack;
    private boolean isStarted;
    private boolean isPlaying;
    private boolean isError;
    private boolean isEnded;
    private int progress;
    private int bufferProgress;

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public void setCurrentTrack(Track currentTrack) {
        this.currentTrack = currentTrack;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getBufferProgress() {
        return bufferProgress;
    }

    public void setBufferProgress(int bufferProgress) {
        this.bufferProgress = bufferProgress;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }

    public boolean isEnded() {
        return isEnded;
    }

    public void setEnded(boolean ended) {
        isEnded = ended;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }
}
