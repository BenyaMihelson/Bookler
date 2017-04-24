package com.mitlosh.bookplayer.ui.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mitlosh.bookplayer.BR;
import com.mitlosh.bookplayer.R;
import com.mitlosh.bookplayer.model.Album;

import java.util.List;

public class HistoryListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final SortedList<Album> mList;
    private boolean hasMore;
    private OnHistoryListListener onHistoryListListener;

    public HistoryListAdapter() {
        mList = new SortedList<>(Album.class, new SortedListAdapterCallback<Album>(this) {
            @Override
            public int compare(Album item1, Album item2) {
                int comp = item1.getStatus() - item2.getStatus();
                if(comp == 0){
                    if(item1.getStatus() == Album.STATUS_NO_LISTENED){
                        comp = -1;
                    }else{
                        comp = (int) (item2.getLastListened() - item1.getLastListened());
                    }
                }
                return comp;
            }

            @Override
            public boolean areContentsTheSame(Album oldItem, Album newItem) {
                return false;
            }

            @Override
            public boolean areItemsTheSame(Album item1, Album item2) {
                return item1 != null && item1.equals(item2);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BindingHolder(inflate(R.layout.history_list_item, parent));
    }

    private View inflate(int layout, ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ((BindingHolder)holder).getBinding().setVariable(BR.album, mList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onHistoryListListener != null)
                    onHistoryListListener.onAlbumClick(mList.get(holder.getAdapterPosition()));
            }
        });
    }

//    private void onBindMoreItemHolder(MoreItemHolder holder) {
//        holder.itemView.setVisibility(hasMore ? View.VISIBLE : View.GONE);
//        holder.itemView.setOnClickListener(!hasMore ? null : new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (onAlbumListListener != null) onAlbumListListener.onLoadMore();
//            }
//        });
//    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void addAll(List<Album> albums, boolean hasMore) {
        mList.beginBatchedUpdates();
        removeAll();
        for (Album album : albums) {
            mList.add(album);
        }
        mList.endBatchedUpdates();

        if(this.hasMore != hasMore){
            this.hasMore = hasMore;
            notifyItemChanged(getItemCount() - 1);
        }
    }

    public void removeAll() {
        for (int i = mList.size() - 1; i >= 0; i--) {
            mList.removeItemAt(i);
        }
    }

    public void setOnHistoryListListener(OnHistoryListListener onHistoryListListener){
        this.onHistoryListListener = onHistoryListListener;
    }

    public static class BindingHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;
        public BindingHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
        }
        public ViewDataBinding getBinding() {
            return binding;
        }
    }

    public interface OnHistoryListListener{
        void onShowMoreHistory();
        void onAlbumClick(Album album);
    }

}
