package com.loften.android.api.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.loften.android.api.R;

public class RedirectGetter extends AppCompatActivity implements View.OnClickListener {

    private EditText text;
    private Button apply;
    private String mTextPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redirect_getter);
        initView();
        loadPrefs();
    }

    private void initView() {
        text = (EditText) findViewById(R.id.text);
        apply = (Button) findViewById(R.id.apply);

        apply.setOnClickListener(this);
    }

    private void loadPrefs(){
        SharedPreferences preferences = getSharedPreferences("RedirectData", 0);

        mTextPref = preferences.getString("text", null);
        if (mTextPref != null) {
            text.setText(mTextPref);
        } else {
            text.setText("");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.apply:
                SharedPreferences preferences = getSharedPreferences("RedirectData", 0);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("text", text.getText().toString());

                if (editor.commit()) {
                    setResult(RESULT_OK);
                }

                finish();
                break;
        }
    }

}
