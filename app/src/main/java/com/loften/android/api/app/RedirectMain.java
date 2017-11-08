package com.loften.android.api.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.loften.android.api.R;

public class RedirectMain extends AppCompatActivity implements View.OnClickListener {

    private TextView text;
    private Button clear;
    private Button newView;

    static final int INIT_TEXT_REQUEST = 0;
    static final int NEW_TEXT_REQUEST = 1;

    private String mTextPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redirect_main);
        initView();

        if(!loadPrefs()){
            Intent intent = new Intent(this, RedirectGetter.class);
            startActivityForResult(intent, INIT_TEXT_REQUEST);
        }
    }

    private void initView() {
        text = (TextView) findViewById(R.id.text);
        clear = (Button) findViewById(R.id.clear);
        newView = (Button) findViewById(R.id.newView);

        clear.setOnClickListener(this);
        newView.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == INIT_TEXT_REQUEST){
            if(resultCode == RESULT_CANCELED){
                finish();
            }else{
                loadPrefs();
            }
        }else if(requestCode == NEW_TEXT_REQUEST){
            if(resultCode != RESULT_CANCELED){
                loadPrefs();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear:
                SharedPreferences preferences = getSharedPreferences("RedirectData", 0);
                preferences.edit().remove("text").commit();
                finish();
                break;
            case R.id.newView:
                Intent intent = new Intent(RedirectMain.this, RedirectGetter.class);
                startActivityForResult(intent, NEW_TEXT_REQUEST);
                break;
        }
    }

    private final boolean loadPrefs(){
        SharedPreferences preferences = getSharedPreferences("RedirectData", 0);

        mTextPref = preferences.getString("text", null);
        if (mTextPref != null) {
            text.setText(mTextPref);
            return true;
        }

        return false;
    }
}
