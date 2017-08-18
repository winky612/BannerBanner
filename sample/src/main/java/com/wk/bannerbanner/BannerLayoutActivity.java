package com.wk.bannerbanner;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import comm.wk.yotalker.library.BannerLayout;

/**
 * Created by wk on 2017/8/18.
 */

public class BannerLayoutActivity extends AppCompatActivity {
    @BindView(R.id.banner1)
    BannerLayout banner1;
    @BindView(R.id.banner2)
    BannerLayout banner2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bannerlayout);
        ButterKnife.bind(this);

        List<String> urls = new ArrayList<>();
        urls.add("http://img3.imgtn.bdimg.com/it/u=2674591031,2960331950&fm=23&gp=0.jpg");
        urls.add("http://img5.imgtn.bdimg.com/it/u=3639664762,1380171059&fm=23&gp=0.jpg");
        urls.add("http://img0.imgtn.bdimg.com/it/u=1095909580,3513610062&fm=23&gp=0.jpg");
        urls.add("http://img4.imgtn.bdimg.com/it/u=1030604573,1579640549&fm=23&gp=0.jpg");
        urls.add("http://img5.imgtn.bdimg.com/it/u=2583054979,2860372508&fm=23&gp=0.jpg");


        banner1.setViewUrls(urls,new BannerLayout.ImageLoaderListener() {
            @Override
            public void displayImage(Context context, String path, ImageView imageView) {
                Picasso.with(context).load(path).into(imageView);
            }
        });


        banner1.setOnViewItemClickListener(new BannerLayout.OnViewItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(BannerLayoutActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();

            }
        });


        //低于3张
        List<String> urls2 = new ArrayList<>();
        urls2.add("http://img3.imgtn.bdimg.com/it/u=2674591031,2960331950&fm=23&gp=0.jpg");
//        urls2.add("http://img5.imgtn.bdimg.com/it/u=3639664762,1380171059&fm=23&gp=0.jpg");

        banner2.setOnViewItemClickListener(new BannerLayout.OnViewItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(BannerLayoutActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();
            }
        });

        banner2.setViewUrls(urls2,new GlideImageLoader());
    }
}
