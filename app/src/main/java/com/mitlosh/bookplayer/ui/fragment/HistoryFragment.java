package com.mitlosh.bookplayer.ui.fragment;

import android.os.Bundle;

import com.mitlosh.bookplayer.R;

public class HistoryFragment extends AlbumListFragment {

    public static final String TAG = "HistoryFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    protected void getAlbumList(int offset) {
        updateList(prefUtils.getHistory(), false, true);
        showListProgress(false);
    }

    @Override
    protected int getTitleRes() {
        return R.string.history;
    }

}
