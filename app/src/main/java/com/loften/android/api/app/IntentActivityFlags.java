package com.loften.android.api.app;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.loften.android.api.R;

public class IntentActivityFlags extends AppCompatActivity implements View.OnClickListener {

    private Button flagActivityClearTask;
    private Button flagActivityClearTaskPi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent_flags);
        initView();
    }

    private void initView() {
        flagActivityClearTask = (Button) findViewById(R.id.flag_activity_clear_task);
        flagActivityClearTaskPi = (Button) findViewById(R.id.flag_activity_clear_task_pi);

        flagActivityClearTask.setOnClickListener(this);
        flagActivityClearTaskPi.setOnClickListener(this);
    }

    private Intent[] buildIntentsToViewsLists() {
        Intent[] intents = new Intent[3];

        Intent i1 = new Intent(this, ImgTranslucentActivity.class);
        i1.putExtra("flag", "1");
        intents[0] = i1;

        Intent i2 = new Intent(this, ImgTranslucentActivity.class);
        i2.putExtra("flag", "2");
        intents[1] = i2;

        Intent i3 = new Intent(this, ImgTranslucentActivity.class);
        i3.putExtra("flag", "3");
        intents[2] = i3;

        return intents;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.flag_activity_clear_task:
                startActivities(buildIntentsToViewsLists());
                break;
            case R.id.flag_activity_clear_task_pi:
                PendingIntent pi = PendingIntent.getActivities(this, 0,
                        buildIntentsToViewsLists(), PendingIntent.FLAG_UPDATE_CURRENT);
                try {
                    pi.send();
                } catch (PendingIntent.CanceledException e) {
                    Log.w("IntentActivityFlags", "Failed sending PendingIntent", e);
                }
                break;
            default:
                break;
        }
    }
}
