package com.loften.android.api.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.loften.android.api.R;

public class NotifyWithText extends AppCompatActivity implements View.OnClickListener {

    private Button shortNotify;
    private Button longNotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify_with_text);
        initView();
    }

    private void initView() {
        shortNotify = (Button) findViewById(R.id.short_notify);
        longNotify = (Button) findViewById(R.id.long_notify);

        shortNotify.setOnClickListener(this);
        longNotify.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.short_notify:
                Toast.makeText(NotifyWithText.this, R.string.notify_with_text_short_notify_text,
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.long_notify:
                Toast.makeText(NotifyWithText.this, R.string.notify_with_text_long_notify_text,
                        Toast.LENGTH_LONG).show();
                break;
        }
    }
}
