package com.loften.android.api.app;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;

import com.loften.android.api.utils.ToolbarHelper;

public class ToolbarActivity extends AppCompatActivity {

    protected ToolbarHelper mToolbarHelper;
    public Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        mToolbarHelper = new ToolbarHelper(this, layoutResID);
        mToolbar = mToolbarHelper.getToolbar();
        mToolbar.setTitle("");
        mToolbar.setSubtitle("");
        setContentView(mToolbarHelper.getContentView());
        /*把toolbar设置到Activity中*/
        setSupportActionBar(mToolbar);
        /*自定义的一些操作*/
        onCreateCustomToolbar(mToolbar);
    }

    //隐藏返回按钮
    public void hideBackBtn(){
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    private void onCreateCustomToolbar(Toolbar toolbar) {
        toolbar.setContentInsetsRelative(0,0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
