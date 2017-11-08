package com.loften.android.api.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.loften.android.api.R;

/**
 * 调用recreate方法重新创建Activity会比正常启动Activity多调用了onSaveInstanceState
 * ()和onRestoreInstanceState()两个方法，onSaveInstanceState()会在onCreate方法之前调用。
 * 所以可以在onCreate()方法中获取onSaveInstanceState()保存的Theme数据
 *
 */
public class RecreateActivity extends AppCompatActivity implements View.OnClickListener {

    int mCurTheme;
    private Button recreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCurTheme = savedInstanceState.getInt("theme");

            switch (mCurTheme) {
                case android.R.style.Theme_DeviceDefault_Light_NoActionBar:
                    mCurTheme = android.R.style.Theme_DeviceDefault_Dialog;
                    break;
                case android.R.style.Theme_DeviceDefault_Dialog:
                    mCurTheme = android.R.style.Theme_DeviceDefault_Light_NoActionBar;
                    break;
                default:
                    mCurTheme = android.R.style.Theme_DeviceDefault_Dialog;
                    break;
            }
            setTheme(mCurTheme);
        }
        setContentView(R.layout.activity_recreate);
        initView();
    }

    private void initView() {
        recreate = (Button) findViewById(R.id.recreate);

        recreate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.recreate:
                recreate();
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("theme", mCurTheme);
    }
}
