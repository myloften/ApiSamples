package com.loften.android.api.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.loften.android.api.R;

public class ReorderTwo extends AppCompatActivity implements View.OnClickListener {

    private Button reorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reorder);
        initView();
    }

    private void initView() {
        reorder = (Button) findViewById(R.id.reorder);
        reorder.setText("我是老二，点击去老三");
        reorder.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reorder:
                startActivity(new Intent(this, ReorderThree.class));
                break;
        }
    }
}
