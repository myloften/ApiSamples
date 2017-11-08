package com.loften.android.api.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.loften.android.api.R;

public class TestToolbarActivity extends ToolbarActivity implements View.OnClickListener {

    private Button img_translucent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toolbar_test);
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        /*------------------ SearchView有三种默认展开搜索框的设置方式，区别如下： ------------------*/
        //设置搜索框直接展开显示。左侧有放大镜(在搜索框中) 右侧有叉叉 可以关闭搜索框
        //searchView.setIconified(false);
        //设置搜索框直接展开显示。左侧有放大镜(在搜索框外) 右侧无叉叉 有输入内容后有叉叉 不能关闭搜索框
        //searchView.setIconifiedByDefault(false);
        //设置搜索框直接展开显示。左侧有无放大镜(在搜索框中) 右侧无叉叉 有输入内容后有叉叉 不能关闭搜索框
        //searchView.onActionViewExpanded();

        //设置最大宽度
        //searchView.setMaxWidth(500);
        //设置是否显示搜索框展开时的提交按钮
        //searchView.setSubmitButtonEnabled(true);
        //设置输入框提示语
        //searchView.setQueryHint("搜索");

        //搜索框展开时后面叉叉按钮的点击事件
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Toast.makeText(getApplicationContext(), "Close", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        //搜索图标按钮(打开搜索框的按钮)的点击事件
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Open", Toast.LENGTH_SHORT).show();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(TestToolbarActivity.this, "查询：" + query, Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                newText = newText.isEmpty() ? "" : "输入：" + newText;
                Toast.makeText(TestToolbarActivity.this, newText, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        return true;
    }

    private void initView() {
        mToolbarHelper.setTitle("沉浸式状态栏");
        img_translucent = (Button) findViewById(R.id.img_translucent);

        img_translucent.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_translucent:
                startActivity(new Intent(this, ImgTranslucentActivity.class));
                break;
            default:
                break;
        }
    }
}
