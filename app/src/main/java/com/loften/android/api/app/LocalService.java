package com.loften.android.api.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.loften.android.api.R;

/**
 * Created by lcw on 2017/11/22.
 */

public class LocalService extends Service {

    private NotificationManager mNM;

    private int NOTIFICATION = R.string.local_service_started;

    public class LocalBindel extends Binder {
        LocalService getService(){
            return LocalService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        showNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mNM.cancel(NOTIFICATION);

        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
    }

    private void showNotification() {
        CharSequence text = getText(R.string.local_service_started);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, LocalServiceActivity.Controller.class), 0);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.head)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("Local Service")
                .setContentText(text)
                .setContentIntent(contentIntent)
                .build();
        mNM.notify(NOTIFICATION, notification);
    }

    private final IBinder mBinder = new LocalBindel();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
