package com.mitlosh.bookplayer.ui.fragment;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import com.mitlosh.bookplayer.R;
import com.mitlosh.bookplayer.model.Category;
import com.mitlosh.bookplayer.ui.adapter.CategoriesListAdapter;

import java.util.List;

public class CategoriesFragment extends BaseListFragment implements CategoriesListAdapter.OnCategoryClickListener {

    private CategoriesListAdapter adapter;
    private CategoriesFI callback;

    @Override
    protected RecyclerView.Adapter provideListAdapter() {
        adapter = new CategoriesListAdapter();
        adapter.setOnCategoryClickListener(this);
        return adapter;
    }

    @Override
    protected void reloadAll() {
        if(callback != null) callback.getCategories();
    }

    public void onLoadCategories(List<Category> categoryList) {
        adapter.setCategories(categoryList);
        setEmptyText(null);
    }

    @Override
    public void onCategoryClick(Category category) {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, CategoryAlbumsFragment.newInstance(category), CategoryAlbumsFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected int getTitleRes() {
        return R.string.categories;
    }

    @Override
    protected boolean showFABOnResume() {
        return false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (CategoriesFI) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    public interface CategoriesFI{
        void getCategories();
    }

}
