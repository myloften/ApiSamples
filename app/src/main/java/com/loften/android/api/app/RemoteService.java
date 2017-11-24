package com.loften.android.api.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loften.android.api.R;

/**
 * Android Interface Definition Language（AIDL）和其它一些支持远程方法调用RMI的系统的IDL类似，它定义了
 * Service和Client 之间的使用接口约定，这种远程调用一般需要通过进程间通信机制(IPC)来实现。在Android系统，
 * 一个进程（Process)通常不能直接访问其它进程的内存空间，Android系统支持使用AIDL来实现使用不同进程间调用服务接口。
 * 在设计AIDL接口之前，要意识到使用AIDL接口是通过直接函数调用的方法来进行的，但这种远程调用所发生的线程Thread随调用者
 * 是否和Service提供者属于同一进程Process的不同而不同：
 * 如果调用者与Service属于同一个进程（可以称为Local Process），那么AIDL Call将使用与调用者同一线程执行。因此如果
 * 你的应用只使用Local Process来访问AIDL Call，那么根本就无必要使用AIDL接口，使用Binder即可。
 * 如果使用Remote Process方式来调用AIDL ,Android将会使用由本进程管理的线程池(Thread pool)来分发函数调用。
 * 因此你的Service需要能够处理多进程触发的AIDL Call，换句话来说，AIDL接口的实现必须是Thread-safe的。
 */

/**
 * 定义AIDL 接口的步骤如下：
 * 1. 创建. aidl 文件(aidl文件夹内)
 * AIDL接口定义使用和Java Interface定义同样的语法，每个.aidl文件只能定义一个调用接口，而且只能定义接口方法，
 * 不能含有静态变量定义。AIDL缺省支持 int ,long, char, boolean, String, CharSequence, List ,Map 变量类型，
 * 也可以引用其它 .aidl中定义的类型。
 * 2. 实现这个AIDL接口(as make下就在build中生成)
 * 3. Expose the interface to Clients
 * 在定义.aidl 和实现AIDL接口定义后，就需要将这个接口提供给Client使用。方法是派生Service并提供onBind方法
 * 返回上面实现的Stub的一个实例。
 */
/**
 * RemoteService 中定义了两个.aidl 接口可供Client使用IRemoteService.aidl和ISecondary.aidl。
 * 有了AIDL定义并在Service中定义了可供Client使用的AIDL实现。下面再来看看Client的实现步骤：
 * 1.将. aidl定义包含着 src目录下，由于本例Service ,Client 都包含在ApiDemos中，.aidl已在src中定义了。
 * 2.根据.aidl接口生成IBinder接口定义（编译时由Android SDK工具自动生成）。
 * 3.实现ServiceConnection接口
 * 4.调用Context.bindService 来绑定需调用的Service。
 * 5.在ServiceConnection 的onServiceConnected方法中，根据传入的IBinder对象（被调用的Service），使用 YourInterfaceName.Stub.asInterface((IBinder)service)) 将 service转换为YourInterfaceName类型。
 * 6.调用YourInterfaceName定义的方法，这里需要捕获DeadObjectException 异常，DeadObjectException会在链接断裂时抛出。
 * 7.使用完Service，使用Context.unbindService断开与Service之间的绑定。
 * Remote Service Binding 例子中 Service 端实现了两个Service：
 * IRemoteService ：提供registerCallback，unregisterCallback用来在RemoteCallbackList 中注册或注销一个Client的Callback。
 * ISecondary： 实际Client会调用的服务，getPid返回当前进程Process ID。basicTypes 介绍了一般参数类型用法，本例中Client为使用。
 */
public class RemoteService extends Service {

    final RemoteCallbackList<IRemoteServiceCallback> mCallbacks
            = new RemoteCallbackList<>();

    int mValue = 0;
    NotificationManager mNM;

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        showNotification();

