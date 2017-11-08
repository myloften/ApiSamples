package com.loften.android.api.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.loften.android.api.R;

/**
 * Created by lcw on 2017/10/11.
 */
public class ToolbarHelper {

    /*上下文，创建view的时候用到*/
    private Context mContext;
    /*视图构造器*/
    private LayoutInflater mInflater;
    /*base view*/
    private FrameLayout mContentView;
    /*用户定义的view*/
    private View mUserView;
    /*title*/
    private TextView tv_title;
    /*toolbar*/
    private Toolbar mToolbar;

    public ToolbarHelper(Context context, int layoutId) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        /*初始化整个内容*/
        initContentView();
        /*初始化toolbar*/
        initToolbar();
        /*初始化用户定义的布局*/
        initUserView(layoutId);
    }

    private void initContentView() {
        /*直接创建一个帧布局，作为视图容器的父容器*/
        mContentView = new FrameLayout(mContext);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mContentView.setLayoutParams(params);
    }

    private void initToolbar() {
        /*通过inflater获取toolbar的布局文件*/
        View toolbar = mInflater.inflate(R.layout.toolbar, mContentView);
        mToolbar = (Toolbar) toolbar.findViewById(R.id.tool_bar);
        tv_title = (TextView) toolbar.findViewById(R.id.tv_title);
    }

    private void initUserView(int layoutId) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){

            int statusBarHeight = getStatusBarHeight();
            mToolbar.setPadding(0, statusBarHeight, 0, 0);
            mToolbar.getLayoutParams().height += statusBarHeight;

            //增加20%透明度的背景栏
            View view = new View(mContext);
            view.setBackgroundColor(Color.parseColor("#33000000"));
            mContentView.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight));
        }
        mUserView = mInflater.inflate(layoutId, null);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        /**
         * 获取属性值
         * toolbar 是否悬浮在窗口之上
         */
        TypedArray typedArray = mContext.getTheme().obtainStyledAttributes(new int[]{R.attr.windowActionBarOverlay});
        /*获取主题中定义的悬浮标志*/
        boolean overly = typedArray.getBoolean(0, false);
        /*获取主题中定义的toolbar的高度*/
        int toolbarSize = mToolbar.getLayoutParams().height;
        typedArray.recycle();
        /*如果是悬浮状态，则不需要设置间距*/
        params.topMargin = overly ? 0 : toolbarSize;
        mContentView.addView(mUserView, params);
    }



    public FrameLayout getContentView(){
        return mContentView;
    }

    public Toolbar getToolbar(){
        return mToolbar;
    }

    public void setTitle(CharSequence title) {
        tv_title.setText(title);
    }

    private int getStatusBarHeight() {
        Resources resources = mContext.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

}
