package com.mitlosh.bookplayer.binding;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class BindAdapter {

    @BindingAdapter({"android:src"})
    public static void loadImage(ImageView view, String url) {
        Picasso.with(view.getContext().getApplicationContext())
                .load(url)
                .fit()
                .into(view);
    }

    @BindingAdapter({"android:src"})
    public static void setImageViewResource(ImageView imageView, int resource) {
        imageView.setImageResource(resource);
    }
}