        mHandler.sendEmptyMessage(REPORT_MSG);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("RemoteService", "Received start id " + startId + ": " + intent);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(R.string.remote_service_started);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.remote_service_stopped, Toast.LENGTH_SHORT).show();

        // Unregister all callbacks.
        mCallbacks.kill();

        // Remove the next pending message to increment the counter, stopping
        // the increment loop.
        mHandler.removeMessages(REPORT_MSG);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (IRemoteService.class.getName().equals(intent.getAction())) {
            return mBinder;
        }
        if (ISecondary.class.getName().equals(intent.getAction())) {
            return mSecondaryBinder;
        }
        return null;
    }

    private final IRemoteService.Stub mBinder = new IRemoteService.Stub() {
        public void registerCallback(IRemoteServiceCallback cb) {
            if (cb != null) mCallbacks.register(cb);
        }
        public void unregisterCallback(IRemoteServiceCallback cb) {
            if (cb != null) mCallbacks.unregister(cb);
        }
    };

    private final ISecondary.Stub mSecondaryBinder = new ISecondary.Stub() {
        public int getPid() {
            return Process.myPid();
        }
        public void basicTypes(int anInt, long aLong, boolean aBoolean,
                               float aFloat, double aDouble, String aString) {
        }
    };

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Toast.makeText(this, "Task removed: " + rootIntent, Toast.LENGTH_LONG).show();
    }

    private static final int REPORT_MSG = 1;

    private final Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {

                // It is time to bump the value!
                case REPORT_MSG: {
                    // Up it goes.
                    int value = ++mValue;

                    // Broadcast to all clients the new value.
                    final int N = mCallbacks.beginBroadcast();
                    for (int i=0; i<N; i++) {
                        try {
                            mCallbacks.getBroadcastItem(i).valueChanged(value);
                        } catch (RemoteException e) {
                            // The RemoteCallbackList will take care of removing
                            // the dead object for us.
                        }
                    }
                    mCallbacks.finishBroadcast();

                    // Repeat every 1 second.
                    sendMessageDelayed(obtainMessage(REPORT_MSG), 1*1000);
                } break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.remote_service_started);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Controller.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.head)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.remote_service_label))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        mNM.notify(R.string.remote_service_started, notification);
    }

    public static class Controller extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_remote_service);

            Button button = (Button) findViewById(R.id.start);
            button.setOnClickListener(mStartListener);
            button = (Button) findViewById(R.id.stop);
            button.setOnClickListener(mStopListener);
        }

        private View.OnClickListener mStartListener = new View.OnClickListener() {
            public void onClick(View v) {
                startService(new Intent(Controller.this, RemoteService.class));
            }
        };

        private View.OnClickListener mStopListener = new View.OnClickListener() {
            public void onClick(View v) {
                stopService(new Intent(Controller.this, RemoteService.class));
            }
        };
    }

    public static class Binding extends AppCompatActivity {
        IRemoteService mService = null;
        ISecondary mSecondaryService = null;

        Button mKillButton;
        TextView mCallbackText;

        private boolean mIsBound;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.remote_service_binding);

            Button button = (Button) findViewById(R.id.bind);
            button.setOnClickListener(mBindListener);
            button = (Button) findViewById(R.id.unbind);
            button.setOnClickListener(mUnbindListener);
            mKillButton = (Button) findViewById(R.id.kill);
            mKillButton.setOnClickListener(mKillListener);
            mKillButton.setEnabled(false);

            mCallbackText = (TextView) findViewById(R.id.callback);
            mCallbackText.setText("Not attached.");
        }

        private ServiceConnection mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                mService = IRemoteService.Stub.asInterface(service);
                mKillButton.setEnabled(true);
                mCallbackText.setText("Attached.");

                try {
                    mService.registerCallback(mCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                Toast.makeText(Binding.this, R.string.remote_service_connected,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mService = null;
                mKillButton.setEnabled(false);
                mCallbackText.setText("Disconnected.");

                Toast.makeText(Binding.this, R.string.remote_service_disconnected,
                        Toast.LENGTH_SHORT).show();
            }
        };

        private ServiceConnection mSecondaryConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                // Connecting to a secondary interface is the same as any
                // other interface.
                mSecondaryService = ISecondary.Stub.asInterface(service);
                mKillButton.setEnabled(true);
            }

            public void onServiceDisconnected(ComponentName className) {
                mSecondaryService = null;
                mKillButton.setEnabled(false);
            }
        };

        private View.OnClickListener mBindListener = new View.OnClickListener() {
            public void onClick(View v) {
                // Establish a couple connections with the service, binding
                // by interface names.  This allows other applications to be
                // installed that replace the remote service by implementing
                // the same interface.
                //这里的Context.BIND_AUTO_CREATE，这意味这如果在绑定的过程中，如果Service由于某种原因被Destroy了，
                // Android还会自动重新启动被绑定的Service。
                Intent intent = new Intent(Binding.this, RemoteService.class);
                intent.setAction(IRemoteService.class.getName());
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                intent.setAction(ISecondary.class.getName());
                bindService(intent, mSecondaryConnection, Context.BIND_AUTO_CREATE);
                mIsBound = true;
                mCallbackText.setText("Binding.");
            }
        };

        private View.OnClickListener mUnbindListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (mIsBound) {
                    // If we have received the service, and hence registered with
                    // it, then now is the time to unregister.
                    if (mService != null) {
                        try {
                            mService.unregisterCallback(mCallback);
                        } catch (RemoteException e) {
                            // There is nothing special we need to do if the service
                            // has crashed.
                        }
                    }

                    // Detach our existing connection.
                    unbindService(mConnection);
                    unbindService(mSecondaryConnection);
                    mKillButton.setEnabled(false);
                    mIsBound = false;
                    mCallbackText.setText("Unbinding.");
                }
            }
        };

        private View.OnClickListener mKillListener = new View.OnClickListener() {
            public void onClick(View v) {
                // To kill the process hosting our service, we need to know its
                // PID.  Conveniently our service has a call that will return
                // to us that information.
                if (mSecondaryService != null) {
                    try {
                        int pid = mSecondaryService.getPid();
                        // Note that, though this API allows us to request to
                        // kill any process based on its PID, the kernel will
                        // still impose standard restrictions on which PIDs you
                        // are actually able to kill.  Typically this means only
                        // the process running your application and any additional
                        // processes created by that app as shown here; packages
                        // sharing a common UID will also be able to kill each
                        // other's processes.
                        Process.killProcess(pid);
                        mCallbackText.setText("Killed service process.");
                    } catch (RemoteException ex) {
                        // Recover gracefully from the process hosting the
                        // server dying.
                        // Just for purposes of the sample, put up a notification.
                        Toast.makeText(Binding.this,
                                R.string.remote_call_failed,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        private IRemoteServiceCallback mCallback = new IRemoteServiceCallback.Stub() {
            @Override
            public void valueChanged(int value) throws RemoteException {

            }
        };

        private static final int BUMP_MSG = 1;

        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case BUMP_MSG:
                        mCallbackText.setText("Received from service: " + msg.arg1);
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };
    }

    public static class BindingOptions extends AppCompatActivity{
        ServiceConnection mCurConnection;
        TextView mCallbackText;
        Intent mBindIntent;

        class MyServiceConnection implements ServiceConnection{
            final boolean mUnbindOnDisconnect;

            public MyServiceConnection(){
                mUnbindOnDisconnect = false;
            }

            public MyServiceConnection(boolean unbindOnDisconnect){
                mUnbindOnDisconnect = unbindOnDisconnect;
            }

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                if(mCurConnection != this){
                    return;
                }
                mCallbackText.setText("Attached.");
                Toast.makeText(BindingOptions.this, R.string.remote_service_connected,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                if(mCurConnection != this){
                    return;
                }
                mCallbackText.setText("Disconnected.");
                Toast.makeText(BindingOptions.this, R.string.remote_service_disconnected,
                        Toast.LENGTH_SHORT).show();
                if (mUnbindOnDisconnect) {
                    unbindService(this);
                    mCurConnection = null;
                    Toast.makeText(BindingOptions.this, R.string.remote_service_unbind_disconn,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.remote_binding_options);

            // Watch for button clicks.
            Button button = (Button)findViewById(R.id.bind_normal);
            button.setOnClickListener(mBindNormalListener);
            button = (Button)findViewById(R.id.bind_not_foreground);
            button.setOnClickListener(mBindNotForegroundListener);
            button = (Button)findViewById(R.id.bind_above_client);
            button.setOnClickListener(mBindAboveClientListener);
            button = (Button)findViewById(R.id.bind_allow_oom);
            button.setOnClickListener(mBindAllowOomListener);
            button = (Button)findViewById(R.id.bind_waive_priority);
            button.setOnClickListener(mBindWaivePriorityListener);
            button = (Button)findViewById(R.id.bind_important);
            button.setOnClickListener(mBindImportantListener);
            button = (Button)findViewById(R.id.bind_with_activity);
            button.setOnClickListener(mBindWithActivityListener);
            button = (Button)findViewById(R.id.unbind);
            button.setOnClickListener(mUnbindListener);

            mCallbackText = (TextView)findViewById(R.id.callback);
            mCallbackText.setText("Not attached.");

            mBindIntent = new Intent(this, RemoteService.class);
            mBindIntent.setAction(IRemoteService.class.getName());
        }

        private View.OnClickListener mBindNormalListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (mCurConnection != null) {
                    unbindService(mCurConnection);
                    mCurConnection = null;
                }
                ServiceConnection conn = new MyServiceConnection();
                if (bindService(mBindIntent, conn, Context.BIND_AUTO_CREATE)) {
                    mCurConnection = conn;
                }
            }
        };

        private View.OnClickListener mBindNotForegroundListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (mCurConnection != null) {
                    unbindService(mCurConnection);
                    mCurConnection = null;
                }
                ServiceConnection conn = new MyServiceConnection();
                if (bindService(mBindIntent, conn,
                        Context.BIND_AUTO_CREATE | Context.BIND_NOT_FOREGROUND)) {
                    mCurConnection = conn;
                }
            }
        };

        private View.OnClickListener mBindAboveClientListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (mCurConnection != null) {
                    unbindService(mCurConnection);
                    mCurConnection = null;
                }
                ServiceConnection conn = new MyServiceConnection();
                if (bindService(mBindIntent,
                        conn, Context.BIND_AUTO_CREATE | Context.BIND_ABOVE_CLIENT)) {
                    mCurConnection = conn;
                }
            }
        };

        private View.OnClickListener mBindAllowOomListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (mCurConnection != null) {
                    unbindService(mCurConnection);
                    mCurConnection = null;
                }
                ServiceConnection conn = new MyServiceConnection();
                if (bindService(mBindIntent, conn,
                        Context.BIND_AUTO_CREATE | Context.BIND_ALLOW_OOM_MANAGEMENT)) {
                    mCurConnection = conn;
                }
            }
        };

        private View.OnClickListener mBindWaivePriorityListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (mCurConnection != null) {
                    unbindService(mCurConnection);
                    mCurConnection = null;
                }
                ServiceConnection conn = new MyServiceConnection(true);
                if (bindService(mBindIntent, conn,
                        Context.BIND_AUTO_CREATE | Context.BIND_WAIVE_PRIORITY)) {
                    mCurConnection = conn;
                }
            }
        };

        private View.OnClickListener mBindImportantListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (mCurConnection != null) {
                    unbindService(mCurConnection);
                    mCurConnection = null;
                }
                ServiceConnection conn = new MyServiceConnection();
                if (bindService(mBindIntent, conn,
                        Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT)) {
                    mCurConnection = conn;
                }
            }
        };

        private View.OnClickListener mBindWithActivityListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (mCurConnection != null) {
                    unbindService(mCurConnection);
                    mCurConnection = null;
                }
                ServiceConnection conn = new MyServiceConnection();
                if (bindService(mBindIntent, conn,
                        Context.BIND_AUTO_CREATE | Context.BIND_ADJUST_WITH_ACTIVITY
                                | Context.BIND_WAIVE_PRIORITY)) {
                    mCurConnection = conn;
                }
            }
        };

        private View.OnClickListener mUnbindListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (mCurConnection != null) {
                    unbindService(mCurConnection);
                    mCurConnection = null;
                }
            }
        };
    }
}
