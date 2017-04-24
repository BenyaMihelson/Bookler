package com.mitlosh.bookplayer.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mitlosh.bookplayer.R;

public abstract class BaseListFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = "BaseListFragment";

    protected RecyclerView list;
    protected SwipeRefreshLayout refreshLayout;
    protected TextView emptyText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list, container, false);

        list = (RecyclerView) v.findViewById(R.id.list);
        refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        emptyText = (TextView) v.findViewById(R.id.tvEmptyText);

        setupList();

        showListProgress(true);

        reloadAll();
        return v;
    }

    protected void setupList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(layoutManager);

        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(R.color.colorAccent);

        list.setAdapter(provideListAdapter());
    }

    public void showListProgress(final boolean show){
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(show);
            }
        });
    }

    public void setEmptyText(String msg) {
        emptyText.setVisibility(list.getAdapter().getItemCount() == getDeltaItemCount() ? View.VISIBLE : View.GONE);
        emptyText.setText(msg != null ? msg : getString(R.string.empty_list));
    }

    protected int getDeltaItemCount() {
        return 0;
    }

    @Override
    public void onRefresh() {
        emptyText.setVisibility(View.GONE);
        reloadAll();
    }

    protected abstract RecyclerView.Adapter provideListAdapter();
    protected abstract void reloadAll();
}
