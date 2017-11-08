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

public class AlarmService extends AppCompatActivity implements View.OnClickListener {

    private PendingIntent mAlarmSender;

    private Button startAlarm;
    private Button stopAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAlarmSender = PendingIntent.getService(this, 0,
                new Intent(this, AlarmService_Service.class), 0);

        setContentView(R.layout.activity_alarm_service);
        initView();
    }

    private void initView() {
        startAlarm = (Button) findViewById(R.id.start_alarm);
        stopAlarm = (Button) findViewById(R.id.stop_alarm);

        startAlarm.setOnClickListener(this);
        stopAlarm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_alarm:
                //获取从设备boot后经历的时间值
                long firstTime = SystemClock.elapsedRealtime();

                AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
                am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        firstTime, 10*1000, mAlarmSender);

                Toast.makeText(this, R.string.repeating_scheduled, Toast.LENGTH_SHORT).show();
                break;
            case R.id.stop_alarm:
                AlarmManager am1 = (AlarmManager)getSystemService(ALARM_SERVICE);
                am1.cancel(mAlarmSender);

                Toast.makeText(this, R.string.repeating_unscheduled, Toast.LENGTH_SHORT).show();;
                break;
        }
    }
}
