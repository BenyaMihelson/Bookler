package com.mitlosh.bookplayer.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mitlosh.bookplayer.model.Category;

public class CategoryAlbumsFragment extends AlbumListFragment{

    public static final String TAG = "CategoryAlbumsFragment";

    private static final String EXTRA_CATEGORY = "category";
    private Category category;
    private CategoryAlbumsFI callback;

    public static Fragment newInstance(Category category) {
        Fragment fragment = new CategoryAlbumsFragment();
        Bundle args = new Bundle(1);
        args.putSerializable(EXTRA_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(false);
        category = (Category) getArguments().getSerializable(EXTRA_CATEGORY);
        setTitle(category.getTitle());
        return v;
    }

    @Override
    protected void getAlbumList(int offset) {
        category = (Category) getArguments().getSerializable(EXTRA_CATEGORY);
        if(callback != null) callback.getAlbumsByCategory(category.getId(), offset);
    }

    @Override
    protected int getTitleRes() {
        return 0;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (CategoryAlbumsFI) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    public interface CategoryAlbumsFI{
        void getAlbumsByCategory(int categoryId, int offset);
    }
}
