package com.mitlosh.bookplayer.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mitlosh.bookplayer.App;
import com.mitlosh.bookplayer.R;
import com.mitlosh.bookplayer.model.Album;
import com.mitlosh.bookplayer.ui.adapter.AlbumsListAdapter;
import com.mitlosh.bookplayer.ui.adapter.HistoryListAdapter;
import com.mitlosh.bookplayer.utils.LogUtils;
import com.mitlosh.bookplayer.utils.PrefUtils;
import com.mitlosh.bookplayer.utils.StateSaveHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class AlbumListFragment extends BaseListFragment implements AlbumsListAdapter.OnAlbumListListener, HistoryListAdapter.OnHistoryListListener {

    public static final String TAG = "AlbumListFragment";

    private static final String EXTRA_POSITION = "position";
    private static final String EXTRA_ALBUMS = "albums";
    private static final String EXTRA_HISTORY = "history";
    private static final String EXTRA_HASMORE = "hasmore";
    private static final String EXTRA_QUERY = "query";

    private AlbumsListAdapter listAdapter;
    private AlbumListFI callback;

    @Inject
    PrefUtils prefUtils;

    private String query;

    private Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        setHasOptionsMenu(true);
        mTracker = new App().getDefaultTracker();
    }

    @Override
    protected RecyclerView.Adapter provideListAdapter() {
        listAdapter = new AlbumsListAdapter();
        return listAdapter;
    }

    @Override
    protected void reloadAll() {
        Bundle savedState = StateSaveHelper.restoreState(getTag(), true);
        if(savedState != null){
            LogUtils.d(TAG, "onActivityCreated savedState != null");
            List<Album> albums = (ArrayList<Album>) savedState.getSerializable(EXTRA_ALBUMS);
            List<Album> history = null;
            if(savedState.containsKey(EXTRA_HISTORY)){
                history = prefUtils.getHistory();
                if(history.size() == 0) history = (ArrayList<Album>) savedState.getSerializable(EXTRA_HISTORY);
            }
            if(savedState.containsKey(EXTRA_QUERY)){
                query = savedState.getString(EXTRA_QUERY);
            }
            boolean hasMore = savedState.getBoolean(EXTRA_HASMORE);
            int savedPosition = savedState.getInt(EXTRA_POSITION);
            updateList(albums, history, hasMore, true);
            list.getLayoutManager().scrollToPosition(savedPosition);
            showListProgress(false);
        }else{
            getAlbumList(0);
        }
    }

    protected void getAlbumList(int offset) {
        if(callback != null) callback.getAlbumList(offset);
    }

    protected void search(String query, int offset) {
        this.query = query;
        if(callback != null) callback.search(query, offset);
    }

    public void updateList(List<Album> albums, boolean hasMore, boolean clear) {
        updateList(albums, null, hasMore, clear);
    }

    public void updateList(List<Album> albums, List<Album> history, boolean hasMore, boolean clear) {
        listAdapter.addAll(albums, hasMore, clear);
        listAdapter.setOnAlbumListListener(this);
        listAdapter.setHistory(history);
        listAdapter.setOnHistoryListListener(history != null ? this : null);
        setEmptyText(query != null ? getString(R.string.search_not_found) : null);
        if(clear) list.getLayoutManager().scrollToPosition(0);
    }

    @Override
    protected int getDeltaItemCount() {
        return listAdapter.withHistory() ? 2 : 1;
    }

    @Override
    protected int getTitleRes() {
        return R.string.albums_list;
    }

    @Override
    protected boolean showFABOnResume() {
        return false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (AlbumListFI) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public void onLoadMore() {
        LogUtils.d(TAG, "onLoadMore");
        if(query != null){
            search(query, listAdapter.getSize());
        }else{
            getAlbumList(listAdapter.getSize());
        }
    }

    @Override
    public void onShowMoreHistory() {
        LogUtils.d(TAG, "onShowMoreHistory");
    }

    @Override
    public void onAlbumClick(Album album) {
        LogUtils.d(TAG, "onAlbumClick title="+album.getTitle());
        getFragmentManager().saveFragmentInstanceState(this);
        getFragmentManager().beginTransaction()
                .replace(R.id.container, AlbumContentFragment.newInstance(album), AlbumContentFragment.TAG)
                .addToBackStack(null)
                .commit();

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("onAlbumClick")
                .setAction(album.getTitle())
                .build());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        LogUtils.d(TAG, "onSaveInstanceState ");

        outState.putSerializable(EXTRA_ALBUMS, listAdapter.getAlbums());

        if(listAdapter.getHistory() != null)
            outState.putSerializable(EXTRA_HISTORY, listAdapter.getHistory());

        outState.putBoolean(EXTRA_HASMORE, listAdapter.isHasMore());

        if(query != null){
            outState.putString(EXTRA_QUERY, query);
        }

        if(list != null){
            LinearLayoutManager lm = (LinearLayoutManager) list.getLayoutManager();
            outState.putInt(EXTRA_POSITION, lm.findFirstVisibleItemPosition());
        }

        StateSaveHelper.onSaveInstanceState(getTag(), outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.album_list_menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        if(query != null){
            searchView.setQuery(query, false);
            searchView.setIconified(false);
            refreshLayout.setEnabled(false);
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query, 0);
                refreshLayout.setEnabled(false);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if(query != null){
                    query = null;
                    refreshLayout.setEnabled(true);
                    showListProgress(true);
                    getAlbumList(0);
                }
                return false;
            }
        });
    }

    public interface AlbumListFI{
        void getAlbumList(int offset);
        void search(String query, int offset);
    }
}
