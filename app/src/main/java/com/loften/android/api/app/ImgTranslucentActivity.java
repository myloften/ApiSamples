package com.loften.android.api.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;

import com.loften.android.api.R;

public class ImgTranslucentActivity extends ToolbarActivity {

    private ConstraintLayout clImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_translucent);
        initView();
        Intent i = getIntent();
        if(!TextUtils.isEmpty(i.getStringExtra("flag"))){
            switch (i.getStringExtra("flag")){
                case "1":
                    clImg.setBackgroundResource(R.drawable.lufei);
                    break;
                case "2":
                    clImg.setBackgroundResource(R.drawable.suolong);
                    break;
                case "3":
                    clImg.setBackgroundResource(R.drawable.luobin);
                    break;
                default:
                    break;
            }
        }
    }

    private void initView() {
        clImg = (ConstraintLayout) findViewById(R.id.cl_img);
    }
}
