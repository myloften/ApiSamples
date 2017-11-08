package com.loften.android.api.app;

import android.app.WallpaperManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.loften.android.api.R;

import java.io.IOException;

public class SetWallpaperActivity extends AppCompatActivity implements View.OnClickListener {

    final static private int[] mColors =
            {Color.BLUE, Color.GREEN, Color.RED, Color.LTGRAY, Color.MAGENTA, Color.CYAN,
                    Color.YELLOW, Color.WHITE};
    private ImageView imageview;
    private Button randomize;
    private Button setwallpaper;
    private WallpaperManager wallpaperManager;
    private Drawable wallpaperDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_wallpaper);
        initView();

        wallpaperManager = WallpaperManager.getInstance(this);
        wallpaperDrawable = wallpaperManager.getDrawable();
        //可防止卡顿
        imageview.setDrawingCacheEnabled(true);
        imageview.setImageDrawable(wallpaperDrawable);

    }

    private void initView() {
        imageview = (ImageView) findViewById(R.id.imageview);
        randomize = (Button) findViewById(R.id.randomize);
        setwallpaper = (Button) findViewById(R.id.setwallpaper);

        randomize.setOnClickListener(this);
        setwallpaper.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.randomize:
                int mColor = (int) Math.floor(Math.random() * mColors.length);
                wallpaperDrawable.setColorFilter(mColors[mColor], PorterDuff.Mode.MULTIPLY);
                imageview.setImageDrawable(wallpaperDrawable);
                imageview.invalidate();
                break;
            case R.id.setwallpaper:
                try {
                    wallpaperManager.setBitmap(imageview.getDrawingCache());
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
}
