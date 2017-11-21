package com.loften.android.api.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.loften.android.api.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 本例Foreground Service Controller就显示了一个在前台运行的Service，主要避免被回收， 前台运行的Service可以
 * 通过调用startForeground()使Service在前台运行。stopForeground停止前台运行，但Service本身不会停止。
 * startForeground，stopForeground是从2.0开始支持的，之前的版本采用setForeground。
 * 本例为了支持2.0之前和2.0之后的版本，采用了Reflection的方法来来查找当前版本是否含有startForeground和stopForeground，
 * 如果有则调用，没有则还是使用setForeground。
 */
public class ForegroundService extends Service {

    static final String ACTION_FOREGROUND = "com.loften.android.api.FOREGROUND";
    static final String ACTION_FOREGROUND_WAKELOCK = "com.loften.android.api.FOREGROUND_WAKELOCK";
    static final String ACTION_BACKGROUND = "com.loften.android.api.BACKGROUND";
    static final String ACTION_BACKGROUND_WAKELOCK = "com.loften.android.api.BACKGROUND_WAKELOCK";

    private static final Class<?>[] mSetForegroundSignature = new Class[]{
            boolean.class};
    private static final Class<?>[] mStartForegrounSignature = new Class[]{
            int.class, Notification.class};
    private static final Class<?>[] mStopForegroundSignature = new Class[]{
            boolean.class};

    private NotificationManager mNM;
    private Method mSetForeground;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mSetForegroundArgs = new Object[1];
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];

    void invokeMethod(Method method, Object[] args) {
        try {
            method.invoke(this, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    void startForegroundCompat(int id, Notification notification){
        if(mStartForeground != null){
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            invokeMethod(mStartForeground, mStartForegroundArgs);
            return;
        }

        mSetForegroundArgs[0] = Boolean.TRUE;
        invokeMethod(mSetForeground, mSetForegroundArgs);
        mNM.notify(id, notification);
    }

    void stopForegroundCompat(int id){
        if(mStopForeground != null){
            mStopForegroundArgs[0] = Boolean.TRUE;
            invokeMethod(mStopForeground, mStopForegroundArgs);
            return;
        }

        mNM.cancel(id);
        mSetForegroundArgs[0] = Boolean.FALSE;
        invokeMethod(mSetForeground, mSetForegroundArgs);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        try {
            mStartForeground = getClass().getMethod("startForeground",
                    mStartForegrounSignature);
            mStopForeground = getClass().getMethod("stopForeground",
                    mStopForegroundSignature);
            return;
        } catch (NoSuchMethodException e) {
            mStartForeground = mStopForeground = null;
        }

        try {
            mSetForeground = getClass().getMethod("setForeground",
                    mSetForegroundSignature);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "OS doesn't have Service.startForeground OR Service.setForeground!");
        }

    }

    @Override
    public void onDestroy() {
        handleDestory();
        stopForegroundCompat(R.string.foreground_service_started);
    }


    /**
     * 因为是作为“Started” Service来设计的，因此需定义onStartCommand ,
     * 同样onStartCommand也是在Android 2.0之后添加的，2.0之前为onStart。
     * 本例为了支持所有版本，两个方法对实现了，对应2.0之后的版本，只会调用onStartCommand，2.0之前的只会调用onStart
     */
    @Override
    public void onStart(Intent intent, int startId) {
        handleCommand(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent);
        // onStartCommand 可以有返回结果，这个返回值告诉Android系统当这个Service被Kill之后（比如当系统内存不足时）
        // 后续操作。START_STICKY 表示系统Kill这个Service之后，如果重新创建这个Service时在调用onStartCommand ，
        // 不会将最后的Intent作为参数传入，也就是说intent=null. START_REDELIVER_INTENT则会传入被杀前未处理的最后一个Intent。
        return START_STICKY;
    }

    private PowerManager.WakeLock mWakeLock;
    private Handler mHandler = new Handler();
    private Runnable mPulser = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(this, 5*1000);
        }
    };

    void handleCommand(Intent intent){
        if(ACTION_FOREGROUND.equals(intent.getAction())
            || ACTION_FOREGROUND_WAKELOCK.equals(intent.getAction())){
            CharSequence text = getText(R.string.foreground_service_started);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, Controller.class), 0);

            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.head)
                    .setTicker(text)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(getText(R.string.alarm_service_label))
                    .setContentText(text)
                    .setContentIntent(contentIntent)
                    .build();
             startForegroundCompat(R.string.foreground_service_started, notification);
        } else if(ACTION_BACKGROUND.equals(intent.getAction())
                || ACTION_FOREGROUND_WAKELOCK.equals(intent.getAction())){
            stopForegroundCompat(R.string.foreground_service_started);
        }

        //PARTIAL_WAKE_LOCK 保证CPU保持高性能运行，而屏幕和键盘背光（也可能是触摸按键的背光）关闭
        if(ACTION_FOREGROUND_WAKELOCK.equals(intent.getAction())
                ||ACTION_BACKGROUND_WAKELOCK.equals(intent.getAction())){
            if(mWakeLock == null){
                mWakeLock = ((PowerManager)getSystemService(Context.POWER_SERVICE)).newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK, "wake-service");
                //获得WakeLock,唤醒CPU
                mWakeLock.acquire();
            }else {
                releaseWakeLock();
            }
        }

        mHandler.removeCallbacks(mPulser);
        mPulser.run();
    }

    void releaseWakeLock(){
        if(mWakeLock != null){
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    void handleDestory(){
        releaseWakeLock();
        mHandler.removeCallbacks(mPulser);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class Controller extends AppCompatActivity implements View.OnClickListener {
        private Button startForeground;
        private Button startForegroundWakelock;
        private Button startBackground;
        private Button startBackgroundWakelock;
        private Button stop;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_foreground_service);
            initView();
        }

        private void initView() {
            startForeground = (Button) findViewById(R.id.start_foreground);
            startForegroundWakelock = (Button) findViewById(R.id.start_foreground_wakelock);
            startBackground = (Button) findViewById(R.id.start_background);
            startBackgroundWakelock = (Button) findViewById(R.id.start_background_wakelock);
            stop = (Button) findViewById(R.id.stop);

            startForeground.setOnClickListener(this);
            startForegroundWakelock.setOnClickListener(this);
            startBackground.setOnClickListener(this);
            startBackgroundWakelock.setOnClickListener(this);
            stop.setOnClickListener(this);

        }

        /**
         * 启动/停止这个Service， Controller 是作为这个Service的控制类来实现的，提供了前台启动，后台启动，和停止Service操作
         */
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.start_foreground:
                    Intent intent = new Intent(ForegroundService.ACTION_FOREGROUND);
                    intent.setClass(Controller.this, ForegroundService.class);
                    startService(intent);
                    break;
                case R.id.start_foreground_wakelock:
                    Intent intent1 = new Intent(ForegroundService.ACTION_FOREGROUND_WAKELOCK);
                    intent1.setClass(Controller.this, ForegroundService.class);
                    startService(intent1);
                    break;
                case R.id.start_background:
                    Intent intent2 = new Intent(ForegroundService.ACTION_BACKGROUND);
                    intent2.setClass(Controller.this, ForegroundService.class);
                    startService(intent2);
                    break;
                case R.id.start_background_wakelock:
                    Intent intent3 = new Intent(ForegroundService.ACTION_BACKGROUND_WAKELOCK);
                    intent3.setClass(Controller.this, ForegroundService.class);
                    startService(intent3);
                    break;
                case R.id.stop:
                    stopService(new Intent(Controller.this,
                            ForegroundService.class));
                    break;
            }
        }
    }
}
