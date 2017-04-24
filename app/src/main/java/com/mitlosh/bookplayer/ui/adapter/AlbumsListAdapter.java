package com.mitlosh.bookplayer.ui.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mitlosh.bookplayer.BR;
import com.mitlosh.bookplayer.R;
import com.mitlosh.bookplayer.model.Album;

import java.util.ArrayList;
import java.util.List;

public class AlbumsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final SortedList<Item> mList;
    private boolean withHistory;
    private boolean hasMore;
    private OnAlbumListListener onAlbumListListener;
    private ArrayList<Album> history;
    private HistoryListAdapter.OnHistoryListListener onHistoryListListener;


    public AlbumsListAdapter() {
        mList = new SortedList<>(Item.class, new SortedListAdapterCallback<Item>(this) {
            @Override
            public int compare(Item item1, Item item2) {
                if(item1.itemType == item2.itemType && item1.itemType == Item.TYPE_ALBUM_ITEM){
                    int comp = item1.album.getStatus() - item2.album.getStatus();
                    if(comp == 0){
                        if(item1.album.getStatus() == Album.STATUS_NO_LISTENED){
                            comp = (int) (item2.album.getTime() - item1.album.getTime());
                        }else{
                            comp = (int) (item2.album.getLastListened() - item1.album.getLastListened());
                        }
                    }
                    return comp;
                }
                return item1.itemType - item2.itemType;
            }

            @Override
            public boolean areContentsTheSame(Item oldItem, Item newItem) {
                if(oldItem.itemType == newItem.itemType && oldItem.itemType == Item.TYPE_ALBUM_ITEM){
                    return oldItem.album.getTitle().equals(newItem.album.getTitle());
                }
                return false;
            }

            @Override
            public boolean areItemsTheSame(Item item1, Item item2) {
                return item1 != null && item1.equals(item2);
            }
        });

        mList.add(new Item(Item.TYPE_MORE));
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).itemType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case Item.TYPE_HISTORY:
                return new HistoryItemHolder(inflate(R.layout.history_list, parent));
            case Item.TYPE_MORE:
                return new MoreItemHolder(inflate(R.layout.more_item, parent));
            case Item.TYPE_ALBUM_ITEM:
                return new BindingHolder(inflate(R.layout.album_item, parent));
        }
        throw new IllegalArgumentException("Illegal viewType");
    }

    private View inflate(int layout, ViewGroup parent){
        return LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Item curItem = mList.get(position);
        switch (curItem.itemType){
            case Item.TYPE_HISTORY:
                onBindHistoryViewHolder((HistoryItemHolder) holder);
                break;
            case Item.TYPE_ALBUM_ITEM:
                onBindAlbumItemHolder(curItem.album, (BindingHolder) holder);
                break;
            case Item.TYPE_MORE:
                onBindMoreItemHolder((MoreItemHolder) holder);
                break;
        }
    }

    private void onBindHistoryViewHolder(HistoryItemHolder holder) {
        if(history != null){
            holder.listAdapter.addAll(history, false);
            holder.listAdapter.setOnHistoryListListener(onHistoryListListener);
        }else {
            holder.listAdapter.removeAll();
        }
    }

    private void onBindAlbumItemHolder(Album curItem, final BindingHolder holder) {
        holder.getBinding().setVariable(BR.album, curItem);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onAlbumListListener != null) onAlbumListListener.onAlbumClick(mList.get(holder.getAdapterPosition()).album);
            }
        });
    }

    private void onBindMoreItemHolder(MoreItemHolder holder) {
        holder.itemView.setVisibility(hasMore ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(!hasMore ? null : new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onAlbumListListener != null) onAlbumListListener.onLoadMore();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void addAll(List<Album> albums, boolean hasMore, boolean clear) {
        mList.beginBatchedUpdates();
        if(clear) removeAll();
        for (Album album : albums) {
            Item item = new Item(Item.TYPE_ALBUM_ITEM);
            item.album = album;
            mList.add(item);
        }
        if(this.hasMore != hasMore){
            this.hasMore = hasMore;
            mList.updateItemAt(getItemCount() - 1, new Item(Item.TYPE_MORE));
        }
        mList.endBatchedUpdates();
    }

    private void removeAll() {
        for (int i = mList.size() - 1; i >= 0; i--) {
            Item item = mList.get(i);
            if(item.itemType == Item.TYPE_ALBUM_ITEM){
                mList.removeItemAt(i);
            }
        }
    }

    public void setHistory(List<Album> history) {
        withHistory = history != null;
        if(withHistory){
            this.history = new ArrayList<>(history);
            Item item = new Item(Item.TYPE_HISTORY);
            if(mList.get(0).itemType == Item.TYPE_HISTORY){
                mList.updateItemAt(0, item);
            }else{
                mList.add(item);
            }
        }else {
            if(mList.get(0).itemType == Item.TYPE_HISTORY){
                mList.removeItemAt(0);
            }
            this.history = null;
        }
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public ArrayList<Album> getAlbums() {
        ArrayList<Album> albumList = new ArrayList<>();
        for(int i = 0; i < mList.size(); i++){
            Item item = mList.get(i);
            if(item.itemType == Item.TYPE_ALBUM_ITEM){
                albumList.add(item.album);
            }
        }
        return albumList;
    }

    public ArrayList<Album> getHistory() {
        return history;
    }

    public void setOnAlbumListListener(OnAlbumListListener onAlbumListListener){
        this.onAlbumListListener = onAlbumListListener;
    }

    public void setOnHistoryListListener(HistoryListAdapter.OnHistoryListListener onHistoryListListener) {
        this.onHistoryListListener = onHistoryListListener;
    }

    public int getSize() {
        int delta = withHistory() ? 2 : 1;
        return mList.size() - delta;
    }

    public boolean withHistory() {
        return withHistory;
    }

    private class Item {

        private static final int TYPE_HISTORY = 0;
        private static final int TYPE_ALBUM_ITEM = 1;
        private static final int TYPE_MORE = 2;

        int itemType;
        Album album;

        private Item(int itemType){
            this.itemType = itemType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Item item = (Item) o;

            if (itemType != item.itemType) return false;
            return album != null ? album.equals(item.album) : item.album == null;

        }

        @Override
        public int hashCode() {
            int result = itemType;
            result = 31 * result + (album != null ? album.hashCode() : 0);
            return result;
        }
    }

    private static class BindingHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;
        public BindingHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
        }
        public ViewDataBinding getBinding() {
            return binding;
        }
    }

    private static class HistoryItemHolder extends RecyclerView.ViewHolder {
        private final HistoryListAdapter listAdapter;
        public HistoryItemHolder(final View itemView) {
            super(itemView);
            RecyclerView list = (RecyclerView)itemView.findViewById(R.id.history_list);
            LinearLayoutManager layoutManager = new LinearLayoutManager(itemView.getContext());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            list.setLayoutManager(layoutManager);
            listAdapter = new HistoryListAdapter();
            list.setAdapter(listAdapter);
        }
    }

    private static class MoreItemHolder extends RecyclerView.ViewHolder {
        public MoreItemHolder(final View itemView) {
            super(itemView);
        }
    }

    public interface OnAlbumListListener{
        void onLoadMore();
        void onAlbumClick(Album album);
    }

}
