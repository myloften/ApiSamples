package com.loften.android.api.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.loften.android.api.R;

public class Intents extends AppCompatActivity implements View.OnClickListener {

    private TextView text;
    private Button getMusic;
    private Button getImage;
    private Button getStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intents);
        initView();
    }

    private void initView() {
        text = (TextView) findViewById(R.id.text);
        getMusic = (Button) findViewById(R.id.get_music);
        getImage = (Button) findViewById(R.id.get_image);
        getStream = (Button) findViewById(R.id.get_stream);

        getMusic.setOnClickListener(this);
        getImage.setOnClickListener(this);
        getStream.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_music:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                startActivity(Intent.createChooser(intent, getString(R.string.get_music)));
                break;
            case R.id.get_image:
                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                intent1.setType("image/*");
                startActivity(Intent.createChooser(intent1, getString(R.string.get_image)));
                break;
            case R.id.get_stream:
                Intent intent2 = new Intent(Intent.ACTION_GET_CONTENT);
                intent2.setType("*/*");
                startActivity(Intent.createChooser(intent2, getString(R.string.get_stream)));
                break;
            default:
                break;
        }
    }
}
