package com.mitlosh.bookplayer.ui.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mitlosh.bookplayer.BR;
import com.mitlosh.bookplayer.R;
import com.mitlosh.bookplayer.model.Track;
import com.mitlosh.bookplayer.viewmodel.TrackViewModel;

import java.util.ArrayList;
import java.util.List;

public class AlbumContentListAdapter extends RecyclerView.Adapter {

    private static final String TAG = "AlbumContentListAdapter";
    private List<Track> content;
    private TrackViewModel.OnTrackClickListener onTrackClickListener;

    public AlbumContentListAdapter(){
        content = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BindingHolder(inflate(R.layout.track_item, parent));
    }

    private View inflate(int layout, ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((BindingHolder)holder).viewModel.setTrack(content.get(position), position);
        ((BindingHolder)holder).viewModel.setOnTrackClickListener(onTrackClickListener);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        ((BindingHolder)holder).viewModel.onDestroy();
        super.onViewRecycled(holder);
    }


    @Override
    public int getItemCount() {
        return content.size();
    }

    public void setContent(List<Track> content) {
        this.content = content;
        notifyDataSetChanged();
    }

    public void setOnTrackClickListener(TrackViewModel.OnTrackClickListener onTrackClickListener){
        this.onTrackClickListener = onTrackClickListener;
    }

    public static class BindingHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;
        private final TrackViewModel viewModel;
        public BindingHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
            viewModel = new TrackViewModel();
            binding.setVariable(BR.track, viewModel);
        }
    }

}
