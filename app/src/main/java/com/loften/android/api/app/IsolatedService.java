package com.loften.android.api.app;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.loften.android.api.R;

/**
 * android:isolatedProcess ：设置 true 意味着，服务会在一个特殊的进程下运行，这个进程与系统其他进程分开且没有自己的权限。
 * 与其通信的唯一途径是通过服务的API(bind and start)。
 *
 * android:enabled：是否可以被系统实例化，默认为 true因为父标签 也有 enable 属性，所以必须两个都为默认值 true 的
 * 情况下服务才会被激活，否则不会激活。
 */
public class IsolatedService extends Service {

    final RemoteCallbackList<IRemoteServiceCallback> mCallbacks
            = new RemoteCallbackList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("IsolatedService", "Creating IsolatedService: " + this);
    }

    @Override
    public void onDestroy() {
        Log.i("IsolatedService", "Destroying IsolatedService: " + this);
        // Unregister all callbacks.
        mCallbacks.kill();
    }

    private final IRemoteService.Stub mBinder = new IRemoteService.Stub() {
        @Override
        public void registerCallback(IRemoteServiceCallback cb) throws RemoteException {
            if(cb != null)
                mCallbacks.register(cb);
        }

        @Override
        public void unregisterCallback(IRemoteServiceCallback cb) throws RemoteException {
            if(cb != null)
                mCallbacks.unregister(cb);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i("IsolatedService", "Task removed in " + this + ": " + rootIntent);
        stopSelf();
    }

    private void broadcastValue(int value) {
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
    }

    public static class Controller extends AppCompatActivity {

        static class ServiceInfo{
            final Activity mActivity;
            final Class<?> mClz;
            final TextView mStatus;
            boolean mServiceBound;
            IRemoteService mService;

            ServiceInfo(Activity activity, Class<?> clz,
                        int start, int stop, int bind, int status){
                mActivity = activity;
                mClz = clz;
                Button button = (Button)mActivity.findViewById(start);
                button.setOnClickListener(mStartListener);
                button = (Button)mActivity.findViewById(stop);
                button.setOnClickListener(mStopListener);
                CheckBox cb = (CheckBox)mActivity.findViewById(bind);
                cb.setOnClickListener(mBindListener);
                mStatus = (TextView)mActivity.findViewById(status);
            }

            void destroy() {
                if (mServiceBound) {
                    mActivity.unbindService(mConnection);
                }
            }

            private View.OnClickListener mStartListener = new View.OnClickListener() {
                public void onClick(View v) {
                    mActivity.startService(new Intent(mActivity, mClz));
                }
            };

            private View.OnClickListener mStopListener = new View.OnClickListener() {
                public void onClick(View v) {
                    mActivity.stopService(new Intent(mActivity, mClz));
                }
            };

            private View.OnClickListener mBindListener = new View.OnClickListener() {
                public void onClick(View v) {
                    if (((CheckBox)v).isChecked()) {
                        if (!mServiceBound) {
                            if (mActivity.bindService(new Intent(mActivity, mClz),
                                    mConnection, Context.BIND_AUTO_CREATE)) {
                                mServiceBound = true;
                                mStatus.setText("BOUND");
                            }
                        }
                    } else {
                        if (mServiceBound) {
                            mActivity.unbindService(mConnection);
                            mServiceBound = false;
                            mStatus.setText("");
                        }
                    }
                }
            };

            private ServiceConnection mConnection = new ServiceConnection() {
                public void onServiceConnected(ComponentName className,
                                               IBinder service) {
                    mService = IRemoteService.Stub.asInterface(service);
                    if (mServiceBound) {
                        mStatus.setText("CONNECTED");
                    }
                }

                public void onServiceDisconnected(ComponentName className) {
                    // This is called when the connection with the service has been
                    // unexpectedly disconnected -- that is, its process crashed.
                    mService = null;
                    if (mServiceBound) {
                        mStatus.setText("DISCONNECTED");
                    }
                }
            };
        }

        ServiceInfo mService1;
        ServiceInfo mService2;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_isolated_service);

            mService1 = new ServiceInfo(this, IsolatedService.class,R.id.start1, R.id.stop1,
                    R.id.bind1, R.id.status1);
            mService2 = new ServiceInfo(this, IsolatedService2.class, R.id.start2, R.id.stop2,
                    R.id.bind2, R.id.status2);
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            mService1.destroy();
            mService2.destroy();
        }

    }
}
