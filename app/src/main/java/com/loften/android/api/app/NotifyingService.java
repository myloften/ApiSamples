package com.loften.android.api.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import com.loften.android.api.R;

public class NotifyingService extends Service {

    private static int MOOD_NOTIFICATIONS = R.layout.status_bar_notifications;

    private ConditionVariable mCondition;

    private NotificationManager mNM;

    private final IBinder mBinder = new Binder(){
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };

    private Runnable mTask = new Runnable() {
        @Override
        public void run() {
            /**
             * 在一分钟内每隔5秒钟显示一个Notification
             */
            for(int i = 0; i < 4; ++i){
                showNotification(R.drawable.stat_happy,
                        R.string.status_bar_notifications_happy_message);
                if (mCondition.block(5 * 1000))
                    break;
                showNotification(R.drawable.stat_neutral,
                        R.string.status_bar_notifications_ok_message);
                if (mCondition.block(5 * 1000))
                    break;
                showNotification(R.drawable.stat_sad,
                        R.string.status_bar_notifications_sad_message);
                if (mCondition.block(5 * 1000))
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Thread notifyingThread = new Thread(null, mTask, "NotifyingService");
        mCondition = new ConditionVariable(false);
        notifyingThread.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void showNotification(int moodId, int textId){
        CharSequence text = getText(textId);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NotifyingController.class), 0);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(moodId)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),moodId))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getText(R.string.status_bar_notifications_mood_title))
                .setContentText(text)
                .setContentIntent(contentIntent)
                .build();
        mNM.notify(MOOD_NOTIFICATIONS, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mNM.cancel(MOOD_NOTIFICATIONS);
        mCondition.open();
    }
}
