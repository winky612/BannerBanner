package com.wk.bannerbanner;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import com.wk.banner.BannerLayout;

/**
 * Created by wk on 2017/8/18.
 */

public class GlideImageLoader implements BannerLayout.ImageLoaderListener {
    @Override
    public void displayImage(Context context, String path, ImageView imageView) {
        Glide.with(context).load(path).into(imageView);
    }
}
