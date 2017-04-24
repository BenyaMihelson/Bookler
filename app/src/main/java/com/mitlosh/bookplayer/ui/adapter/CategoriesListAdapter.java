package com.mitlosh.bookplayer.ui.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mitlosh.bookplayer.BR;
import com.mitlosh.bookplayer.R;
import com.mitlosh.bookplayer.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoriesListAdapter extends RecyclerView.Adapter {

    private static final String TAG = "CategoriesListAdapter";
    private List<Category> list;
    private OnCategoryClickListener onCategoryClickListener;

    public CategoriesListAdapter(){
        list = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BindingHolder(inflate(R.layout.category_item, parent));
    }

    private View inflate(int layout, ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ((BindingHolder)holder).binding.setVariable(BR.category, list.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onCategoryClickListener != null) onCategoryClickListener.onCategoryClick(list.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setCategories(List<Category> categories) {
        this.list = categories;
        notifyDataSetChanged();
    }

    public void setOnCategoryClickListener(OnCategoryClickListener onCategoryClickListener){
        this.onCategoryClickListener = onCategoryClickListener;
    }

    public static class BindingHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;
        public BindingHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
        }
    }

    public interface OnCategoryClickListener{
        void onCategoryClick(Category category);
    }

}
