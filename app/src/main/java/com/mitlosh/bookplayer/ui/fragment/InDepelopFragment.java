package com.mitlosh.bookplayer.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mitlosh.bookplayer.R;

public class InDepelopFragment extends BaseFragment {

    public static final String TAG = "InDepelopFragment";
    private static final String EXTRA_TITLE = "title";

    public static Fragment newInstance(String title){
        Fragment fragment = new InDepelopFragment();
        Bundle args = new Bundle(1);
        args.putString(EXTRA_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_in_develop, container, false);
        setTitle(getArguments().getString(EXTRA_TITLE));
        return view;
    }

    @Override
    protected int getTitleRes() {
        return 0;
    }

    @Override
    protected boolean showFABOnResume() {
        return false;
    }

}
