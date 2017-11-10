package com.loften.android.api.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.loften.android.api.R;

public class NotifyingController extends AppCompatActivity implements View.OnClickListener {

    private Button notifyStart;
    private Button notifyStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifying_controller);
        initView();
    }

    private void initView() {
        notifyStart = (Button) findViewById(R.id.notifyStart);
        notifyStop = (Button) findViewById(R.id.notifyStop);

        notifyStart.setOnClickListener(this);
        notifyStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.notifyStart:
                startService(new Intent(this, NotifyingService.class));
                break;
            case R.id.notifyStop:
                stopService(new Intent(this, NotifyingService.class));
                break;
        }
    }
}
