package com.loften.android.api.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.loften.android.api.R;

public class RedirectEnter extends AppCompatActivity implements View.OnClickListener {

    private Button go;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redirect_enter);
        initView();
    }

    private void initView() {
        go = (Button) findViewById(R.id.go);

        go.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go:
                Intent intent = new Intent(this, RedirectMain.class);
                startActivity(intent);
                break;
        }
    }
}
