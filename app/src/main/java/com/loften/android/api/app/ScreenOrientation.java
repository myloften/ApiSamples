package com.loften.android.api.app;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.loften.android.api.R;

public class ScreenOrientation extends AppCompatActivity {

    private Spinner orientation;

    //这数组必须对应在res/values/arrays.xml中找到
    final static int mOrientationValues[] = new int[] {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,//默认值。由系统选择显示方向。在不同设备可能有不同
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,//横向
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,//纵向
            ActivityInfo.SCREEN_ORIENTATION_USER,//用户当前的首选方向
            ActivityInfo.SCREEN_ORIENTATION_BEHIND,//与在活动堆栈下的活动相同方向
            ActivityInfo.SCREEN_ORIENTATION_SENSOR,//根据物理方向传感器确定方向
            ActivityInfo.SCREEN_ORIENTATION_NOSENSOR,
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE,
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT,
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR,
            ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE,
            ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT,
            ActivityInfo.SCREEN_ORIENTATION_FULL_USER,
            ActivityInfo.SCREEN_ORIENTATION_LOCKED,
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_orientation);
        initView();
    }

    private void initView() {
        orientation = (Spinner) findViewById(R.id.orientation);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,R.array.screen_orientations,android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orientation.setAdapter(adapter);
        orientation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setRequestedOrientation(mOrientationValues[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        });
    }
}
