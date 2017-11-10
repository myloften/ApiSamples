package com.loften.android.api.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

import com.loften.android.api.MainActivity;
import com.loften.android.api.R;

public class StatusBarNotifications extends AppCompatActivity implements View.OnClickListener {

    private Button happy;
    private Button neutral;
    private Button sad;
    private Button happyMarquee;
    private Button neutralMarquee;
    private Button sadMarquee;
    private Button happyViews;
    private Button neutralViews;
    private Button sadViews;
    private Button defaultSound;
    private Button defaultVibrate;
    private Button defaultAll;
    private Button clear;

    private NotificationManager mNotificationManager;

    private static int MOOD_NOTIFICATIONS = R.layout.status_bar_notifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_bar_notifications);
        initView();

        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    private void initView() {
        happy = (Button) findViewById(R.id.happy);
        neutral = (Button) findViewById(R.id.neutral);
        sad = (Button) findViewById(R.id.sad);
        happyMarquee = (Button) findViewById(R.id.happyMarquee);
        neutralMarquee = (Button) findViewById(R.id.neutralMarquee);
        sadMarquee = (Button) findViewById(R.id.sadMarquee);
        happyViews = (Button) findViewById(R.id.happyViews);
        neutralViews = (Button) findViewById(R.id.neutralViews);
        sadViews = (Button) findViewById(R.id.sadViews);
        defaultSound = (Button) findViewById(R.id.defaultSound);
        defaultVibrate = (Button) findViewById(R.id.defaultVibrate);
        defaultAll = (Button) findViewById(R.id.defaultAll);
        clear = (Button) findViewById(R.id.clear);

        happy.setOnClickListener(this);
        neutral.setOnClickListener(this);
        sad.setOnClickListener(this);
        happyMarquee.setOnClickListener(this);
        neutralMarquee.setOnClickListener(this);
        sadMarquee.setOnClickListener(this);
        happyViews.setOnClickListener(this);
        neutralViews.setOnClickListener(this);
        sadViews.setOnClickListener(this);
        defaultSound.setOnClickListener(this);
        defaultVibrate.setOnClickListener(this);
        defaultAll.setOnClickListener(this);
        clear.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.happy:
                setMood(R.drawable.stat_happy, R.string.status_bar_notifications_happy_message,
                        false);
                break;
            case R.id.neutral:
                setMood(R.drawable.stat_neutral, R.string.status_bar_notifications_ok_message,
                        false);
                break;
            case R.id.sad:
                setMood(R.drawable.stat_sad, R.string.status_bar_notifications_sad_message, false);
                break;
            case R.id.happyMarquee:
                setMood(R.drawable.stat_happy, R.string.status_bar_notifications_happy_message,
                        true);
                break;
            case R.id.neutralMarquee:
                setMood(R.drawable.stat_neutral, R.string.status_bar_notifications_ok_message, true);
                break;
            case R.id.sadMarquee:
                setMood(R.drawable.stat_sad, R.string.status_bar_notifications_sad_message, true);
                break;
            case R.id.happyViews:
                setMoodView(R.drawable.stat_happy, R.string.status_bar_notifications_happy_message);
                break;
            case R.id.neutralViews:
                setMoodView(R.drawable.stat_neutral, R.string.status_bar_notifications_ok_message);
                break;
            case R.id.sadViews:
                setMoodView(R.drawable.stat_sad, R.string.status_bar_notifications_sad_message);
                break;
            case R.id.defaultSound:
                setDefault(Notification.DEFAULT_SOUND);
                break;
            case R.id.defaultVibrate:
                setDefault(Notification.DEFAULT_VIBRATE);
                break;
            case R.id.defaultAll:
                setDefault(Notification.DEFAULT_ALL);
                break;
            case R.id.clear:
                mNotificationManager.cancel(R.layout.status_bar_notifications);
                break;
        }
    }

    private PendingIntent makeMoodIntent(int moodId){
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NotificationDisplay.class).putExtra("moodimg", moodId),
                PendingIntent.FLAG_CANCEL_CURRENT);
        return contentIntent;
    }

    private void setMood(int moodId, int textId, boolean showTicker){
        CharSequence text = getText(textId);

        CharSequence title = getText(R.string.status_bar_notifications_mood_title);

        Notification.Builder notifBuilder = new Notification.Builder(this)
                .setSmallIcon(moodId)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(makeMoodIntent(moodId));

        if(showTicker){
            notifBuilder.setTicker(getString(textId));
        }

        mNotificationManager.notify(MOOD_NOTIFICATIONS, notifBuilder.build());
    }

    /**
     * 自定义通知栏布局
     */
    private void setMoodView(int moodId, int textId){
        Notification notif = new Notification();

        notif.contentIntent = makeMoodIntent(moodId);

        CharSequence text = getText(textId);
        notif.tickerText = text;

        notif.icon = moodId;

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.status_bar_balloon);
        contentView.setTextViewText(R.id.text, text);
        contentView.setImageViewResource(R.id.icon, moodId);
        notif.contentView = contentView;

        mNotificationManager.notify(MOOD_NOTIFICATIONS, notif);
    }

    private PendingIntent makeDefaultIntent() {
        Intent[] intents = new Intent[4];

        intents[0] = Intent.makeRestartActivityTask(new ComponentName(this,
                com.loften.android.api.MainActivity.class));

        intents[1] = new Intent(this, com.loften.android.api.MainActivity.class);
        intents[1].putExtra(MainActivity.MY_APIS_PATH, "App");

        intents[2] = new Intent(this, com.loften.android.api.MainActivity.class);
        intents[2].putExtra(MainActivity.MY_APIS_PATH, "App/Notification");

        intents[3] = new Intent(this, StatusBarNotifications.class);

        PendingIntent contentIntent = PendingIntent.getActivities(this, 0,
                intents, PendingIntent.FLAG_UPDATE_CURRENT);
        return contentIntent;
    }

    private void setDefault(int defaults) {

        PendingIntent contentIntent = makeDefaultIntent();

        CharSequence text = getText(R.string.status_bar_notifications_happy_message);

        CharSequence title = getText(R.string.status_bar_notifications_mood_title);

        Notification notification = new Notification.Builder(this) // the context to use
                .setSmallIcon(R.drawable.stat_happy)  // 设置小图标
                .setTicker(text)  // 在状态栏显示的通知内容
                .setWhen(System.currentTimeMillis())  // 设置通知时间，默认为系统发出通知的时间，通常不用设置
                .setContentTitle(title)  // 设置通知标题
                .setContentText(text)  // 设置通知内容
                .setContentIntent(contentIntent)  // 设置 Action
                .setDefaults(defaults)
                .build();

        mNotificationManager.notify(
                MOOD_NOTIFICATIONS,
                notification);
    }
}
