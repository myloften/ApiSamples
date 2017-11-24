package com.loften.android.api.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.loften.android.api.R;

/**
 * 前面的例子忽略了一个重要的问题： Service 的onStartCommand 或是 onStart(2.1版本之前）是使用调用它的Android组件
 * （通常是Activity）同一个Thread来执行的，对应Activity来说，这个Thread通常是UI Thread，
 * 前面的Service例子都是提供非常简单的服务，对于UI 性能不会有很大影响，但如果在Service中使用了较费时的操作，
 * 如果网络访问，数据库查询，如果还是使用UI Thread来运行的话，就可能大大降低UI的响应性能，
 * 甚至出现ANR（Application Not Response）对话框，所以通常都是在Service新建一个线程来处理来自Client的请求。
 */
/**
 *  使用一个新创建的Thread来处理来自Client的消息，这个Thread不是UI Thread，使用HandlerThread 创建一个带Looper的线程，
 *  而具体的消息处理是由ServiceHandler的handleMessage来处理。
 *  Message类定义了消息，可以含有对消息的描述description，类型what，缺省带arg1,arg2, obj三个参数可以直接使用。
 *  并提供了obtain()静态函数来构造一个新的Message对象。
 *  ServiceStartArguments.Controller 为ServiceStartArguments Service的Client。
 *  它定义了四个按钮，通过Intent的Extra向Service传递参数：
 */
public class ServiceStartArguments extends Service {
    private NotificationManager mNM;
    private Intent mInvokeIntent;
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle arguments = (Bundle) msg.obj;

            String txt = arguments.getString("name");

            Log.i("ServiceStartArguments", "Message: " + msg + ", "
                    + arguments.getString("name"));

            if ((msg.arg2 & Service.START_FLAG_REDELIVERY) == 0) {
                txt = "New cmd #" + msg.arg1 + ": " + txt;
            } else {
                txt = "Re-delivered #" + msg.arg1 + ": " + txt;
            }

            showNotification(txt);

            // Normally we would do some work here...  for our sample, we will
            // just sleep for 5 seconds.
            long endTime = System.currentTimeMillis() + 5 * 1000;
            while (System.currentTimeMillis() < endTime) {
                synchronized (this) {
                    try {
                        wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
            }

            hideNotification();

            Log.i("ServiceStartArguments", "Done with #" + msg.arg1);
            stopSelf(msg.arg1);
        }

    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Toast.makeText(this, R.string.service_created,
                Toast.LENGTH_SHORT).show();

        // This is who should be launched if the user selects our persistent
        // notification.
        mInvokeIntent = new Intent(this, Controller.class);

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("ServiceStartArguments",
                "Starting #" + startId + ": " + intent.getExtras());
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.arg2 = flags;
        msg.obj = intent.getExtras();
        mServiceHandler.sendMessage(msg);
        Log.i("ServiceStartArguments", "Sending: " + msg);

        // For the start fail button, we will simulate the process dying
        // for some reason in onStartCommand().
        if (intent.getBooleanExtra("fail", false)) {
            // Don't do this if we are in a retry... the system will
            // eventually give up if we keep crashing.
            if ((flags & START_FLAG_RETRY) == 0) {
                // Since the process hasn't finished handling the command,
                // it will be restarted with the command again, regardless of
                // whether we return START_REDELIVER_INTENT.
                Process.killProcess(Process.myPid());
            }
        }

        // Normally we would consistently return one kind of result...
        // however, here we will select between these two, so you can see
        // how they impact the behavior.  Try killing the process while it
        // is in the middle of executing the different commands.
        return intent.getBooleanExtra("redeliver", false)
                ? START_REDELIVER_INTENT : START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mServiceLooper.quit();

        hideNotification();

        // Tell the user we stopped.
        Toast.makeText(ServiceStartArguments.this, R.string.service_destroyed,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification(String text) {
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Controller.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification.Builder noteBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.head)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.service_start_arguments_label))  // the label
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent);  // The intent to send when the entry is clicked

        // We show this for as long as our service is processing a command.
        noteBuilder.setOngoing(true);

        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        mNM.notify(R.string.service_created, noteBuilder.build());
    }

    private void hideNotification() {
        mNM.cancel(R.string.service_created);
    }

    public static class Controller extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_service_start_arguments);

            Button button = (Button)findViewById(R.id.start1);
            button.setOnClickListener(mStart1Listener);
            button = (Button)findViewById(R.id.start2);
            button.setOnClickListener(mStart2Listener);
            button = (Button)findViewById(R.id.start3);
            button.setOnClickListener(mStart3Listener);
            button = (Button)findViewById(R.id.startfail);
            button.setOnClickListener(mStartFailListener);
            button = (Button)findViewById(R.id.kill);
            button.setOnClickListener(mKillListener);
        }

        private View.OnClickListener mStart1Listener = new View.OnClickListener() {
            public void onClick(View v) {
                startService(new Intent(Controller.this,
                        ServiceStartArguments.class)
                        .putExtra("name", "One"));
            }
        };

        private View.OnClickListener mStart2Listener = new View.OnClickListener() {
            public void onClick(View v) {
                startService(new Intent(Controller.this,
                        ServiceStartArguments.class)
                        .putExtra("name", "Two"));
            }
        };

        private View.OnClickListener mStart3Listener = new View.OnClickListener() {
            public void onClick(View v) {
                startService(new Intent(Controller.this,
                        ServiceStartArguments.class)
                        .putExtra("name", "Three")
                        .putExtra("redeliver", true));
            }
        };

        private View.OnClickListener mStartFailListener = new View.OnClickListener() {
            public void onClick(View v) {
                startService(new Intent(Controller.this,
                        ServiceStartArguments.class)
                        .putExtra("name", "Failure")
                        .putExtra("fail", true));
            }
        };

        private View.OnClickListener mKillListener = new View.OnClickListener() {
            public void onClick(View v) {
                // This is to simulate the service being killed while it is
                // running in the background.
                Process.killProcess(Process.myPid());
            }
        };
    }
}
