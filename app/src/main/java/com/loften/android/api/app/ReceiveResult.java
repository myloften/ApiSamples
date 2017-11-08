package com.loften.android.api.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.loften.android.api.R;

public class ReceiveResult extends AppCompatActivity implements View.OnClickListener {

    private TextView result;
    private Button get;

    static final private int GET_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_result);
        initView();
    }

    private void initView() {
        result = (TextView) findViewById(R.id.result);
        result.setText(result.getText(), TextView.BufferType.EDITABLE);
        get = (Button) findViewById(R.id.get);
        get.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get:
                Intent intent = new Intent(this, SendResult.class);
                startActivityForResult(intent, GET_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GET_CODE){
            Editable text = (Editable)result.getText();
            if(resultCode == RESULT_CANCELED){
                text.append("cancelled");
            }else{
                text.append("(okay ");
                text.append(Integer.toString(resultCode));
                text.append(") ");
                if(data != null){
                    text.append(data.getAction());
                }
            }

            text.append("\n");
        }
    }
}
