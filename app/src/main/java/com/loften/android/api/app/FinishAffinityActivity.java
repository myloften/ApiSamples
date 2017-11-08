package com.loften.android.api.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.loften.android.api.R;

public class FinishAffinityActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView seq;
    private Button next;
    private Button finish;

    int mNesting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_affinity);
        initView();

        mNesting = getIntent().getIntExtra("nesting", 1);
        seq.setText("Current nesting: " + mNesting);
    }

    private void initView() {
        seq = (TextView) findViewById(R.id.seq);
        next = (Button) findViewById(R.id.next);
        finish = (Button) findViewById(R.id.finish);

        next.setOnClickListener(this);
        finish.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                Intent intent = new Intent(this, FinishAffinityActivity.class);
                intent.putExtra("nesting", mNesting+1);
                startActivity(intent);
                break;
            case R.id.finish:
                finishAffinity();
                break;
            default:
                break;
        }
    }
}
