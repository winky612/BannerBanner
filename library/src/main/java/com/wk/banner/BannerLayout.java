package com.wk.banner;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wk on 2017/8/16.
 * 自定义控件步骤：1-在values中新建attr.xml，并定义属性值
 * 2-定义控件&属性
 * 3-在构造方法中获取自定义属性（通过TypedArray），并把属性值赋给控件(控件别忘了初始化)
 * 4-将控件添加到viewGroup中（LayoutParams  将单个指示器---->添加到LeanerLayout容器&将指示器容器---->加到BannerLayou）
 * <p>
 * 4.1-先想好，谁？以啥方式加入到布局？ 初始化LayoutParams，并设置控件的？宽、高,addRule  setMargin等属性
 * 4.2-addView(某控件，params)方法————将params属性给某控件，利用这个params来加入到viewGroup（布局）中
 * <p>
 * 5-在layout 的 xml中引用自定义的控件（给自定义控件一个命名空间，这样就可以引用我们自定义的属性了（android studio中res-auto即可））
 * 6-给自定义控件添加点击事件
 */

public class BannerLayout extends RelativeLayout {

    private static final int WHAT_AUTO_PLAY = 1000;
    //定义在attrs.xml中声明的属性，并赋予初始值
    private int selectedIndicatorColor = 0xffff0000;
    private int unSelectedIndicatorColor = 0x88888888;
    private int selectedIndicatorHeight = 6;
    private int unSelectedIndicatorHeight = 6;
    private int selectedIndicatorWidth = 6;
    private int unselectedIndicatorWidth = 6;
    private Shape indicatorShape = Shape.oval;
    private Position indicatorPosition = Position.centerBottom;
    private int autoPlayDuration = 4000;
    private int scrollDuration = 900;
    private int indicatorSpace = 3;
    private int indicatorMargin = 10;
    private boolean isAutoPlay = true;


    private Drawable unSelectedDrawable;
    private Drawable selectedDrawable;
    private int itemCount;

    private ViewPager pager;

