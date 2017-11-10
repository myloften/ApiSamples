package com.loften.android.api.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.loften.android.api.MainActivity;
import com.loften.android.api.R;

import java.util.Random;

/**
 * Notification 的基本操作主要有创建、更新、取消这三种。一个 Notification 的必要属性有三项，如果不设置则在运行时会抛出异常：

 * 小图标，通过 setSmallIcon() 方法设置标题，
 * 通过 setContentTitle() 方法设置内容，
 * 通过 setContentText() 方法设置
 * 除了以上三项，其它均为可选项。
 * 虽然如此，但还是应该给 Notification 设置一个 Action ，这样就可以直接跳转到 App 的某个 Activity 、启动一个 Service 或者发送一个 Broadcast。
 * 否则，Notification 仅仅只能起到通知的效果，而不能与用户交互。

 * 当系统接收到通知时，可以通过震动、响铃、呼吸灯等多种方式进行提醒。
 *
 * 关于 setSmallIcon() 与 setLargeIcon()。在 NotificationCompat.Builder 中有设置通知的大小图标的两个方法。
 * 这两个方法有什么区别呢？当 setSmallIcon() 与 setLargeIcon() 同时存在时, smallIcon 显示在通知的右下角, largeIcon 显示在左侧；
 * 当只设置 setSmallIcon() 时, smallIcon 显示在左侧。对于部分 ROM ，可能修改过源码，如 MIUI 上通知的大图标和小图标是没有区别的
 */

/**
 * PendingIntent 具有以下几种 flag：

 FLAG_CANCEL_CURRENT:如果当前系统中已经存在一个相同的 PendingIntent 对象，那么就将先将已有的 PendingIntent 取消，
 然后重新生成一个 PendingIntent 对象。

 FLAG_NO_CREATE:如果当前系统中不存在相同的 PendingIntent 对象，系统将不会创建该 PendingIntent 对象而是直接返回 null 。

 FLAG_ONE_SHOT:该 PendingIntent 只作用一次。

 FLAG_UPDATE_CURRENT:如果系统中已存在该 PendingIntent 对象，那么系统将保留该 PendingIntent 对象，
 但是会使用新的 Intent 来更新之前 PendingIntent 中的 Intent 对象数据，例如更新 Intent 中的 Extras 。
 */

/**
 *  更新 Notification

 更新通知很简单，只需要再次发送相同 ID 的通知即可，如果之前的通知还未被取消，则会直接更新该通知相关的属性；
 如果之前的通知已经被取消，则会重新创建一个新通知。

 取消 Notification

 取消通知有如下 5 种方式：

 点击通知栏的清除按钮，会清除所有可清除的通知
 设置了 setAutoCancel() 或 FLAG_AUTO_CANCEL 的通知，点击该通知时会清除它
 通过 NotificationManager 调用 cancel(int id) 方法清除指定 ID 的通知
 通过 NotificationManager 调用 cancel(String tag, int id) 方法清除指定 TAG 和 ID 的通知
 通过 NotificationManager 调用 cancelAll() 方法清除所有该应用之前发送的通知
 如果你是通过 NotificationManager.notify(String tag, int id, Notification notify) 方法创建的通知，
 那么只能通过 NotificationManager.cancel(String tag, int id) 方法才能清除对应的通知，
 调用NotificationManager.cancel(int id) 无效。
 */

/**
 * 设置 Notification 的通知效果
 * Notification 有震动、响铃、呼吸灯三种响铃效果，可以通过 setDefaults(int defualts) 方法来设置。
 * Default 属性有以下四种，一旦设置了 Default 效果，自定义的效果就会失效
 * //添加默认震动效果,需要申请震动权限
   //<uses-permission android:name="android.permission.VIBRATE" />
   Notification.DEFAULT_VIBRATE

   //添加系统默认声音效果，设置此值后，调用setSound()设置自定义声音无效
   Notification.DEFAULT_SOUND

   //添加默认呼吸灯效果，使用时须与 Notification.FLAG_SHOW_LIGHTS 结合使用，否则无效
   Notification.DEFAULT_LIGHTS

   //添加上述三种默认提醒效果
   Notification.DEFAULT_ALL
 */
public class IncomingMessage extends AppCompatActivity implements View.OnClickListener {

    private Button notifyApp;
    private Button notifyInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_message);
        initView();
    }

    private void initView() {
        notifyApp = (Button) findViewById(R.id.notify_app);
        notifyInterstitial = (Button) findViewById(R.id.notify_interstitial);

        notifyApp.setOnClickListener(this);
        notifyInterstitial.setOnClickListener(this);
    }

    static Intent[] makeMessageIntentStack(Context context, CharSequence from,
                                           CharSequence msg){
        Intent[] intents = new Intent[4];

        intents[0] = Intent.makeRestartActivityTask(new ComponentName(context,
                com.loften.android.api.MainActivity.class));

        intents[1] = new Intent(context, com.loften.android.api.MainActivity.class);
        intents[1].putExtra(MainActivity.MY_APIS_PATH, "App");

        intents[2] = new Intent(context, com.loften.android.api.MainActivity.class);
        intents[2].putExtra(MainActivity.MY_APIS_PATH, "App/Notification");

        intents[3] = new Intent(context, IncomingMessageView.class);
        intents[3].putExtra(IncomingMessageView.KEY_FROM, from);
        intents[3].putExtra(IncomingMessageView.KEY_MESSAGE, msg);

        return intents;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.notify_app:
                showAppNotification();
                break;
            case R.id.notify_interstitial:
                showInterstitialNotification();
                break;
        }
    }

    private void showAppNotification() {
        //获取NotificationManager实例
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        CharSequence from = "loften";
        CharSequence message;
        switch ((new Random().nextInt()) % 3){
            case 0: message = "I don't ever forget you？";break;
            case 1: message = "I am near you";break;
            default: message = "Can you have dinner with me?";
        }

        PendingIntent contentIntent = PendingIntent.getActivities(this, 0,
                makeMessageIntentStack(this, from, message), PendingIntent.FLAG_CANCEL_CURRENT);

        String tickerText = getString(R.string.imcoming_message_ticker_text, message);
        //实例化NotificationCompat.Builde并设置相关属性
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.head)
                .setTicker(tickerText)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(from)
                .setContentText(message)
                .setContentIntent(contentIntent);

        notifBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
        //通过builder.build()方法生成Notification对象,并发送通知
        nm.notify(R.string.imcoming_message_ticker_text, notifBuilder.build());

    }

    private void showInterstitialNotification() {
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        CharSequence from = "alisa";
        CharSequence message;
        switch ((new Random().nextInt()) % 3){
            case 0: message = "Have you remember me？";break;
            case 1: message = "relly";break;
            default: message = "ok,see you tonight";
        }

        Intent intent = new Intent(this, IncomingMessageInterstitial.class);
        intent.putExtra(IncomingMessageView.KEY_FROM, from);
        intent.putExtra(IncomingMessageView.KEY_MESSAGE, message);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        String tickerText = getString(R.string.imcoming_message_ticker_text, message);


        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.head)
                .setTicker(tickerText)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(from)
                .setContentText(message)
                .setContentIntent(contentIntent);

        notifBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
        nm.notify(R.string.imcoming_message_ticker_text, notifBuilder.build());
    }
}
