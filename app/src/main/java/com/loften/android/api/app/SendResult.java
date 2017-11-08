package com.loften.android.api.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.loften.android.api.R;

public class SendResult extends AppCompatActivity implements View.OnClickListener {

    private TextView text;
    private Button corky;
    private Button violet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_result);
        initView();
    }

    private void initView() {
        text = (TextView) findViewById(R.id.text);
        corky = (Button) findViewById(R.id.corky);
        violet = (Button) findViewById(R.id.violet);

        corky.setOnClickListener(this);
        violet.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.corky:
                setResult(RESULT_OK, (new Intent()).setAction("Corky!"));
                finish();
                break;
            case R.id.violet:
                setResult(RESULT_OK, (new Intent()).setAction("Violet!"));
                finish();
                break;
        }
    }
}
