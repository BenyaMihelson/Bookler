package com.mitlosh.bookplayer.audio;

import com.mitlosh.bookplayer.model.Album;
import com.mitlosh.bookplayer.model.Track;

import java.util.List;

public interface AudioInterface {

    void playContent(Album album, List<Track> content, int position);

    void pauseOrResume();
    void stop();
    void skipForward();
    void skipBack();
    void seekTo(int progress);

    void setOnAudioPlaybackListener(AudioService.OnAudioPlaybackListener audioPlaybackListener);

    AudioState getAudioState();

}
