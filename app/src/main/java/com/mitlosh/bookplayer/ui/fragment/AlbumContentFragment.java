package com.mitlosh.bookplayer.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mitlosh.bookplayer.App;
import com.mitlosh.bookplayer.R;
import com.mitlosh.bookplayer.audio.AudioService;
import com.mitlosh.bookplayer.audio.AudioServiceManager;
import com.mitlosh.bookplayer.audio.AudioState;
import com.mitlosh.bookplayer.databinding.FragmentAlbumContentBinding;
import com.mitlosh.bookplayer.model.Album;
import com.mitlosh.bookplayer.model.Track;
import com.mitlosh.bookplayer.network.response.AlbumContent;
import com.mitlosh.bookplayer.ui.adapter.AlbumContentListAdapter;
import com.mitlosh.bookplayer.viewmodel.TrackViewModel;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

public class AlbumContentFragment extends BaseFragment implements TrackViewModel.OnTrackClickListener, AudioService.OnAudioPlaybackListener {

    public static final String TAG = "AlbumContentFragment";
    private static final String EXTRA_ALBUM = "album";
    private FragmentAlbumContentBinding binding;
    private AlbumContentListAdapter adapter;
    private AlbumContentFI callback;
    private AlbumContent albumContent;
    private Button topButton;

    private int statCount = 0;

    @Inject
    AudioServiceManager audioServiceManager;

    public static Fragment newInstance(Album album) {
        Fragment fragment = new AlbumContentFragment();
        Bundle args = new Bundle(1);
        args.putSerializable(EXTRA_ALBUM, album);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent().inject(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_album_content, container, false);

        //binding.topButton.setText("0000");


        setupList();

        Album album = (Album) getArguments().getSerializable(EXTRA_ALBUM);
        setTitle(album.getTitle());

        showProgress(true);
        callback.getAlbumContent(album.getId());

        audioServiceManager.addOnAudioPlaybackListener(this);

        setHasOptionsMenu(true);





        return binding.getRoot();
    }

    protected void setupList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setRecycleChildrenOnDetach(true);
        binding.list.setLayoutManager(layoutManager);
        adapter = new AlbumContentListAdapter();
        adapter.setOnTrackClickListener(this);
        binding.list.setAdapter(adapter);
    }

    public void onLoadAlbumContent(AlbumContent albumContent) {
        this.albumContent = albumContent;
        adapter.setContent(albumContent.getContent());
        setEmptyText(null);
    }

    public void showProgress(boolean show) {
        binding.progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setEmptyText(String message) {
        binding.emptyText.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        binding.emptyText.setText(message != null ? message : getString(R.string.empty_list));
    }

    @Override
    protected int getTitleRes() {
        return 0;
    }

    @Override
    protected boolean showFABOnResume() {
        return false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (AlbumContentFI) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public void onDestroyView() {
        audioServiceManager.removeOnAudioPlaybackListener(this);
        super.onDestroyView();
    }

    @Override
    public void onTrackClick(int position) {
        callback.playContent(albumContent.getAlbum(), albumContent.getContent(), position);

        Calendar c = Calendar.getInstance();
        int seconds = c.get(Calendar.SECOND);


        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

         int seconds_pref = sharedPref.getInt("time", 0);

        if(seconds>seconds_pref+ 86400){

            callback.addStatistic(albumContent.getAlbum().getId());

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("time", seconds);
            editor.commit();

        }

        statCount++;
    }

    @Override
    public void onProgressUpdate(int albumId, int position, int progress, int buffer) {}

    @Override
    public void onStateChanged(AudioState audioState) {
        if(audioState.isEnded()) showShareMessage();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.album_content_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_like){
            if(callback != null) callback.shareAlbum(albumContent.getAlbum());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showShareMessage() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.share)
                .setMessage(R.string.share_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callback.shareApp();
                    }
                })
                .show();
    }

    public interface AlbumContentFI{
        void getAlbumContent(int albumId);
        void playContent(Album album, List<Track> content, int position);
        void shareApp();
        void shareAlbum(Album album);
        void addStatistic(int albumId);
    }

}
