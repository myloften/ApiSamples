package com.loften.android.api.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.loften.android.api.R;

import java.util.Calendar;

/**
 * AlarmManager
 * AlarmManager.ELAPSED_REALTIME表示闹钟在手机睡眠状态下不可用，该状态下闹钟使用相对时间（相对于系统启动开始），状态值为3；
 * AlarmManager.ELAPSED_REALTIME_WAKEUP表示闹钟在睡眠状态下会唤醒系统并执行提示功能，该状态下闹钟也使用相对时间，状态值为2；
 * AlarmManager.RTC表示闹钟在睡眠状态下不可用，该状态下闹钟使用绝对时间，即当前系统时间，状态值为1；
 * AlarmManager.RTC_WAKEUP表示闹钟在睡眠状态下会唤醒系统并执行提示功能，该状态下闹钟使用绝对时间，状态值为0；
 * AlarmManager.POWER_OFF_WAKEUP表示闹钟在手机关机状态下也能正常进行提示功能，所以是5个状态中用的最多的状态之一，该状态下闹钟也是用绝对时间，状态值为4；不过本状态好像受SDK版本影响，某些版本并不支持；
 */
public class AlarmController extends AppCompatActivity implements View.OnClickListener {

    private Button oneShot;
    private Button startRepeating;
    private Button stopRepeating;

    Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_controller);
        initView();
    }

    private void initView() {
        oneShot = (Button) findViewById(R.id.one_shot);
        startRepeating = (Button) findViewById(R.id.start_repeating);
        stopRepeating = (Button) findViewById(R.id.stop_repeating);

        oneShot.setOnClickListener(this);
        startRepeating.setOnClickListener(this);
        stopRepeating.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.one_shot:
                Intent intent = new Intent(this, OneShotAlarm.class);
                PendingIntent sender = PendingIntent.getBroadcast(
                        this, 0, intent, 0);
                //定时10s后闹铃
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.add(Calendar.SECOND, 10);

                //定时闹钟
                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);

                if(mToast != null){
                    mToast.cancel();
                }
                mToast = Toast.makeText(this, R.string.one_shot_scheduled,
                        Toast.LENGTH_LONG);
                mToast.show();
                break;
            case R.id.start_repeating:
                Intent intent1 = new Intent(this, RepeatingAlarm.class);
                PendingIntent sender1 = PendingIntent.getBroadcast(
                        this, 0, intent1, 0);
                //定时10s后闹铃
                long firstTime = SystemClock.elapsedRealtime();
                firstTime += 10*1000;
                //定时闹钟
                AlarmManager am1 = (AlarmManager)getSystemService(ALARM_SERVICE);
                am1.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        firstTime, 10*1000, sender1);
                if(mToast != null){
                    mToast.cancel();
                }
                mToast = Toast.makeText(this, R.string.repeating_scheduled, Toast.LENGTH_SHORT);
                mToast.show();
                break;
            case R.id.stop_repeating:
                Intent intent2 = new Intent(AlarmController.this, RepeatingAlarm.class);
                PendingIntent sender2 = PendingIntent.getBroadcast(AlarmController.this,
                        0, intent2, 0);
                AlarmManager am2 = (AlarmManager)getSystemService(ALARM_SERVICE);
                am2.cancel(sender2);

                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(AlarmController.this, R.string.repeating_unscheduled,
                        Toast.LENGTH_LONG);
                mToast.show();
                break;
        }
    }
}
