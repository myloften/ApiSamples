package com.loften.android.api.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.loften.android.api.R;

public class IncomingMessageInterstitial extends AppCompatActivity implements View.OnClickListener {

    private Button notifyApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_message_interstitial);
        initView();
    }

    private void initView() {
        notifyApp = (Button) findViewById(R.id.notify_app);

        notifyApp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.notify_app:
                switchToApp();
                break;
        }
    }

    void switchToApp(){
        CharSequence from = getIntent().getCharSequenceExtra(IncomingMessageView.KEY_FROM);
        CharSequence msg = getIntent().getCharSequenceExtra(IncomingMessageView.KEY_MESSAGE);

        Intent[] stack = IncomingMessage.makeMessageIntentStack(this, from, msg);
        startActivities(stack);
        finish();
    }
}
