package com.loften.android.api.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.loften.android.api.R;

public class ReorderOnLaunch extends AppCompatActivity implements View.OnClickListener {

    private Button reorderLaunchTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reorder_on_launch);
        initView();
    }

    private void initView() {
        reorderLaunchTwo = (Button) findViewById(R.id.reorder_launch_two);

        reorderLaunchTwo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reorder_launch_two:
                startActivity(new Intent(this, ReorderTwo.class));
                break;
        }
    }
}
