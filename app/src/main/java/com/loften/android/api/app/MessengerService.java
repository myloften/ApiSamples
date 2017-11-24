package com.loften.android.api.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.loften.android.api.R;

import java.util.ArrayList;

/**
 * 这里service可远程交互 却不用通过AIDL接口，主要使用Messenger传递信息
 */
public class MessengerService extends Service {
    NotificationManager mNM;
    //保存所有的注册对象
    ArrayList<Messenger> mClients = new ArrayList<>();
    int mValue = 0;

    //命令service注册client对象， 接收从service传回的消息
    static final int MSG_REGISTER_CLIENT = 1;

    //命令service注销client对象 或 停止接收从service接收回调消息
    static final int MSG_UNREGISTER_CLIENT = 2;

    //命令service设置一个新值，重新传回给 注册对象
    static final int MSG_SET_VALUE = 3;

    /**
     * 对象 通过handler 传入message
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_VALUE:
                    mValue = msg.arg1;
                    for(int i = mClients.size()-1; i >= 0; i--){
                        try {
                            mClients.get(i).send(Message.obtain(null,
                                MSG_SET_VALUE, mValue, 0));
                        } catch (RemoteException e) {
                            // The client is dead.  Remove it from the list;
                            // we are going through the list from back to front
                            // so this is safe to do inside the loop.
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        showNotification();
    }

    @Override
    public void onDestroy() {
        mNM.cancel(R.string.remote_service_started);

        Toast.makeText(this, R.string.remote_service_stopped, Toast.LENGTH_SHORT).show();
    }

    /**
     * 这种方法采用的“Bound” Service模式，onBind 需要返回一个IBind对象, 可以通过mMessenger.getBinder()
     * 返回与这个Messenger关联的IBinder对象，Client可以通过这个IBinder对象重新构造一个Messenger对象，从而
     * 建立起与Service之间的通信链路。
     */
    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    private void showNotification(){
        CharSequence text = getText(R.string.remote_service_started);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, RemoteService.Controller.class), 0);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.head)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("local service")  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        mNM.notify(R.string.remote_service_started, notification);
    }
}
