package com.loften.android.api.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.loften.android.api.R;

public class Animation extends AppCompatActivity implements View.OnClickListener {

    private Button fadeAnimation;
    private Button zoomAnimation;
    private Button modernFadeAnimation;
    private Button modernZoomAnimation;
    private Button scaleUpAnimation;
    private Button zoomThumbnailAnimation;
    private Button noAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation);
        initView();

    }

    private void initView() {
        fadeAnimation = (Button) findViewById(R.id.fade_animation);
        zoomAnimation = (Button) findViewById(R.id.zoom_animation);
        modernFadeAnimation = (Button) findViewById(R.id.modern_fade_animation);
        modernZoomAnimation = (Button) findViewById(R.id.modern_zoom_animation);
        scaleUpAnimation = (Button) findViewById(R.id.scale_up_animation);
        zoomThumbnailAnimation = (Button) findViewById(R.id.zoom_thumbnail_animation);

        fadeAnimation.setOnClickListener(this);
        zoomAnimation.setOnClickListener(this);
        modernFadeAnimation.setOnClickListener(this);
        modernZoomAnimation.setOnClickListener(this);
        scaleUpAnimation.setOnClickListener(this);
        zoomThumbnailAnimation.setOnClickListener(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            modernFadeAnimation.setVisibility(View.GONE);
            modernZoomAnimation.setVisibility(View.GONE);
            scaleUpAnimation.setVisibility(View.GONE);
            zoomThumbnailAnimation.setVisibility(View.GONE);
        }
        noAnimation = (Button) findViewById(R.id.no_animation);
        noAnimation.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fade_animation:
                startActivity(new Intent(this, ImgTranslucentActivity.class));
                overridePendingTransition(R.anim.fade, R.anim.hold);
                break;
            case R.id.zoom_animation:
                startActivity(new Intent(this, ImgTranslucentActivity.class));
                overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
                break;
            case R.id.modern_fade_animation:
                ActivityOptionsCompat opts = ActivityOptionsCompat.makeCustomAnimation(this,
                        R.anim.fade, R.anim.hold);
                startActivity(new Intent(this, ImgTranslucentActivity.class), opts.toBundle());
                break;
            case R.id.modern_zoom_animation:
                ActivityOptionsCompat opts1 = ActivityOptionsCompat.makeCustomAnimation(this,
                        R.anim.zoom_enter, R.anim.zoom_exit);
                startActivity(new Intent(this, ImgTranslucentActivity.class), opts1.toBundle());
                break;
            case R.id.scale_up_animation:
                ActivityOptionsCompat opts2 = ActivityOptionsCompat.makeScaleUpAnimation(v,
                        0, 0, v.getWidth(), v.getHeight());
                startActivity(new Intent(this, ImgTranslucentActivity.class), opts2.toBundle());
                break;
            case R.id.zoom_thumbnail_animation:
                v.setDrawingCacheEnabled(true);
                v.setPressed(false);
                v.refreshDrawableState();
                Bitmap bm = v.getDrawingCache();
                Canvas c = new Canvas(bm);
                //c.drawARGB(255, 255, 0, 0);
                ActivityOptionsCompat opts3 = ActivityOptionsCompat.makeThumbnailScaleUpAnimation(v,
                        bm, 0, 0);
                startActivity(new Intent(this, ImgTranslucentActivity.class), opts3.toBundle());
                break;
            case R.id.no_animation:
                startActivity(new Intent(this, ImgTranslucentActivity.class));
                overridePendingTransition(0, 0);
                break;
            default:
                break;
        }
    }
}
