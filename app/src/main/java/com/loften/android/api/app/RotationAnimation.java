package com.loften.android.api.app;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import com.loften.android.api.R;

public class RotationAnimation extends AppCompatActivity {

    private int mRotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_ROTATE;
    private CheckBox windowFullscreen;
    private RadioGroup rotationRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRotationAnimation(mRotationAnimation);
        setContentView(R.layout.activity_rotation_animation);
        initView();
    }

    private void setFullscreen(boolean on){
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if(on){
            winParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }else{
            winParams.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        win.setAttributes(winParams);
    }

    private void setRotationAnimation(int rotationAnimation) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.rotationAnimation = rotationAnimation;
        win.setAttributes(winParams);
    }

    private void initView() {
        windowFullscreen = (CheckBox) findViewById(R.id.windowFullscreen);
        rotationRadioGroup = (RadioGroup) findViewById(R.id.rotation_radio_group);
        windowFullscreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setFullscreen(b);
            }
        });

        rotationRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (i){
                    default:
                    case R.id.rotate:
                        mRotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_ROTATE;
                        break;
                    case R.id.crossfade:
                        mRotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE;
                        break;
                    case R.id.jumpcut:
                        mRotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_JUMPCUT;
                        break;
                }
                setRotationAnimation(mRotationAnimation);
            }
        });
    }
}
