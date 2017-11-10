package com.loften.android.api.app;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.loften.android.api.R;

public class NotificationDisplay extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RelativeLayout container = new RelativeLayout(this);

        ImageButton button = new ImageButton(this);
        button.setImageResource(getIntent().getIntExtra("moodimg", 0));
        button.setOnClickListener(this);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);

        container.addView(button, lp);

        setContentView(container);
    }

    @Override
    public void onClick(View view) {
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
                .cancel(R.layout.status_bar_notifications);

        Intent intent = new Intent(this, StatusBarNotifications.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        finish();
    }
}
