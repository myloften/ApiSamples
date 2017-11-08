package com.loften.android.api.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.loften.android.api.R;

public class AlarmService_Service extends Service {
    NotificationManager mNM;

    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        showNotification();

        Thread thr = new Thread(null, mTask, "AlarmService_Service");
        thr.start();
    }

    @Override
    public void onDestroy() {
        mNM.cancel(1);
        Toast.makeText(this, R.string.alarm_service_finished, Toast.LENGTH_SHORT).show();

        super.onDestroy();
    }

    private void showNotification() {
        CharSequence text = getString(R.string.alarm_service_started);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, AlarmService.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.head)
                        .setTicker(text)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(getString(R.string.alarm_service_label))
                        .setContentText(text)
                        .setContentIntent(contentIntent);


        mNM.notify(1, mBuilder.build());
    }

    Runnable mTask = new Runnable() {
        @Override
        public void run() {
            long endTime = System.currentTimeMillis() + 10 * 1000;
            while (System.currentTimeMillis() < endTime) {
                synchronized (mBinder) {
                    try {
                        mBinder.wait(endTime - System.currentTimeMillis());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            //停止service
            AlarmService_Service.this.stopSelf();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };
}