    private OnViewItemClickListener viewItemClickListener;
    private ImageLoaderListener imageLoaderListener;


    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == WHAT_AUTO_PLAY) {
                if (pager != null && isAutoPlay) {
                    pager.setCurrentItem(pager.getCurrentItem() + 1, true);
                    handler.sendEmptyMessageDelayed(WHAT_AUTO_PLAY, autoPlayDuration);//接着发送延时消息，死循环
                }

            }
            return false;
        }
    });
    private LinearLayout indicatorContainer;
    private int currentPosition;


    private enum Shape {
        rect, oval
    }

    private enum Position {
        centerBottom, rightBottom, leftBottom, centerTop, rightTop, leftTop
    }


    public BannerLayout(Context context) {
        this(context,null,0);//注意！！此处填this() 调3个参数的构造方法
    }

    public BannerLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BannerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //在构造方法中，获取自定义属性，并把属性值赋给控件
        init(attrs, defStyleAttr);

    }

    private void init(AttributeSet attrs, int defStyleAttr) {

        //将在xml中定义的属性值映射到自定义属性中，里面包含了自定义的属性和值
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.BannerLayout, defStyleAttr, 0);

        //从TypedArray中获取自定义属性值 通过键值对存储
        //通过下划线连接 形成属性名
        selectedIndicatorColor = typedArray
                .getColor(R.styleable.BannerLayout_selectedIndicatorColor, selectedIndicatorColor);
        unSelectedIndicatorColor = typedArray
                .getColor(R.styleable.BannerLayout_unSelectedIndicatorColor, unSelectedIndicatorColor);
        selectedIndicatorHeight = (int) typedArray
                .getDimension(R.styleable.BannerLayout_selectedIndicatorHeight, selectedIndicatorHeight);
        unSelectedIndicatorHeight = (int) typedArray
                .getDimension(R.styleable.BannerLayout_unSelectedIndicatorHeight, unSelectedIndicatorHeight);
        selectedIndicatorWidth = (int) typedArray
                .getDimension(R.styleable.BannerLayout_selectedIndicatorWidth, selectedIndicatorWidth);
        unselectedIndicatorWidth = (int) typedArray
                .getDimension(R.styleable.BannerLayout_unSelectedIndicatorWidth, unselectedIndicatorWidth);
        indicatorMargin = (int) typedArray
                .getDimension(R.styleable.BannerLayout_indicatorMargin, indicatorMargin);
        indicatorSpace = (int) typedArray.
                getDimension(R.styleable.BannerLayout_indicatorSpace, indicatorSpace);
        autoPlayDuration = typedArray
                .getInt(R.styleable.BannerLayout_autoPlayDuration, autoPlayDuration);
        scrollDuration = typedArray
                .getInt(R.styleable.BannerLayout_scrollDuration, scrollDuration);
        isAutoPlay = typedArray
                .getBoolean(R.styleable.BannerLayout_isAutoPlay, isAutoPlay);

        //
        int shape = typedArray.getInt(R.styleable.BannerLayout_indicatorShape, Shape.oval.ordinal());
        for (Shape shape1 : Shape.values()) {
            if (shape1.ordinal() == shape) {
                indicatorShape = shape1;
                break;
            }

        }

        int position = typedArray.getInt(R.styleable.BannerLayout_indicatorPosition, Position.centerBottom.ordinal());
        for (Position position1 : Position.values()) {
            if (position1.ordinal() == position) {
                indicatorPosition = position1;
                break;
            }
        }
        typedArray.recycle();

        //绘制指示器
        //定义所需要使用的控件并初始化
        LayerDrawable selectedLayerDrawable;
        LayerDrawable unSelectedLayerDrawable;

        GradientDrawable selectedGradientDrawable;
        GradientDrawable unSelectedGradientDrawable;
        selectedGradientDrawable = new GradientDrawable();
        unSelectedGradientDrawable = new GradientDrawable();

        //设置指示器形状、颜色、大小等（将属性值赋给控件）
        switch (indicatorShape) {
            case rect:
                unSelectedGradientDrawable.setShape(GradientDrawable.RECTANGLE);
                selectedGradientDrawable.setShape(GradientDrawable.RECTANGLE);
                break;
            case oval:
                selectedGradientDrawable.setShape(GradientDrawable.OVAL);
                unSelectedGradientDrawable.setShape(GradientDrawable.OVAL);
                break;
        }

        unSelectedGradientDrawable.setColor(unSelectedIndicatorColor);
        selectedGradientDrawable.setColor(selectedIndicatorColor);

        unSelectedGradientDrawable.setSize(unselectedIndicatorWidth, unSelectedIndicatorHeight);
        selectedGradientDrawable.setSize(selectedIndicatorWidth, selectedIndicatorHeight);

        //绘制一次即可 将Drawable存起来使用
        unSelectedLayerDrawable = new LayerDrawable(new Drawable[]{unSelectedGradientDrawable});
        unSelectedDrawable = unSelectedLayerDrawable;

        selectedLayerDrawable = new LayerDrawable(new Drawable[]{selectedGradientDrawable});
        selectedDrawable = selectedLayerDrawable;
    }

    public void setViewUrls(List<String> urls,ImageLoaderListener loaderListener){
        setImageLoaderListener(loaderListener);
        setViewUrls(urls);
    }

    //添加网络图片路径（用url换view,并将view设置到页面）
    private void setViewUrls(List<String> urls) {
        List<View> views = new ArrayList<>();
        itemCount = urls.size();

        //主要是解决当item为小于3个的时候滑动有问题，这里将其拼凑成3个以上
        //viewPager会默认加载当前页面左右的2个视图，若只有2个页面，则会出现因无法缓存另一个页面，而出现空白（加载需要时间）
        if (itemCount < 1) {
            throw new IllegalStateException("item count not equal zero");
        } else if (itemCount < 2) {

            views.add(getImageView(urls.get(0), 0));
            views.add(getImageView(urls.get(0), 0));
            views.add(getImageView(urls.get(0), 0));

        } else if (itemCount < 3) {
            views.add(getImageView(urls.get(0), 0));
            views.add(getImageView(urls.get(1), 1));
            views.add(getImageView(urls.get(0), 0));
            views.add(getImageView(urls.get(1), 1));

        } else {
            for (int i = 0; i < urls.size(); i++) {
                views.add(getImageView(urls.get(i), i));

            }
        }
        setViews(views);


    }


    //@NonNull————指明一个参数，字段或者方法的返回值不可以为null

    //将url----->转化成view
    private View getImageView(String url, final int position) {
        ImageView imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (imageLoaderListener != null) {
            imageLoaderListener.displayImage(getContext(), url, imageView);
        }

        //单个图片View点击事件
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewItemClickListener != null) {
                    viewItemClickListener.onItemClick(position);
                }
            }
        });

        return imageView;
    }



    private void setImageLoaderListener(ImageLoaderListener loaderListener) {
        this.imageLoaderListener = loaderListener;

    }

    public interface ImageLoaderListener {
        void displayImage(Context context, String path, ImageView imageView);

    }

    public void setOnViewItemClickListener(OnViewItemClickListener listener) {
        this.viewItemClickListener = listener;
    }

    public interface OnViewItemClickListener {
        void onItemClick(int position);
    }


    //添加任意View视图
    //将控件添加到viewGroup中(将单个指示器---->添加到LeanerLayout容器&将指示器容器---->加到BannerLayout)
    private void setViews(List<View> views) {

        pager = new ViewPager(getContext());
        //添加viewpager到BannerLayout;addView()是viewGroup的方法 效果等同于在xml中添加一个view
        addView(pager);

        //装指示器的容器
        indicatorContainer = new LinearLayout(getContext());
        indicatorContainer.setGravity(Gravity.CENTER_VERTICAL);

        //指示器容器以什么方式添加到布局
        //两个属性必须分开添加不能一起写成RelativeLayout.ALIGN_PARENT_RIGHT|RelativeLayout.ALIGN_PARENT_BOTTOM

        RelativeLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        switch (indicatorPosition) {
            case centerBottom:
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                break;
            case centerTop:
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                break;
            case leftBottom:
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                break;
            case leftTop:
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                break;
            case rightBottom://右下
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                break;
            case rightTop:
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                break;
        }

        params.setMargins(indicatorMargin, indicatorMargin, indicatorMargin, indicatorMargin);

        addView(indicatorContainer, params);

        //单个指示器以何种方式添加到布局中
        for (int i = 0; i < itemCount; i++) {
            ImageView indicatorItem = new ImageView(getContext());
            indicatorItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            indicatorItem.setPadding(indicatorSpace, indicatorSpace, indicatorSpace, indicatorSpace);
            indicatorItem.setImageDrawable(unSelectedDrawable);//设置背景图片
//            indicatorItem.setBackgroundColor(Color.BLACK);
            indicatorContainer.addView(indicatorItem);
        }


        LoopPagerAdapter adapter = new LoopPagerAdapter(views);
        pager.setAdapter(adapter);


        //设置当前item到Integer.MAX_VALUE中间的一个值，看起来像无论是往前滑还是往后滑都是ok的
        //如果不设置，用户往左边滑动的时候已经划不动了
        currentPosition = Integer.MAX_VALUE / 2 - Integer.MAX_VALUE / 2 % itemCount;
        pager.setCurrentItem(currentPosition);

        switchIndicator(currentPosition);//??????????????

        //添加每当页面更改或逐步滚动时将调用的侦听器  ????????????????????????????????????
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);///??????
                currentPosition = position;
                switchIndicator(currentPosition % itemCount);//////////???
            }

        });

        if (isAutoPlay) {
            startAutoPlay();
        }

        //设置滑动速度
        setSliderTransformDuration(scrollDuration);


    }


    /**
     * 切换指示器状态
     *
     * @param position 当前位置
     */
    private void switchIndicator(int position) {
        for (int i = 0; i < indicatorContainer.getChildCount(); i++) {

            ((ImageView) indicatorContainer.getChildAt(i)).setImageDrawable(i == position ? selectedDrawable : unSelectedDrawable);

        }


    }

    // 2-改变原生ViewPager切换速度
    //通过反射拿到ViewPager的滑动器，改变duration参数
    private void setSliderTransformDuration(int duration) {
        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);

            FixedSpeedScroller scroller = new FixedSpeedScroller(getContext(), null, duration);
            field.set(pager, scroller);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }


    //与当前页面绑定时
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAutoPlay();
    }


    //与当前页面解绑
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAutoPlay();

    }

    //触摸时应当停止轮播，放开恢复正常
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
               stopAutoPlay();
                break;
            case MotionEvent.ACTION_UP:
                //postDelayed() 多长时间后，执行run()方法中的内容
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startAutoPlay();
                    }
                },5000);

                break;
        }
        return super.dispatchTouchEvent(ev);
    }


    //页面不可见时自动暂停,可见时恢复播放
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            startAutoPlay();
        } else {
            stopAutoPlay();
        }

    }

    //实现自动切换
    //开始自动轮播
    private void startAutoPlay() {
        stopAutoPlay();// 避免重复消息(若发送2次startAutoPlay )
        if (isAutoPlay) {
            handler.sendEmptyMessageDelayed(WHAT_AUTO_PLAY, autoPlayDuration);

        }

    }

    //停止自动轮播
    private void stopAutoPlay() {
        if (pager != null) {
            pager.setCurrentItem(pager.getCurrentItem(), false);//True to smoothly scroll to the new item, false to transition immediately
        }

        if (isAutoPlay) {
            handler.removeMessages(WHAT_AUTO_PLAY);//删除Message Queue中的有待处理的消息
            if (pager != null) {
                pager.setCurrentItem(pager.getCurrentItem(), false);
            }
        }
    }

    // 无限轮播
    // 在设置适配器的时候,getCount返回个非常大的数(Integer.MAX_VALUE), 然后让数据不断的重复再重复的显示.
    // 需要注意的是,一开始的下标不能为0,而是在中间的某个位置,这样才能保证两个方向都可以滑动
    private class LoopPagerAdapter extends PagerAdapter {

        private List<View> views;

        public LoopPagerAdapter(List<View> views) {
            this.views = views;
        }

        /**
         * Return the number of views available.
         */
        @Override
        public int getCount() {
            //Integer.MAX_VALUE = 2147483647
            return Integer.MAX_VALUE;
        }

        /**
         * 该函数用来判断instantiateItem(ViewGroup,int)函数所返回来的Key与一个页面视图
         * 是否是代表的同一个视图(即它俩是否是对应的，对应的表示同一个View)
         * Determines whether a page View is associated with a specific key object
         * as returned by {@link #instantiateItem(ViewGroup, int)}. This method is
         * required for a PagerAdapter to function properly.
         *
         * @param view   Page View to check for association with <code>object</code>
         * @param object Object to check for association with <code>view</code>
         * @return true if <code>view</code> is associated with the key object <code>object</code>
         */
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }


        //这个函数的功能是创建指定位置的页面视图。
        // 适配器的责任就是将创建的view添加到指定的container中，返回值表示的是新增视图页面的key,
        // 一般的情况下我们将创建的视图view返回就可以了。
        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            if (views.size() > 0) {
                //position % view.size()是指虚拟的position会在[0，view.size()）之间循环
                View view = views.get(position % views.size());
                if (container.equals(view.getParent())) {//判断这个view是否已经添加到这个ViewGroup中
                    container.removeView(view);
                }
                container.addView(view);
                return view;
            }

//            return super.instantiateItem(container, position);
            return null;
        }


        //这个方法的功能是是移除一个给定位置的页面。适配器的责任就是从容器中删除这个视图
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }
    }

    //设置固定速度切换页面
    public class FixedSpeedScroller extends Scroller {

        //默认1秒，可以通过setSliderTransformDuration()方法控制
        private int mDuration = 1000;

        public FixedSpeedScroller(Context context) {
            super(context);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }


        public FixedSpeedScroller(Context context, Interpolator interpolator, int duration) {
            this(context, interpolator);
            mDuration = duration;
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }
    }

}
